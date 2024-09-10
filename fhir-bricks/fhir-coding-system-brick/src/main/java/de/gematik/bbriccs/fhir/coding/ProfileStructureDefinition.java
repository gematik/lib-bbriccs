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

package de.gematik.bbriccs.fhir.coding;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;

public interface ProfileStructureDefinition<T extends ProfileVersion> extends WithSystem {

  default String getVersionedUrl(T version) {
    val v =
        (version.omitZeroPatch())
            ? VersionUtil.omitZeroPatch(version.getVersion())
            : version.getVersion();
    return format("{0}|{1}", this.getCanonicalUrl(), v);
  }

  default CanonicalType asCanonicalType() {
    return new CanonicalType(this.getCanonicalUrl());
  }

  /**
   * @param version to use for the canonical type
   * @return the canonical type
   */
  default CanonicalType asCanonicalType(T version) {
    return new CanonicalType(this.getVersionedUrl(version));
  }

  default Extension asBooleanExtension(boolean value) {
    return new Extension(this.getCanonicalUrl(), new BooleanType(value));
  }
}
