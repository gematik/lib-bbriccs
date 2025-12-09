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

package de.gematik.bbriccs.crypto.certificate;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum contains the OIDs for the professions used in the gematik ecosystem. The OIDs are used
 * to identify the profession of a healthcare professional in certificates.
 *
 * @see <a
 *     href="https://gemspec.gematik.de/downloads/gemSpec/gemSpec_OID/gemSpec_V3.12.3_Aend.html">gemSpec
 *     OID</a>
 *     <p>TODO: duplicates with de.gematik.bbriccs.fhir.vzd.valueSet.ProfessionOID
 */
@AllArgsConstructor
@Getter
public enum ProfessionOid implements Oid {
  ARZT("1.2.276.0.76.4.30", "Ärztin/Arzt"),
  ZAHNARZT("1.2.276.0.76.31", "Zahnärztin/Zahnarzt"),
  APOTHEKER("1.2.276.0.76.4.32", "Apotheker/-in"),
  APOTHEKER_ASSISTENT("1.2.276.0.76.4.33", "Apothekerassistent/-in"),
  PHARMAZIEINGENIEUR("1.2.276.0.76.4.34", "Pharmazieingenieur/-in"),
  PHARMA_TECH_ASSISTENT("1.2.276.0.76.4.35", "pharmazeutisch-technische/-r Assistent/-in"),
  PHARMA_KAUFMANN("1.2.276.0.76.4.36", "pharmazeutisch-kaufmännische/-r Angestellte"),
  APOTHEKEN_HELFER("1.2.276.0.76.4.37", "Apothekenhelfer/-in"),
  APOTHEKEN_ASSISTENT("1.2.276.0.76.4.38", "Apothekenassistent/-in"),
  PHARMA_ASSISTENT("1.2.276.0.76.4.39", "Pharmazeutische/-r Assistent/-in"),

  PSYCHOTHERAPEUT("1.2.276.0.76.4.45", "Psychotherapeut/-in"),
  PS_PSYCHOTHERAPEUT("1.2.276.0.76.4.46", "Psychologische/-r Psychotherapeut/-in"),
  KUJ_PSYCHOTHERAPEUT("1.2.276.0.76.4.47", "Kinder- und Jugendlichenpsychotherapeut/-in"),
  RETTUNGSASSISTENT("1.2.276.0.76.4.48", "Rettungsassistent/-in"),

  // there are more OIDs here to cover

  PRAXIS_ARZT("1.2.276.0.76.4.50", "Betriebsstätte Arzt"),
  ZAHNARZTPRAXIS("1.2.276.0.76.4.51", "Zahnarztpraxis"),
  PRAXIS_PSYCHOTHERAPEUT("1.2.276.0.76.4.52", "Betriebsstätte Psychotherapeut"),
  KRANKENHAUS("1.2.276.0.76.4.53", "Krankenhaus"),
  OEFFENTLICHE_APOTHEKE("1.2.276.0.76.4.54", "Öffentliche Apotheke"),
  KRANKENHAUSAPOTHEKE("1.2.276.0.76.4.55", "Krankenhausapotheke"),
  BUNDESWEHRAPOTHEKE("1.2.276.0.76.4.56", "Bundeswehrapotheke"),
  MOBILE_RETTUNGSDIENST("1.2.276.0.76.4.57", "Betriebsstätte Mobile Einrichtung Rettungsdienst"),
  BS_GEMATIK("1.2.276.0.76.4.58", "Betriebsstätte gematik"),
  KOSTENTRAEGER("1.2.276.0.76.4.59", "Betriebsstätte Kostenträger"),

  ADV_KTR("1.2.276.0.76.4.190", "AdV-Umgebung bei Kostenträger"),

  DIGA("1.2.276.0.76.4.282", "DiGA-Hersteller und -Anbieter"),
  BS_WEITERE_KOSTENTRAEGER(
      "1.2.276.0.76.4.284", "Betriebsstätte Weitere Kostenträger im Gesundheitswesen"),
  ORG_GESUNDHEITSVERSORGUNG(
      "1.2.276.0.76.4.285", "Weitere Organisationen der Gesundheitsversorgung"),
  KIM_ANBIETER("1.2.276.0.76.4.286", "KIM-Hersteller und -Anbieter"),
  NCPEH_FACHDIENST("1.2.276.0.76.4.292", "NCPeH Fachdienst"),

  UNKNOWN("n/a", "Unknown Profession OID"),
  ;
  private final String value;
  private final String display;

  public static Optional<ProfessionOid> fromString(String oid) {
    return Oid.fromString(ProfessionOid.class, oid);
  }

  public static ProfessionOid fromStringOrThrow(String oid) {
    return Oid.fromStringOrThrow(ProfessionOid.class, oid);
  }
}
