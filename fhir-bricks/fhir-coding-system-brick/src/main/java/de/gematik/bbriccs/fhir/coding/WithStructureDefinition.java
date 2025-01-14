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
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UrlType;

/**
 * This interface encapsulates the handling of StructureDefinitions for specific profiles and their
 * corresponding versions.
 *
 * @param <T> is the type of the corresponding version of the profile to which this
 *     StructureDefinition belongs
 * @see <a href="https://build.fhir.org/structuredefinition.html">Resource StructureDefinition</a>
 */
public interface WithStructureDefinition<T extends ProfileVersion> extends WithSystem {

  /**
   * Returns the canonical URL of the StructureDefinition with the given version appended to it.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop"
   * DeBasisProfilStructDef.GKV_WOP.getVersionedUrl(DeBasisProfilVersion.V1_4_0);
   *
   * // will create the following versioned URL
   * http://fhir.de/StructureDefinition/gkv/wop|1.4
   * }</pre>
   *
   * @param version the version to append to the canonical URL
   * @return the canonical URL of the StructureDefinition with the given version appended to it
   */
  default String getVersionedUrl(T version) {
    val v =
        (version.omitZeroPatch())
            ? VersionUtil.omitZeroPatch(version.getVersion())
            : version.getVersion();
    return format("{0}|{1}", this.getCanonicalUrl(), v);
  }

  /**
   * This method creates a CanonicalType object for this StructureDefinition without any version.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop"
   * val canonical = DeBasisProfilStructDef.GKV_WOP.asCanonicalType();
   * resource.getMeta().setProfile(List.of(canonical));
   *
   * // will create the following Meta object as the profile definition of a resource
   * <meta>
   *   <profile value="http://fhir.de/StructureDefinition/gkv/wop"/>
   * </meta>
   * }</pre>
   *
   * @return the canonical type for this StructureDefinition
   */
  default CanonicalType asCanonicalType() {
    return new CanonicalType(this.getCanonicalUrl());
  }

  /**
   * This method creates a CanonicalType object for this StructureDefinition with a specific
   * version.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop"
   * val canonical = DeBasisProfilStructDef.GKV_WOP.asCanonicalType(DeBasisProfilVersion.V1_4_0);
   * resource.getMeta().setProfile(List.of(canonical));
   *
   * // will create the following Meta object as the profile definition of a resource
   * <meta>
   *   <profile value="http://fhir.de/StructureDefinition/gkv/wop|1.4"/>
   * </meta>
   * }</pre>
   *
   * @param version to use for the canonical type
   * @return the canonical type for this StructureDefinition
   */
  default CanonicalType asCanonicalType(T version) {
    return new CanonicalType(this.getVersionedUrl(version));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * Code, encoded as a {@link BooleanType} as its value.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop""
   * DeBasisProfilStructDef.GKV_WOP.asBooleanExtension(true)
   *
   * // will create the following extension when serialized to XML:
   * <extension url="http://fhir.de/StructureDefinition/gkv/wop">
   *   <valueBoolean value="true"/>
   * </extension>
   *
   * }</pre>
   *
   * @param value the Code to be encoded as the value of the Extension as a {@link BooleanType}
   * @return an Extension object of this StructureDefinition containing the encoded Code as a {@link
   *     BooleanType}.
   * @see <a href="https://build.fhir.org/datatypes.html#boolean">FHIR DataTypes Boolean</a>
   */
  default Extension asBooleanExtension(boolean value) {
    return asExtension(new BooleanType(value));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * Code, encoded as a {@link StringType} as its value.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop""
   * DeBasisProfilStructDef.GKV_WOP.asStringExtension("Berlin")
   *
   * // will create the following extension when serialized to XML:
   * <extension url="http://fhir.de/StructureDefinition/gkv/wop">
   *   <valueString value="Berlin"/>
   * </extension>
   *
   * }</pre>
   *
   * @param value the Code to be encoded as the value of the Extension as a {@link StringType}
   * @return an Extension object of this StructureDefinition containing the encoded Code as a {@link
   *     StringType}.
   * @see <a href="https://build.fhir.org/datatypes.html#string">FHIR DataTypes String</a>
   */
  default Extension asStringExtension(String value) {
    return asExtension(new StringType(value));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * Code, encoded as a {@link CodeType} as its value.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where GKV_WOP is an instance of WithStructureDefinition and has a canonical URL of
   * // "http://fhir.de/StructureDefinition/gkv/wop""
   * DeBasisProfilStructDef.GKV_WOP.asCodeExtension("BW")
   *
   * // will create the following extension when serialized to XML:
   * <extension url="http://fhir.de/StructureDefinition/gkv/wop">
   *   <valueCode value="BW"/>
   * </extension>
   *
   * }</pre>
   *
   * @param code the Code to be encoded as the value of the Extension as a {@link CodeType}
   * @return an Extension object of this StructureDefinition containing the encoded Code as a {@link
   *     CodeType}.
   * @see <a href="https://build.fhir.org/datatypes.html#code">FHIR DataTypes Code</a>
   */
  default Extension asCodeExtension(String code) {
    return asExtension(new CodeType(code));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * URL, encoded as a {@link UrlType} as its value.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where DEEP_LINK is an instance of WithStructureDefinition and has a canonical URL of
   * // "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink"
   * ErpWorkflowStructDef.DEEP_LINK.asUrlExtension("https://www.my.diga.app.com/123123");
   *
   * // will create the following extension when serialized to XML:
   * <extension url="https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink">
   *   <valueUrl value="https://www.my.diga.app.com/123123"/>
   * </extension>
   *
   * }</pre>
   *
   * @param url the URL to be encoded as the value of the Extension
   * @return an Extension object of this StructureDefinition containing the encoded URL as a {@link
   *     UrlType}.
   * @see <a href="https://build.fhir.org/datatypes.html#url">FHIR DataTypes URL</a>
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc1738">RFC 1738</a>
   */
  default Extension asUrlExtension(String url) {
    return asExtension(new UrlType(url));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * URI, encoded as a {@link UriType} as its value. The created Extension will have this
   * StructureDefinition's System as its system
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // where DEEP_LINK is an instance of WithStructureDefinition and has a canonical URL of
   * // "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink"
   * ErpWorkflowStructDef.DEEP_LINK.asUrlExtension("https://www.my.diga.app.com/123123");
   *
   * // will create the following extension when serialized to XML:
   * <extension url="https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink">
   *   <valueUri value="https://www.my.diga.app.com/123123"/>
   * </extension>
   *
   * }</pre>
   *
   * @param uri the URI to be encoded as the value of the Extension
   * @return an Extension object of this StructureDefinition containing the encoded URI as a {@link
   *     UriType}.
   * @see <a href="https://build.fhir.org/datatypes.html#uri">FHIR DataTypes URI</a>
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>
   */
  default Extension asUriExtension(String uri) {
    return asExtension(new UriType(uri));
  }

  /**
   * This method creates an Extension object for this StructureDefinition that contains the provided
   * value, encoded as any {@link IBaseDatatype}
   *
   * @param type the value to be encoded as the value of the Extension
   * @return an Extension object of this StructureDefinition containing the provided value
   * @see <a href="https://build.fhir.org/datatypes.html">FHIR DataTypes</a>
   */
  default Extension asExtension(IBaseDatatype type) {
    return new Extension(this.getCanonicalUrl(), type);
  }

  /**
   * This method creates "an empty" Extension object for this StructureDefinition without any value.
   *
   * @return an empty Extension with the given profile structure definition
   */
  default Extension asExtension() {
    return new Extension(this.getCanonicalUrl());
  }
}
