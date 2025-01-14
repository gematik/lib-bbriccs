/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.bbriccs.fhir.validation.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import de.gematik.bbriccs.fhir.conf.ProfileDto;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

@Slf4j
public class ProfileValidationSupport implements IValidationSupport {

  private final FhirContext ctx;
  private final ProfileDto profile;

  private final Map<String, StructureDefinition> structureDefinitions;
  private final Map<String, NamingSystem> namingSystems; // NOSONAR this might be required
  private final Map<String, CodeSystem> codeSystems;
  private final Map<String, ValueSet> valueSets;

  @SneakyThrows
  public ProfileValidationSupport(
      FhirContext ctx,
      ProfileDto profile,
      Map<String, StructureDefinition> structureDefinitions,
      Map<String, NamingSystem> namingSystems,
      Map<String, CodeSystem> codeSystems,
      Map<String, ValueSet> valueSets) {

    log.trace("Instantiate ValidationSupport for {}", profile);
    this.ctx = ctx;
    this.profile = profile;

    if (this.profile.getCanonicalClaims().isEmpty()) {
      log.warn(
          "No canonical claims provided for {}-{}",
          this.profile.getName(),
          this.profile.getVersion());
    }

    if (this.profile.getCompatibleVersions() == null) {
      this.profile.setCompatibleVersions(List.of());
    }

    this.structureDefinitions = structureDefinitions;
    this.namingSystems = namingSystems;
    this.codeSystems = codeSystems;
    this.valueSets = valueSets;
  }

  @Override
  public FhirContext getFhirContext() {
    return this.ctx;
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theSystem) {
    return this.codeSystems.containsKey(theSystem);
  }

  @Override
  public boolean isValueSetSupported(
      ValidationSupportContext theValidationSupportContext, String theValueSetUrl) {
    return this.valueSets.containsKey(theValueSetUrl);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IBaseResource> List<T> fetchAllStructureDefinitions() {
    val retVal = new ArrayList<>(this.structureDefinitions.values());
    return (List<T>) Collections.unmodifiableList(retVal);
  }

  @Override
  public IBaseResource fetchCodeSystem(String theSystem) {
    return fetchBaseResource(theSystem, this.codeSystems);
  }

  @Override
  public IBaseResource fetchStructureDefinition(String theUrl) {
    return fetchBaseResource(theUrl, this.structureDefinitions);
  }

  @Override
  public IBaseResource fetchValueSet(String theValueSetUrl) {
    return fetchBaseResource(theValueSetUrl, this.valueSets);
  }

  private <R extends IBaseResource> IBaseResource fetchBaseResource(
      String resourceUrl, Map<String, R> map) {
    if (!matchesClaim(resourceUrl)) return null;

    val tokens = resourceUrl.split("\\|");
    val url = tokens[0];
    val version = tokens.length > 1 ? tokens[1] : null;

    R resource = null;

    boolean exactVersionMatch = false;
    if (version == null) {
      // no version given, try to find simply by URL
      // this results in a greedy fetch between competing profile versions
      resource = map.get(url);
    } else if (matchesVersion(version)) {
      // profile version does match exactly
      resource = map.get(url);
      exactVersionMatch = true;
    }

    val matched = resource != null;
    if (matched) {
      val em = exactVersionMatch ? "with exact version" : "without version";
      log.trace(
          "Matched {} in profile {}:{} {}",
          resourceUrl,
          this.profile.getName(),
          this.profile.getVersion(),
          em);
    }

    return resource;
  }

  private boolean matchesClaim(String theUrl) {
    if (this.profile.getCanonicalClaims().isEmpty()) {
      return true;
    }

    return this.profile.getCanonicalClaims().stream().anyMatch(theUrl::startsWith);
  }

  private boolean matchesVersion(String version) {
    val myVersion = this.profile.getVersion();
    return this.profile.getAllVersions().contains(version)
        || VersionUtil.areEqual(myVersion, version);
  }
}
