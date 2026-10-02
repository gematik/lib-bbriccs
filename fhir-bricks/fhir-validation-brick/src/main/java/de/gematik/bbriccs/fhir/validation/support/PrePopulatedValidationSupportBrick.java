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

package de.gematik.bbriccs.fhir.validation.support;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.util.ILockable;
import de.gematik.bbriccs.fhir.conf.ProfileDto;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hl7.fhir.common.hapi.validation.support.BaseValidationSupport;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

/**
 * This class is an implementation of {@link IValidationSupport} which may be pre-populated with a
 * collection of validation resources to be used by the validator.
 */
@Slf4j
public class PrePopulatedValidationSupportBrick extends BaseValidationSupport
    implements IValidationSupport, ILockable {

  private static final String STRUCT_DEF = "StructureDefinition";
  private static final String VALUE_SET = "ValueSet";
  private static final String CODE_SYSTEM = "CodeSystem";
  private static final String SEARCH_PARAMETER = "SearchParameter";

  private final ProfileDto profile;
  private final Map<String, IBaseResource> myUrlToCodeSystems;
  private final Map<String, IBaseResource> myUrlToStructureDefinitions;
  private final Map<String, IBaseResource> myUrlToSearchParameters;
  private final Map<String, IBaseResource> myUrlToValueSets;
  private final List<IBaseResource> myCodeSystems;
  private final List<IBaseResource> myStructureDefinitions;
  private final List<IBaseResource> mySearchParameters;
  private final List<IBaseResource> myValueSets;
  private final Map<String, byte[]> myBinaries;
  private boolean myLocked;

  /** Constructor */
  public PrePopulatedValidationSupportBrick(ProfileDto profile, FhirContext theContext) {
    this(
        profile,
        theContext,
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>());
  }

  /**
   * Constructor
   *
   * @param theUrlToStructureDefinitions The StructureDefinitions to be returned by this module.
   *     Keys are the logical URL for the resource, and values are the resource itself.
   * @param theUrlToValueSets The ValueSets to be returned by this module. Keys are the logical URL
   *     for the resource, and values are the resource itself.
   * @param theUrlToCodeSystems The CodeSystems to be returned by this module. Keys are the logical
   *     URL for the resource, and values are the resource itself.
   * @param theBinaries The binary files to be returned by this module. Keys are the unique filename
   *     for the binary, and values are the contents of the file as a byte array.
   */
  private PrePopulatedValidationSupportBrick(
      ProfileDto profile,
      FhirContext theFhirContext,
      Map<String, IBaseResource> theUrlToStructureDefinitions,
      Map<String, IBaseResource> theUrlToValueSets,
      Map<String, IBaseResource> theUrlToCodeSystems,
      Map<String, IBaseResource> theUrlToSearchParameters,
      Map<String, byte[]> theBinaries) {
    super(theFhirContext);
    Validate.notNull(theFhirContext, "theFhirContext must not be null");
    Validate.notNull(theUrlToStructureDefinitions, "theStructureDefinitions must not be null");
    Validate.notNull(theUrlToValueSets, "theValueSets must not be null");
    Validate.notNull(theUrlToCodeSystems, "theCodeSystems must not be null");
    Validate.notNull(theUrlToSearchParameters, "theSearchParameters must not be null");
    Validate.notNull(theBinaries, "theBinaries must not be null");
    this.profile = profile;
    myUrlToStructureDefinitions = theUrlToStructureDefinitions;
    myStructureDefinitions =
        theUrlToStructureDefinitions.values().stream()
            .distinct()
            .collect(Collectors.toList()); // NOSONAR

    myUrlToValueSets = theUrlToValueSets;
    myValueSets =
        theUrlToValueSets.values().stream().distinct().collect(Collectors.toList()); // NOSONAR

    myUrlToCodeSystems = theUrlToCodeSystems;
    myCodeSystems =
        theUrlToCodeSystems.values().stream().distinct().collect(Collectors.toList()); // NOSONAR

    myUrlToSearchParameters = theUrlToSearchParameters;
    mySearchParameters =
        theUrlToSearchParameters.values().stream()
            .distinct()
            .collect(Collectors.toList()); // NOSONAR

    myBinaries = theBinaries;
  }

  @Override
  public String getName() {
    return getFhirContext().getVersion().getVersion() + " Bricks Validation Support for " + profile;
  }

  public void addBinary(byte[] theBinary, String theBinaryKey) {
    validateNotLocked();
    Validate.notNull(theBinary, "theBinaryKey must not be null");
    Validate.notNull(theBinary, "the" + theBinaryKey + " must not be null");
    myBinaries.put(theBinaryKey, theBinary);
  }

  private synchronized void validateNotLocked() {
    Validate.isTrue(!myLocked, "Can not add to validation support, module is locked");
  }

  /**
   * Add a new CodeSystem resource which will be available to the validator. Note that {@link
   * CodeSystem#getUrl()} the URL field in this resource must contain a value as this value will be
   * used as the logical URL.
   *
   * <p>Note that if the URL is a canonical FHIR URL (e.g. <a
   * href="http://hl7.org/StructureDefinition/Extension">Extension</a>), it will be stored in three
   * ways:
   *
   * <ul>
   *   <li>Extension
   *   <li>StructureDefinition/Extension
   *   <li>http://hl7.org/StructureDefinition/Extension
   * </ul>
   */
  public void addCodeSystem(IBaseResource theCodeSystem) {
    validateNotLocked();
    Set<String> urls = processResourceAndReturnUrls(theCodeSystem, CODE_SYSTEM);
    addToMap(theCodeSystem, myCodeSystems, myUrlToCodeSystems, urls);
  }

  private Set<String> processResourceAndReturnUrls(
      IBaseResource theResource, String theResourceName) {
    Validate.notNull(theResource, "the" + theResourceName + " must not be null");

    RuntimeResourceDefinition resourceDef = getFhirContext().getResourceDefinition(theResource);
    String actualResourceName = resourceDef.getName();
    Validate.isTrue(
        actualResourceName.equals(theResourceName),
        "the"
            + theResourceName
            + " must be a "
            + theResourceName
            + " - Got: "
            + actualResourceName);

    Optional<IBase> urlValue =
        resourceDef.getChildByName("url").getAccessor().getFirstValueOrNull(theResource);
    String url = urlValue.map(t -> (((IPrimitiveType<?>) t).getValueAsString())).orElse(null);

    Validate.notNull(url, "the" + theResourceName + ".getUrl() must not return null");
    Validate.notBlank(url, "the" + theResourceName + ".getUrl() must return a value");

    String urlWithoutVersion;
    int pipeIdx = url.indexOf('|');
    if (pipeIdx != -1) {
      urlWithoutVersion = url.substring(0, pipeIdx);
    } else {
      urlWithoutVersion = url;
    }

    val retVal = Sets.newHashSet(url, urlWithoutVersion);
    profile.getAllVersions().forEach(v -> retVal.add(urlWithoutVersion + "|" + v));

    // add the version extracted from the resource itself as well
    resourceDef
        .getChildByName("version")
        .getAccessor()
        .getFirstValueOrNull(theResource)
        .map(base -> ((IPrimitiveType<?>) base).getValueAsString())
        .ifPresent(v -> retVal.add(urlWithoutVersion + "|" + v));

    return retVal;
  }

  /**
   * Add a new StructureDefinition resource which will be available to the validator. Note that
   * {@link StructureDefinition#getUrl()} the URL field) in this resource must contain a value as
   * this value will be used as the logical URL.
   *
   * <p>Note that if the URL is a canonical FHIR URL (e.g. <a
   * href="http://hl7.org/StructureDefinition/Extension">...</a>), it will be stored in three ways:
   *
   * <ul>
   *   <li>Extension
   *   <li>StructureDefinition/Extension
   *   <li>http://hl7.org/StructureDefinition/Extension
   * </ul>
   */
  public void addStructureDefinition(IBaseResource theStructureDefinition) {
    validateNotLocked();
    Set<String> url = processResourceAndReturnUrls(theStructureDefinition, STRUCT_DEF);
    addToMap(theStructureDefinition, myStructureDefinitions, myUrlToStructureDefinitions, url);
  }

  public void addSearchParameter(IBaseResource theSearchParameter) {
    validateNotLocked();
    val url = processResourceAndReturnUrls(theSearchParameter, SEARCH_PARAMETER);
    addToMap(theSearchParameter, mySearchParameters, myUrlToSearchParameters, url);
  }

  private <T extends IBaseResource> void addToMap(
      T theResource, List<T> theList, Map<String, T> theMap, Collection<String> theUrls) {
    theList.add(theResource);
    for (String urls : theUrls) {
      if (isNotBlank(urls)) {
        theMap.put(urls, theResource);

        int lastSlashIdx = urls.lastIndexOf('/');
        if (lastSlashIdx != -1) {
          theMap.put(urls.substring(lastSlashIdx + 1), theResource);
          int previousSlashIdx = urls.lastIndexOf('/', lastSlashIdx - 1);
          if (previousSlashIdx != -1) {
            theMap.put(urls.substring(previousSlashIdx + 1), theResource);
          }
        }
      }
    }
  }

  /**
   * Add a new ValueSet resource which will be available to the validator. Note that
   * {@link ValueSet#getUrl() the URL field) in this resource must contain a value as this value
   * will be used as the logical URL.
   * <p>
   * Note that if the URL is a canonical FHIR URL (e.g.
   * <a href="http://hl7.org/StructureDefinition/Extension">Extension</a>), it will be stored in three ways:
   * <ul>
   * <li>Extension</li>
   * <li>StructureDefinition/Extension</li>
   * <li>http://hl7.org/StructureDefinition/Extension</li>
   * </ul>
   * </p>
   */
  public void addValueSet(IBaseResource theValueSet) {
    validateNotLocked();
    Set<String> urls = processResourceAndReturnUrls(theValueSet, VALUE_SET);
    addToMap(theValueSet, myValueSets, myUrlToValueSets, urls);
  }

  /**
   * @param theResource The resource. This method delegates to the type-specific methods (e.g.
   *     {@link #addCodeSystem(IBaseResource)}) and will do nothing if the resource type is not
   *     supported by this class.
   * @since 5.5.0
   */
  public void addResource(@Nonnull IBaseResource theResource) {
    validateNotLocked();
    Validate.notNull(theResource, "theResource must not be null");

    val resourceType = getFhirContext().getResourceType(theResource);
    switch (resourceType) {
      case SEARCH_PARAMETER:
        addSearchParameter(theResource);
        break;
      case STRUCT_DEF:
        addStructureDefinition(theResource);
        break;
      case CODE_SYSTEM:
        addCodeSystem(theResource);
        break;
      case VALUE_SET:
        addValueSet(theResource);
        break;
      default:
        break; // default case is very common; logging here would pollute the log
    }
  }

  @Override
  public List<IBaseResource> fetchAllConformanceResources() {
    ArrayList<IBaseResource> retVal = new ArrayList<>();
    retVal.addAll(myCodeSystems);
    retVal.addAll(myStructureDefinitions);
    retVal.addAll(myValueSets);
    return retVal;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T extends IBaseResource> List<T> fetchAllSearchParameters() {
    return (List<T>) Collections.unmodifiableList(mySearchParameters);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IBaseResource> List<T> fetchAllStructureDefinitions() {
    return (List<T>) Collections.unmodifiableList(myStructureDefinitions);
  }

  @Override
  public IBaseResource fetchCodeSystem(String theSystem) {
    val cs = myUrlToCodeSystems.get(theSystem);

    if (cs != null) logValidationEvent("Matched", CODE_SYSTEM, theSystem);

    return cs;
  }

  @Override
  public IBaseResource fetchValueSet(String theUri) {
    val vs = myUrlToValueSets.get(theUri);

    if (vs != null) logValidationEvent("Matched", VALUE_SET, theUri);

    return vs;
  }

  @Override
  public IBaseResource fetchStructureDefinition(String theUrl) {
    val sd = myUrlToStructureDefinitions.get(theUrl);

    if (sd != null) logValidationEvent("Matched", STRUCT_DEF, theUrl);

    return sd;
  }

  @Override
  public byte[] fetchBinary(String theBinaryKey) {
    val b = myBinaries.get(theBinaryKey);

    if (b != null) logValidationEvent("Matched", "Binary", theBinaryKey);

    return b;
  }

  private void logValidationEvent(String event, String type, String input) {
    log.debug("-> {} {} {} in {}  [{}]", event, type, input, profile, this);
  }

  /**
   * Validates that the given code exists and if possible returns a display name. This method is
   * called to check codes which are found in "example" binding fields (e.g. <code>Observation.code
   * </code>) in the default profile.
   *
   * @param theValidationSupportContext The validation support module will be passed in to this
   *     method. This is convenient in cases where the operation needs to make calls to other method
   *     in the support chain, so that they can be passed through the entire chain. Implementations
   *     of this interface may always safely ignore this parameter.
   * @param theOptions
   * @param theCodeSystem The code system, e.g. "<code>http://loinc.org</code>"
   * @param theCode The code, e.g. "<code>1234-5</code>"
   * @param theDisplay The display name, if it should also be validated
   * @param theValueSet The ValueSet to validate against. Must not be null, and must be a ValueSet
   *     resource.
   * @return Returns a validation result object, or <code>null</code> if this validation support
   *     module can not handle this kind of request
   */
  @Override
  public IValidationSupport.CodeValidationResult validateCodeInValueSet(
      ValidationSupportContext theValidationSupportContext,
      ConceptValidationOptions theOptions,
      String theCodeSystem,
      String theCode,
      String theDisplay,
      @NonNull IBaseResource theValueSet) {

    if (theValueSet instanceof ValueSet vs) {
      /*
        whenever a CodeSystem gets "ignored" e.g. by IgnoreCodeSystemSupport, we end up getting the code to validate
         embedded within the ValueSet's ConceptSet. So when talking about "ignoring a CodeSystem" we actually validate the code against itself here.
         This is a workaround to make "ignore" codes from a ValueSet which belong to CodeSystems ignored by e.g. IgnoreCodeSystemSupport.

         Step 1: try to validate code normally with pre-populated
         if that fails, try to find the code in the ValueSet's compose/include/concept list
      */
      val superResult =
          Optional.ofNullable(
              super.validateCodeInValueSet(
                  theValidationSupportContext,
                  theOptions,
                  theCodeSystem,
                  theCode,
                  theDisplay,
                  theValueSet));
      if (superResult.isPresent()) {
        logValidationEvent("CodeValidationSuper", vs.getUrl(), superResult.get().getCode());
        return superResult.get();
      }

      val includedConceptResult =
          vs.getCompose().getInclude().stream()
              //          .filter(it -> it.getSystem() != null &&
              // it.getSystem().equals(theCodeSystem)) // sometimes the system is null here
              .flatMap(it -> it.getConcept().stream())
              .filter(it -> theCode.equals(it.getCode()))
              .map(it -> new CodeValidationResult().setCode(theCode).setDisplay(theDisplay))
              .findFirst()
              .orElseGet(
                  () ->
                      this.validateCode(
                          theValidationSupportContext,
                          theOptions,
                          theCodeSystem,
                          theCode,
                          theDisplay,
                          vs.getUrl()));

      if (includedConceptResult != null)
        logValidationEvent(
            "CodeValidationIncludedConcept", vs.getUrl(), String.valueOf(includedConceptResult));
      else
        log.trace(
            "Code {} from system {} not found in ValueSet {} during validation in {}",
            theCode,
            theCodeSystem,
            vs.getUrl(),
            profile);

      return includedConceptResult;
    }

    // this should never happen because theValueSet MUST be always a Value
    return super.validateCodeInValueSet(
        theValidationSupportContext, theOptions, theCodeSystem, theCode, theDisplay, theValueSet);
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theSystem) {
    val isSupported = myUrlToCodeSystems.containsKey(theSystem);

    if (isSupported) logValidationEvent("Supported", CODE_SYSTEM, theSystem);

    return isSupported;
  }

  @Override
  public boolean isValueSetSupported(
      ValidationSupportContext theValidationSupportContext, String theValueSetUrl) {
    val isSupported = myUrlToValueSets.containsKey(theValueSetUrl);

    if (isSupported) logValidationEvent("Supported", VALUE_SET, theValueSetUrl);

    return isSupported;
  }

  @Override
  public synchronized void lock() {
    this.myLocked = true;
  }
}
