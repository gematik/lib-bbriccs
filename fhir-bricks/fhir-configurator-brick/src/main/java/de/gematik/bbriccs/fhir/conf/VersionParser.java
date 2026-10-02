/*
 * Copyright (Change Date see Readme) gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.fhir.conf;

import static java.text.MessageFormat.format;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class VersionParser {

  public static final Pattern SEMVER_REGEX =
      Pattern.compile("(\\d{1,3}+\\.\\d+(\\.(?<patch>\\d+))?)");
  private static final String PATCH_GROUP = "patch";

  /**
   * Parses the first SemVer conforming version from the given input String
   *
   * @param input String possibly containing a SemVer version
   * @return Optional containing the found version or empty if the input does not contain a valid
   *     SemVer
   */
  public static Optional<String> parseVersion(String input) {
    val matcher = SEMVER_REGEX.matcher(input);
    if (!matcher.find()) {
      return Optional.empty();
    }

    return Optional.of(matcher.group(0));
  }

  /**
   * Omits the patch part of a SemVer version in the given input String without regard to its value
   *
   * @param input String possibly containing a SemVer version
   * @return input String with omitted patch part if found, else the original input
   */
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

  /**
   * Omits the patch part of a SemVer version in the given input String only if its value is zero.
   *
   * @param input String possibly containing a SemVer version
   * @return input String with omitted patch part if found and its value is zero, else the original
   *     input
   */
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
}
