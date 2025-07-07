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

package de.gematik.bbriccs.fhir.vzd.util;

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.bbriccs.fhir.vzd.r4.VzdHealthServiceTriple;
import de.gematik.bbriccs.fhir.vzd.r4.VzdHealthcareService;
import de.gematik.bbriccs.fhir.vzd.r4.VzdLocation;
import de.gematik.bbriccs.fhir.vzd.r4.VzdOrganization;
import de.gematik.bbriccs.fhir.vzd.valueset.PharmacyHealthcareSpeciality;
import java.io.PrintStream;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Enumeration;

@Slf4j
public class VzdSummaryPrinter {

  private final PrintStream out;

  @SuppressWarnings("java:S106")
  public VzdSummaryPrinter() {
    this(System.out);
  }

  public VzdSummaryPrinter(PrintStream out) {
    this.out = out;
  }

  public static String getSummary(VzdOrganization organization) {
    return format(
        "{0}: {1} ({2})",
        organization.getProfessionOID().getDisplay(),
        organization.getName(),
        organization.getTelematikId().getValue());
  }

  public static String getSummary(VzdLocation location) {
    return format("{0} ({1})", location.getAddressString(), location.getLocationString());
  }

  public static String getSummary(VzdHealthcareService service) {
    val sb = new StringBuilder(format("Service {0}", service.getOrigin().getDisplay()));
    service.getTelematikID().ifPresent(tid -> sb.append(format(" (TelematikID: {0})", tid)));
    sb.append("\n");
    sb.append(getPharmacySpecialitySummary(service));
    sb.append("\n");
    sb.append(getAvailabilitySummary(service));
    return sb.toString();
  }

  public static String getPharmacySpecialitySummary(VzdHealthcareService service) {
    var ps =
        service.getPharmacySpecialities().stream()
            .map(PharmacyHealthcareSpeciality::getDisplay)
            .collect(Collectors.joining(","));
    if (Strings.isNullOrEmpty(ps)) {
      ps = "n/a";
    }
    return format("PharmacySpecialities: {0}", ps);
  }

  public static String getAvailabilitySummary(VzdHealthcareService service) {
    val availability =
        service.getAvailableTime().stream()
            .map(
                it -> {
                  val daysOfWeek =
                      it.getDaysOfWeek().stream()
                          .map(Enumeration::getCode)
                          .map(String::toUpperCase)
                          .collect(Collectors.joining(","));
                  val start = it.getAvailableStartTime();
                  val end = it.getAvailableEndTime();
                  log.info("ALL DAY: {}", it.getAllDay());
                  return format("\t{0} / {1} - {2}", daysOfWeek, start, end);
                })
            .collect(Collectors.joining("\n"));

    if (Strings.isNullOrEmpty(availability)) {
      return "Availability: n/a";
    } else {
      return format("Availability:\n{0}", availability);
    }
  }

  public void printSummary(VzdHealthServiceTriple triple) {
    val location = triple.location();
    val org = triple.organization();
    val service = triple.service();

    this.out.println(getSummary(org));
    this.out.println(getSummary(location));
    this.out.println(getSummary(service));
    this.out.println("-------------------------------");
  }

  public void printSummary(VzdHealthcareService service) {
    this.out.println(getSummary(service));
  }

  public void printSummary(VzdOrganization organization) {
    this.out.println(getSummary(organization));
  }

  public void printSummary(VzdLocation organization) {
    this.out.println(getSummary(organization));
  }
}
