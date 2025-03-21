/*
 * Copyright 2025 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.bbriccs.fhir.coding.version;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.exceptions.FhirVersionException;
import de.gematik.bbriccs.fhir.conf.ProfileDto;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.toggle.FeatureToggle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.val;

public class VersionUtil {

  public static final Pattern SEMVER_REGEX =
      Pattern.compile("(\\d{1,3}+\\.\\d+(\\.(?<patch>\\d+))?)");
  private static final String PATCH_GROUP = "patch";

  private VersionUtil() {
    throw new IllegalAccessError("Utility class");
  }

  public static String parseVersion(String input) {
    val matcher = SEMVER_REGEX.matcher(input);
    if (!matcher.find()) {
      throw new FhirVersionException(format("Given input does not contain a version: {0}", input));
    }

    return matcher.group(0);
  }

  public static String omitPatch(String input) {
    val matcher = SEMVER_REGEX.matcher(input);
    if (matcher.find()) {
      val patchGroup = matcher.group(PATCH_GROUP);
      return Optional.ofNullable(patchGroup)
          .map(
              v -> {
                val patchIdx = matcher.start(PATCH_GROUP) - 1;
                return input.substring(0, patchIdx);
              })
          .orElse(input);
    } else {
      return input;
    }
  }

  public static String omitZeroPatch(String input) {
    val matcher = SEMVER_REGEX.matcher(input);
    if (matcher.find()) {
      val patchGroup = matcher.group(PATCH_GROUP);
      if (patchGroup != null && patchGroup.equals("0")) {
        val patchIdx = matcher.start(PATCH_GROUP) - 1;
        return input.substring(0, patchIdx);
      } else {
        return input;
      }
    } else {
      return input;
    }
  }

  /**
   * Compares the two versions by taking ZeroPatches into account. Meaning that the following
   * combinations will be equal.
   *
   * <p>This method is equivalent to
   *
   * <pre><code>return omitZeroPatch(left).equals(omitZeroPatch(right))</code></pre>
   *
   * <p>and
   *
   * <pre><code>return compare(left, right) == 0</code></pre>
   *
   * <p>but slightly faster
   *
   * <ul>
   *   <li>1.2.3 == 1.2.3
   *   <li>1.2.0 == 1.2
   *   <li>1.0 == 1.0.0
   * </ul>
   *
   * <p>While the following combinations won't be equal
   *
   * <ul>
   *   <li>1.2.3 != 1.2.0
   *   <li>1.0 != 1.0.1
   * </ul>
   *
   * <p>There is also a special case for invalid SemVer Strings. If at least one of the given
   * Versions does not comply with the pattern MAJOR.MINOR.PATCH? the response will be false
   *
   * @param left version for comparison
   * @param right version for comparison
   * @return true if both versions are equal
   */
  public static boolean areEqual(String left, String right) {
    val leftTokens = left.split("\\.");
    val rightTokens = right.split("\\.");

    if (leftTokens.length < 3) {
      left = format("{0}.0", left);
    }

    if (rightTokens.length < 3) {
      right = format("{0}.0", right);
    }

    return left.equals(right);
  }

  public static int compare(String left, String right) {
    // make sure versions have minor and patch in any case!!
    left += ".0.0";
    right += ".0.0";

    val leftTokens = left.split("\\.");
    val rightTokens = right.split("\\.");
    for (var i = 0; i < 3; i++) {
      val lt = Integer.parseInt(leftTokens[i]); // my version token
      val rt = Integer.parseInt(rightTokens[i]); // another version token
      if (rt != lt) {
        return (lt < rt) ? -1 : 1;
      }
    }

    return 0;
  }

  public static <T extends ProfileVersion> T fromString(Class<T> type, List<String> inputs) {
    val profileVersion = inputs.stream().map(VersionUtil::parseVersion).toList();

    if (type.isEnum()) {
      return fromEnumeratedVersionType(type, profileVersion);
    } else {
      // Note: on Class-VersionType currently only the concrete version is supported
      // and compatible versions will be omitted
      return fromClassVersionType(type, profileVersion.get(0));
    }
  }

  public static <T extends ProfileVersion> T fromString(Class<T> type, String... inputs) {
    return fromString(type, Arrays.asList(inputs));
  }

  public static <T extends ProfileVersion> Optional<T> getDefaultVersionOptionally(
      Class<T> type, String profileName) {
    // if a profile name is configured by a feature toggle use always this version,
    // otherwise fallback to the configuration from the virtual default profile
    val profileVersions =
        FeatureToggle.getStringToggle(profileName)
            .map(List::of)
            .orElseGet(
                () ->
                    ProfilesConfigurator.getVirtualDefaultProfile(profileName)
                        .map(ProfileDto::getAllVersions)
                        .orElse(List.of()));
    return Optional.of(profileVersions)
        .filter(list -> !list.isEmpty())
        .map(list -> fromString(type, list));
  }

  public static <T extends ProfileVersion> T getDefaultVersion(Class<T> type, String profileName) {
    return getDefaultVersionOptionally(type, profileName)
        .orElseThrow(
            () -> {
              val defaultProfileId =
                  ProfilesConfigurator.getDefaultConfiguration().getDefaultProfile().getId();
              val defaultProfiles =
                  ProfilesConfigurator.getDefaultConfiguration()
                      .getDefaultProfile()
                      .getProfiles()
                      .stream()
                      .map(p -> format("\t{0}:{1}", p.getName(), p.getVersion()))
                      .collect(Collectors.joining("\n"));
              return new FhirVersionException(
                  format(
                      "Profile {0} not found in virtual configuration ''{1}'' which contains\n{2}",
                      profileName, defaultProfileId, defaultProfiles));
            });
  }

  @SuppressWarnings("unchecked")
  private static <T extends ProfileVersion> T fromClassVersionType(
      Class<T> type, String profileVersion) {
    val constructors = type.getConstructors();

    val version =
        Arrays.stream(constructors)
            .filter(
                c -> {
                  val paramTypes = c.getParameterTypes();
                  return paramTypes.length <= 1;
                })
            .sorted((c1, c2) -> c2.getParameterCount() - c1.getParameterCount())
            .map(
                c -> {
                  if (c.getParameterCount() == 0) {
                    return (Function<String, T>)
                        pv -> instantiateVersionClassConstructor((Constructor<T>) c, null);
                  } else {
                    return (Function<String, T>)
                        pv -> instantiateVersionClassConstructor((Constructor<T>) c, pv);
                  }
                })
            .findFirst()
            .map(constructionFunction -> constructionFunction.apply(profileVersion))
            .orElseThrow(
                () ->
                    new FhirVersionException(
                        format(
                            "Unable to find a proper Constructor to instantiate {0} with Version"
                                + " {1}",
                            type.getSimpleName(), profileVersion)));

    if (!version.isEqual(profileVersion)) {
      throw new FhirVersionException(
          format(
              "Given Profile Version {0} does not match the instantiated Version {1}",
              profileVersion, version.getVersion()));
    }
    return version;
  }

  private static <T extends ProfileVersion> T instantiateVersionClassConstructor(
      Constructor<T> constructor, @Nullable String profileVersion) {
    try {
      if (profileVersion != null) {
        return constructor.newInstance(profileVersion);
      } else {
        return constructor.newInstance();
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new FhirVersionException(
          format("Unable to instantiate Version class {0}", constructor.getClass().getSimpleName()),
          e);
    }
  }

  private static <T extends ProfileVersion> T fromEnumeratedVersionType(
      Class<T> type, List<String> profileVersions) {
    return Arrays.stream(type.getEnumConstants())
        .filter(version -> profileVersions.contains(version.getVersion()))
        .findFirst()
        .orElseThrow(
            () ->
                new FhirVersionException(
                    format(
                        "Profile version {0} is not known for {1}",
                        String.join("|", profileVersions), type.getSimpleName())));
  }
}
