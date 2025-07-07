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

package de.gematik.bbriccs.fhir.vzd.valueset;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.vzd.VzdCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;

/**
 * The enumeration names are <b>NOT</b> translated to english to avoid confusion and translation
 * errors.
 *
 * <p>However, to avoid german umlaute and long recurrent terms some of them are abbreviated as
 * follows
 *
 * <ul>
 *   <li>BS: Betriebsstätte
 *   <li>BW: Bundeswehr
 *   <li>KTR: Kostenträger
 *   <li>LEO: Leistungserbringerorganisation
 *   <li>BV: Bundesvereinigung
 * </ul>
 *
 * <p>TODO: duplicates with {@link de.gematik.bbriccs.crypto.certificate.GemOid}
 */
@Getter
@RequiredArgsConstructor
@Deprecated(since = "0.5.0", forRemoval = true)
public enum ProfessionOID implements FromValueSet {
  BS_ARZT("1.2.276.0.76.4.50", "Betriebsstätte Arzt"),
  PRAXIS_ZAHNARZT("1.2.276.0.76.4.51", "Zahnarztpraxis"),
  BS_PSYCHOTHERAPEUT("1.2.276.0.76.4.52", "Betriebsstätte Psychotherapeut"),
  KRANKENHAUS("1.2.276.0.76.4.53", "Krankenhaus"),
  APOTHEKE_OEFFENTLICH("1.2.276.0.76.4.54", "Öffentliche Apotheke"),
  APOTHEKE_KRANKENHAUS("1.2.276.0.76.4.55", "Krankenhausapotheke"),
  APOTHEKE_BW("1.2.276.0.76.4.56", "Bundeswehrapotheke"),
  BS_MOBILE_EINRICHTUNG_RETTUNGSDIENST(
      "1.2.276.0.76.4.57", "Betriebsstätte Mobile Einrichtung Rettungsdienst"),
  BS_GEMATIK("1.2.276.0.76.4.58", "Betriebsstätte gematik"),
  BS_KTR("1.2.276.0.76.4.59", "Betriebsstätte Kostenträger"),
  BS_LEO_VERTRAGSZAHNAERZTE(
      "1.2.276.0.76.4.187", "Betriebsstätte Leistungserbringerorganisation Vertragszahnärzte"),
  ADV_UMGEBUNG_KTR("1.2.276.0.76.4.190", "AdV-Umgebung bei Kostenträger"),
  BS_LEO_KASSENAERZTLICHE_VEREINIGUNG(
      "1.2.276.0.76.4.210",
      "Betriebsstätte Leistungserbringerorganisation Kassenärztliche Vereinigung"),
  BS_GKV_SPITZENVERBAND("1.2.276.0.76.4.223", "Betriebsstätte GKV-Spitzenverband"),
  BS_MITGLIEDSVERBAND_KRANKENHAEUSER(
      "1.2.276.0.76.4.226", "Betriebsstätte Mitgliedsverband der Krankenhäuser"),
  BS_TRUSTCENTER_GMBH(
      "1.2.276.0.76.4.227",
      "Betriebsstätte der Deutsche Krankenhaus TrustCenter und Informationsverarbeitung GmbH"),
  BS_DEUTSCHEN_KRANKENHAUSGESELLSCHAFT(
      "1.2.276.0.76.4.228", "Betriebsstätte der Deutschen Krankenhausgesellschaft"),
  BS_APOTHEKERVERBAND("1.2.276.0.76.4.224", "Betriebsstätte Apothekerverband"),
  BS_DEUTSCHER_APOTHEKERVERBAND("1.2.276.0.76.4.225", "Betriebsstätte Deutscher Apothekerverband"),
  BS_BUNDESAERZTEKAMMER("1.2.276.0.76.4.229", "Betriebsstätte der Bundesärztekammer"),
  BS_AERZTEKAMMER("1.2.276.0.76.4.230", "Betriebsstätte einer AErztekammer"),
  BS_ZAHNAERZTEKAMMER("1.2.276.0.76.4.231", "Betriebsstätte einer Zahnärztekammer"),
  BS_KASSENAERZTLICHEN_BV(
      "1.2.276.0.76.4.242", "Betriebsstätte der Kassenärztlichen Bundesvereinigung"),
  BS_BUNDESZAHNAERZTEKAMMER("1.2.276.0.76.4.243", "Betriebsstätte der Bundeszahnärztekammer"),
  BS_KASSENZAHNAERZTLICHEN_BV(
      "1.2.276.0.76.4.244", "Betriebsstätte der Kassenzahnärztlichen Bundesvereinigung"),
  BS_GESUNDHEITS_KRANKEN_ALTENPFLEGE(
      "1.2.276.0.76.4.245", "Betriebsstätte Gesundheits-, Kranken- und Altenpflege"),
  BS_GEBURTSHILFE("1.2.276.0.76.4.246", "Betriebsstätte Geburtshilfe"),
  BS_PHYSIOTHERAPIE("1.2.276.0.76.4.247", "Betriebsstätte Physiotherapie"),
  BS_AUGENOPTIKER("1.2.276.0.76.4.248", "Betriebsstätte Augenoptiker"),
  BS_HOERAKUSTIKER("1.2.276.0.76.4.249", "Betriebsstätte Hörakustiker"),
  BS_ORTHOPAEDIESCHUHMACHER("1.2.276.0.76.4.250", "Betriebsstätte Orthopädieschuhmacher"),
  BS_ORTHOPAEDIETECHNIKER("1.2.276.0.76.4.251", "Betriebsstätte Orthopädietechniker"),
  BS_ZAHNTECHNIKER("1.2.276.0.76.4.252", "Betriebsstätte Zahntechniker"),
  RETTUNGSLEITSTELLE("1.2.276.0.76.4.253", "Rettungsleitstelle"),
  BS_SANITAETSDIENST_BW("1.2.276.0.76.4.254", "Betriebsstätte Sanitätsdienst Bundeswehr"),
  BS_OEFFENTLICHER_GESUNDHEITSDIENST(
      "1.2.276.0.76.4.255", "Betriebsstätte Öffentlicher Gesundheitsdienst"),
  BS_ARBEITSMEDIZIN("1.2.276.0.76.4.256", "Betriebsstätte Arbeitsmedizin"),
  BS_VORSORGE_REHABILITATION("1.2.276.0.76.4.257", "Betriebsstätte Vorsorge- und Rehabilitation"),
  EPA_KTR_ZUGRIFFSAUTORISIERUNG("1.2.276.0.76.4.273", "ePA KTR-Zugriffsautorisierung"),
  BS_PFLEGEBERATUNG("1.2.276.0.76.4.262", "Betriebsstätte Pflegeberatung nach § 7a SGB XI"),
  BS_PSYCHOTHERAPEUTENKAMMER("1.2.276.0.76.4.263", "Betriebsstätte Psychotherapeutenkammer"),
  BS_BUNDESPSYCHOTHERAPEUTENKAMMER(
      "1.2.276.0.76.4.264", "Betriebsstätte Bundespsychotherapeutenkammer"),
  BS_LANDESAPOTHEKERKAMMER("1.2.276.0.76.4.265", "Betriebsstätte Landesapothekerkammer"),
  BS_BUNDESAPOTHEKERKAMMER("1.2.276.0.76.4.266", "Betriebsstätte Bundesapothekerkammer"),
  BS_ELEKTRONISCHES_GESUNDHEITSBERUFEREGISTER(
      "1.2.276.0.76.4.267", "Betriebsstätte elektronisches Gesundheitsberuferegister"),
  BS_HANDWERKSKAMMER("1.2.276.0.76.4.268", "Betriebsstätte Handwerkskammer"),
  BS_REGISTER_GESUNDHEITSDATEN(
      "1.2.276.0.76.4.269", "Betriebsstätte Register für Gesundheitsdaten"),
  BS_ABRECHNUNGSDIENSTLEISTER("1.2.276.0.76.4.270", "Betriebsstätte Abrechnungsdienstleister"),
  BS_PKV_VERBAND("1.2.276.0.76.4.271", "Betriebsstätte PKV-Verband"),
  PRAXIS_ERGOTHERAPIE("1.2.276.0.76.4.278", "Ergotherapiepraxis"),
  PRAXIS_LOGOPAEDISCHE("1.2.276.0.76.4.279", "Logopaedische Praxis"),
  PRAXIS_PODOLOGIE("1.2.276.0.76.4.280", "Podologiepraxis"),
  PRAXIS_ERNAEHRUNGSTHERAPEUTISCHE("1.2.276.0.76.4.281", "Ernährungstherapeutische Praxis"),
  DIGA_HERSTELLER("1.2.276.0.76.4.282", "DIGA-Hersteller und Anbieter"),
  BS_WEITERE_KTR_IM_GESUNDHEITSWESEN(
      "1.2.276.0.76.4.284", "Betriebsstätte Weitere Kostenträger im Gesundheitswesen"),
  WEITERE_ORGANISATIONEN_GESUNDHEITSVERSORGUNG(
      "1.2.276.0.76.4.285", "Weitere Organisationen der Gesundheitsversorgung"),
  KIM_HERSTELLER("1.2.276.0.76.4.286", "KIM-Hersteller und -Anbieter"),
  NCPEH_FACHDIENST("1.2.276.0.76.4.292", "NCPeH Fachdienst"),
  TIM_HERSTELLER("1.2.276.0.76.4.295", "TIM-Hersteller und -Anbieter"),
  OMBUDSSTELLE_KTR("1.2.276.0.76.4.303", "Ombudsstelle eines Kostenträgers"),
  BS_AUGENOPTIKER_HOERAKUSTIKER(
      "1.2.276.0.76.4.304", "Betriebsstätte Augenoptiker und Hörakustiker"),
  BS_ORTHOPAEDIESCHUHMACHER_ORTHOPAEDIETECHNIKER(
      "1.2.276.0.76.4.306", "Betriebsstätte Orthopädieschuhmacher und Orthopädietechniker"),
  BS_HILFSMITTELERBRINGER(
      "1.2.276.0.76.4.311",
      "Betriebsstätte Hilfsmittelerbringer (Hinweis: Betriebsstätten der Hilfsmittelerbringer,"
          + " welche nicht den Gesundheitshandwerken zugeordnet sind)"),
  BS_FRISOER("1.2.276.0.76.4.314", "Betriebsstätte Frisör"),
  BS_SOZIOTHERAPIE("1.2.276.0.76.4.317", "Betriebsstätte Soziotherapie"),

  // Note: these are custom values for "Null Object Pattern"
  UNKOWN("1.2.3", "UNBEKANNTE PROFESSION OID"),
  EMPTY("4.5.6", "KEINE PROFESSION OID");

  private static final VzdCodeSystem CODE_SYSTEM = VzdCodeSystem.ORG_PROFESSION_OID;

  private final String code;
  private final String display;

  @Override
  public VzdCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ProfessionOID from(CodeableConcept codeableConcept) {
    if (codeableConcept.getCoding().isEmpty()) {
      return ProfessionOID.EMPTY;
    }

    return Arrays.stream(ProfessionOID.values())
        .filter(v -> v.code.equals(codeableConcept.getCodingFirstRep().getCode()))
        .findFirst()
        .orElse(ProfessionOID.UNKOWN);
  }

  public static boolean matches(CodeableConcept codeableConcept) {
    return CODE_SYSTEM.matches(codeableConcept);
  }
}
