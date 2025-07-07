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

package de.gematik.bbriccs.fhir.validation;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import lombok.*;
import org.hl7.fhir.r4.model.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProfileExtractorTest {

  private boolean doesAcceptInvalidEnums;
  private static ProfileExtractor profileExtractor;

  @BeforeAll
  static void initExtractor() {
    profileExtractor = new ProfileExtractor();
  }

  @BeforeEach
  void setup() {
    this.doesAcceptInvalidEnums = Configuration.isAcceptInvalidEnums();
    // will trigger a special edge-case for test coverage
    Configuration.setAcceptInvalidEnums(false);
  }

  @AfterEach
  void teardown() {
    Configuration.setAcceptInvalidEnums(doesAcceptInvalidEnums);
  }

  @ParameterizedTest(name = "Should not fail on missing profile/meta tags in searchsets on {0}")
  @ValueSource(
      strings = {
        "examples/fhir/valid/erp/erx/1.2.0/chargeitembundle/a05a235a-a214-11ed-a8fc-0242ac120002.xml",
        "examples/fhir/valid/erp/erx/1.2.0/chargeitembundle/abc825bc-bc30-45f8-b109-1b343fff5c45.json",
        "examples/fhir/invalid/no_profiles_bundle.xml",
        "examples/fhir/invalid/no_metas_bundle.xml",
        "examples/fhir/invalid/no_profile_value_bundle.xml",
        "examples/fhir/invalid/invalid_bundle_type.xml",
        "examples/invalid/sample_01.xml"
      })
  void shouldNotFailOnNoProfilesInSearchsets(String filePath) {
    val content = ResourceLoader.readFileFromResource(filePath);
    val p = profileExtractor.extractProfile(content);
    assertTrue(p.isEmpty());
  }

  @ParameterizedTest(name = "Should not fail on missing profile/meta tags in collections on {0}")
  @ValueSource(
      strings = {
        "examples/fhir/edgecases/empty_root_profile_collection.xml",
        "examples/fhir/edgecases/missing_root_profile_collection.xml"
      })
  void shouldFindProfileFromCollectionChildren(String filePath) {
    val content = ResourceLoader.readFileFromResource(filePath);
    val p = profileExtractor.extractProfile(content);
    assertTrue(p.isPresent());
  }

  @Test
  void shouldExtractXmlProfiles() {
    val kbvBundleResources =
        ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.0.2/bundle");
    val expectedProfile = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2";

    kbvBundleResources.forEach(
        file -> {
          val content = ResourceLoader.readString(file);

          val profile = profileExtractor.extractProfile(content);
          assertEquals(expectedProfile, profile.orElseThrow());
        });
  }

  @Test
  void shouldExtractJsonProfiles() {
    val filePath =
        "examples/fhir/valid/erp/erx/1.2.0/auditevent/9361863d-fec0-4ba9-8776-7905cf1b0cfa.json";

    val content = ResourceLoader.readFileFromResource(filePath);
    val profile = profileExtractor.extractProfile(content);

    val expectedProfile =
        "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_AuditEvent|1.2";
    assertTrue(profile.isPresent());
    assertEquals(expectedProfile, profile.orElseThrow());
  }

  @ParameterizedTest
  @ValueSource(strings = {"<xml>", "alternative_json", ""})
  void shouldNotCrashOnCheckingSearchBundles(String content) {
    assertFalse(profileExtractor.isUnprofiledSearchSet(content));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "examples/fhir/valid/erp/erx/1.2.0/chargeitembundle/abc825bc-bc30-45f8-b109-1b343fff5c45.json",
        "examples/fhir/valid/erp/erx/1.2.0/chargeitembundle/ea33a992-a214-11ed-a8fc-0242ac120002.xml"
      })
  void shouldNonProfiledDetectSearchSets(String resourcePath) {
    val content = ResourceLoader.readFileFromResource(resourcePath);
    assertTrue(profileExtractor.isUnprofiledSearchSet(content));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "examples/fhir/valid/erp/dav/1.2/ad80703d-8c62-44a3-b12b-2ea66eda0aa2.xml",
        "examples/fhir/valid/erp/erx/1.2.0/receiptbundle/dffbfd6a-5712-4798-bdc8-07201eb77ab8.json",
        "examples/fhir/valid/erp/erx/1.2.0/receiptbundle/dffbfd6a-5712-4798-bdc8-07201eb77ab8.xml",
        "examples/fhir/valid/erp/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml",
        "examples/fhir/valid/erp/erx/1.2.0/auditevent/9361863d-fec0-4ba9-8776-7905cf1b0cfa.xml",
        "examples/fhir/valid/erp/erx/1.2.0/auditevent/9361863d-fec0-4ba9-8776-7905cf1b0cfa.json",
      })
  void shouldPassOtherResources(String resourcePath) {
    val content = ResourceLoader.readFileFromResource(resourcePath);
    assertFalse(profileExtractor.isUnprofiledSearchSet(content));
  }
}
