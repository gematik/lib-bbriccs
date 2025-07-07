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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.fhir.vzd.r4;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class VzdHealthcareServiceSearchSet extends Bundle {

  private static VzdOrganization findOrganizationByRef(
      List<VzdOrganization> organizations, Reference orgRef) {
    return findResourceByRef(organizations, orgRef);
  }

  private static VzdLocation findLocationById(List<VzdLocation> locations, Reference locationRef) {
    return findResourceByRef(locations, locationRef);
  }

  private static <R extends Resource> R findResourceByRef(List<R> resources, Reference ref) {
    return resources.stream()
        .filter(r -> ref.getReference().contains(r.getIdElement().getIdPart()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    VzdHealthcareServiceSearchSet.class,
                    format("No resource found by reference {0}", ref.getReference())));
  }

  public List<VzdHealthServiceTriple> getHealthcareServiceTriples() {
    val hcs = this.getHealthcareServices();
    val locations = this.getLocations();
    val organizations = this.getOrganizations();

    return hcs.stream()
        .map(
            it -> {
              val locRef = it.getLocation().get(0); // take only the first for now!
              val orgRef = it.getProvidedBy();
              val loc = findLocationById(locations, locRef);
              val org = findOrganizationByRef(organizations, orgRef);
              return new VzdHealthServiceTriple(it, org, loc);
            })
        .toList();
  }

  public List<VzdHealthcareService> getHealthcareServices() {
    return this.getEntryResources(ResourceType.HealthcareService);
  }

  public List<VzdOrganization> getOrganizations() {
    return this.getEntryResources(ResourceType.Organization);
  }

  public List<VzdLocation> getLocations() {
    return this.getEntryResources(ResourceType.Location);
  }

  @SuppressWarnings("unchecked")
  private <T extends Resource> List<T> getEntryResources(ResourceType resourceType) {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(r -> resourceType.equals(r.getResourceType()))
        .map(r -> (T) r)
        .toList();
  }
}
