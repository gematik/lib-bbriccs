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

package de.gematik.bbriccs.fhir.coding;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * This base interface encapsulates the handling elements which are codeable with the FHIR coding
 * system.
 *
 * <p>An implementation of the {@link WithSystem} interface can be one of
 *
 * <ul>
 *   <li>{@link WithStructureDefinition} representing a specific <a
 *       href="https://build.fhir.org/structuredefinition.html">StructureDefinition</a>
 *   <li>{@link WithNamingSystem} representing a specific <a
 *       href="https://build.fhir.org/namingsystem.html">NamingSystem</a>
 *   <li>{@link WithCodeSystem} representing a specific <a
 *       href="https://build.fhir.org/codesystem.html">CodeSystem</a>
 *   <li>{@link FromValueSet} representing a specific <a
 *       href="https://build.fhir.org/valueset.html">ValueSet</a>
 * </ul>
 *
 * <b>Note:</b> rather than implementing the {@link WithSystem} interface directly, you should
 * always implement one of the above-mentioned interfaces.
 */
public interface WithSystem {

  /**
   * Get the canonical URL(1) of the system.
   *
   * <p><b>Note(1):</b> while the method is called {@link WithSystem#getCanonicalUrl()}
   * resources/elements/values can be sometimes annotated with a system url which is not a URL at
   * all. In fact, in most cases it should not be necessary to treat this value as a {@link URL}
   *
   * @return the canonical URL of the system
   */
  String getCanonicalUrl();

  default CodeableConcept asCodeableConcept(String code) {
    val coding = this.asCoding(code);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  default Coding asCoding(String code) {
    val coding = new Coding();
    coding.setSystem(this.getCanonicalUrl()).setCode(code);
    return coding;
  }

  /**
   * Find the first resource in the given bundle entries which matches the system.
   *
   * <p>Example usage: Image you have a {@link Bundle} with a list of resources, and you want to
   * find the first {@link Patient} resource which is profiled by <a
   * href="https://fhir.kbv.de/">KBV</a> and has a definition {@code KbvItaForStructDef.PATIENT}
   * implementing {@link WithStructureDefinition} and carrying the canonical URL system.
   *
   * <pre>{@code
   * Bundle bundle = ... // which might contain the KBV-Patient Resource
   * Optional<Patient> patient = KbvItaForStructDef.PATIENT
   *        .findFirstResource(bundle.getEntry())
   *        .map(Patient.class::cast);
   * }</pre>
   *
   * @param bundleEntries is the list of bundle entries to search in
   * @return the first resource which matches the system
   */
  default Optional<Resource> findFirstResource(List<BundleEntryComponent> bundleEntries) {
    return bundleEntries.stream()
        .filter(this::matches)
        .map(BundleEntryComponent::getResource)
        .findFirst();
  }

  /**
   * Check if the given {@link BundleEntryComponent} matches this system.
   *
   * <p>Example usage: Image you have a custom "KbvBundle" class which extends from {@link Bundle}
   * and you want to provide a convenient method to extract any {@link Coverage} resource which
   * matches the <a href="https://fhir.kbv.de/">KBV</a> StructureDefinition defined in {@code
   * KbvItaForStructDef.COVERAGE} wich itself is implementing {@link WithStructureDefinition}
   *
   * <pre>{@code
   * public class KbvBundle extends Bundle {
   *   public List<Coverage> getCoverages() {
   *     return this.getEntry().stream()
   *         .filter(KbvItaForStructDef.COVERAGE::matches)
   *         .map(BundleEntryComponent::getResource)
   *         .map(Coverage.class::cast)
   *         .toList();
   *   }
   * }
   * }</pre>
   *
   * @param bundleEntry to be checked if it matches the system
   * @return true if the bundle entry matches the system
   */
  default boolean matches(BundleEntryComponent bundleEntry) {
    return this.matches(bundleEntry.getResource());
  }

  default boolean matches(Resource resource) {
    return this.matches(resource.getMeta());
  }

  default boolean matches(Meta meta) {
    return this.matches(meta.getProfile().toArray(CanonicalType[]::new));
  }

  default boolean matches(Identifier... identifier) {
    return Arrays.stream(identifier).anyMatch(id -> this.matches(id.getSystem()));
  }

  default boolean matches(Coding... coding) {
    return Arrays.stream(coding).anyMatch(c -> this.matches(c.getSystem()));
  }

  default boolean matches(CodeableConcept... codeableConcepts) {
    return Arrays.stream(codeableConcepts)
        .flatMap(cc -> cc.getCoding().stream())
        .anyMatch(this::matches);
  }

  default boolean matches(Extension... extension) {
    return Arrays.stream(extension).anyMatch(e -> this.matches(e.getUrl()));
  }

  default boolean matches(CanonicalType... canonicalType) {
    return Arrays.stream(canonicalType).anyMatch(ct -> this.matches(ct.asStringValue()));
  }

  default boolean matches(WithSystem... other) {
    return Arrays.stream(other).anyMatch(ws -> this.matches(ws.getCanonicalUrl()));
  }

  default boolean matchesReferenceIdentifier(Reference... reference) {
    return Arrays.stream(reference).anyMatch(r -> this.matches(r.getIdentifier()));
  }

  default boolean matches(String url) {
    if (url == null) {
      return false;
    }
    val withoutVersion = url.split("\\|")[0];
    return this.getCanonicalUrl().equals(withoutVersion);
  }

  static AnyOfSystemMatcher anyOf(WithSystem... systems) {
    return new AnyOfSystemMatcher(Arrays.asList(systems));
  }

  class AnyOfSystemMatcher {

    private final List<WithSystem> systems;

    public AnyOfSystemMatcher(List<WithSystem> systems) {
      this.systems = systems;
    }

    public boolean matches(BundleEntryComponent bundleEntry) {
      return matches(bundleEntry.getResource());
    }

    public boolean matches(Resource resource) {
      return matches(resource.getMeta());
    }

    public boolean matches(Meta meta) {
      return systems.stream().anyMatch(system -> system.matches(meta));
    }

    public boolean matchesReferenceIdentifier(Reference... reference) {
      return systems.stream()
          .anyMatch(
              system ->
                  Arrays.stream(reference).map(Reference::getIdentifier).anyMatch(system::matches));
    }

    public boolean matches(Identifier... identifier) {
      return matchesAnyIdentifier(Arrays.asList(identifier));
    }

    public boolean matches(Coding... coding) {
      return systems.stream().anyMatch(system -> Arrays.stream(coding).anyMatch(system::matches));
    }

    public boolean matches(CodeableConcept... codeableConcepts) {
      return systems.stream()
          .anyMatch(system -> Arrays.stream(codeableConcepts).anyMatch(system::matches));
    }

    public boolean matches(Extension... extension) {
      return matchesAnyExtension(Arrays.asList(extension));
    }

    public boolean matches(CanonicalType... canonicalType) {
      return systems.stream()
          .anyMatch(system -> Arrays.stream(canonicalType).anyMatch(system::matches));
    }

    public boolean matchesAnyIdentifier(List<Identifier> identifiers) {
      return systems.stream().anyMatch(system -> identifiers.stream().anyMatch(system::matches));
    }

    public boolean matchesAnyCoding(List<Coding> codings) {
      return systems.stream().anyMatch(system -> codings.stream().anyMatch(system::matches));
    }

    public boolean matchesAnyExtension(List<Extension> extensions) {
      return systems.stream().anyMatch(system -> extensions.stream().anyMatch(system::matches));
    }
  }
}
