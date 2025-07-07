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

package de.gematik.bbriccs.rest.vzd.search;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.fhir.vzd.valueset.ProfessionOID;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import lombok.val;
import org.junit.jupiter.api.Test;

class VzdSearchTest {

  @Test
  void shouldCreateSearchRequestForAnyPharmacy() {
    val reqBuilder = assertDoesNotThrow(VzdSearch::forAnyPharmacy);
    val req = assertDoesNotThrow(reqBuilder::build);

    assertNotNull(req.getRequestBody());
    assertEquals(HttpRequestMethod.GET, req.getMethod());
    assertEquals("/HealthcareService", req.getFhirResource());
    assertTrue(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_OEFFENTLICH.getCode()));
    assertTrue(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_KRANKENHAUS.getCode()));
    assertTrue(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_BW.getCode()));

    // default query parameters
    assertTrue(req.getRequestLocator().contains("organization.active=true"));
    assertTrue(req.getRequestLocator().contains("_include=*"));
  }

  @Test
  void shouldCreateSearchRequestForPublicPharmacyOnly() {
    val req = VzdSearch.forPublicPharmacies().build();

    assertTrue(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_OEFFENTLICH.getCode()));
    assertFalse(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_KRANKENHAUS.getCode()));
    assertFalse(req.getRequestLocator().contains(ProfessionOID.APOTHEKE_BW.getCode()));
  }

  @Test
  void shouldCreateSearchRequestWithMultipleQueryParameters() {
    val telematikId = TelematikID.from("3-2222-ARV1225146600043068");
    val iknr = IKNR.asSidIknr("104940005");
    val latitude = 52.5246997;
    val longitude = 13.3904614;
    val defaultRadius = 10;
    val expectedGeo = format("location.near=52.5246997%7C13.3904614%7C{0}%7Ckm", defaultRadius);

    val req =
        VzdSearch.forAnyProfession()
            .withTelematikId(telematikId)
            .withIknr(iknr)
            .withName("Barmer")
            .inCity("Berlin")
            .nearBy(latitude, longitude)
            .withMaxCount(10)
            .build();

    assertTrue(req.getRequestLocator().contains(expectedGeo));
    assertTrue(req.getRequestLocator().contains(telematikId.getValue()));
    assertTrue(req.getRequestLocator().contains(iknr.getValue()));
    assertTrue(req.getRequestLocator().contains("Berlin"));
    assertTrue(req.getRequestLocator().contains("Barmer"));
  }
}
