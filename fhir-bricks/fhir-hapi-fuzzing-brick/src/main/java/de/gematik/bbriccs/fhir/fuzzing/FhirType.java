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

package de.gematik.bbriccs.fhir.fuzzing;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@RequiredArgsConstructor
public enum FhirType {
  DATE(DateType.class),
  DATE_TIME(DateTimeType.class),
  INSTANT(InstantType.class),
  TIME(TimeType.class),
  META(Meta.class),
  MONEY_QUANTITY(MoneyQuantity.class),
  ADDRESS(Address.class),
  ATTACHMENT(Attachment.class),
  INTEGER_TYPE(IntegerType.class),
  INTEGER_POSITIVE_TYPE(PositiveIntType.class),
  INTEGER_UNSIGNED(UnsignedIntType.class),
  COUNT(Count.class),
  DATA_REQUIREMENT(DataRequirement.class),
  DOSAGE(Dosage.class),
  UUID(UuidType.class),
  IDENTIFIER(Identifier.class),
  SUBSTANCE_AMOUNT(SubstanceAmount.class),
  NARRATIVE(Narrative.class),
  CODING(Coding.class),
  SAMPLE_DATA(SampledData.class),
  ID(IdType.class),
  ELEMENT_DEFINITION(ElementDefinition.class),
  DISTANCE(Distance.class),
  PERIOD(Period.class),
  DURATION(Duration.class),
  CANONICAL(CanonicalType.class),
  RANGE(Range.class),
  RELATED_ARTIFACT(RelatedArtifact.class),
  PRODUCT_SHELF_LIFE(ProductShelfLife.class),
  BASE64_BINARY(Base64BinaryType.class),
  USAGE_CONTEXT(UsageContext.class),
  TIMING(Timing.class),
  DECIMAL(DecimalType.class),
  PROD_CHARACTERISTIC(ProdCharacteristic.class),
  CODEABLE_CONCEPT(CodeableConcept.class),
  CODE(CodeType.class),
  PARAMETER_DEFINITION(ParameterDefinition.class),
  STRING(StringType.class),
  CONTRIBUTOR(Contributor.class),
  OID(OidType.class),
  MONEY(Money.class),
  HUMAN_NAME(HumanName.class),
  CONTACT_POINT(ContactPoint.class),
  MARKETING_STATUS(MarketingStatus.class),
  MARKDOWN(MarkdownType.class),
  POPULATION(Population.class),
  RATIO(Ratio.class),
  AGE(Age.class),
  REFERENCE(Reference.class),
  TRIGGER_DEFINITION(TriggerDefinition.class),
  SIMPLE_QUANTITY(SimpleQuantity.class),
  QUANTITY(Quantity.class),
  URI(UriType.class),
  URL(UrlType.class),
  ANNOTATION(Annotation.class),
  EXTENSION(Extension.class),
  CONTACT_DETAIL(ContactDetail.class),
  BOOLEAN(BooleanType.class),
  EXPRESSION(Expression.class),
  SIGNATURE(Signature.class);

  private final Class<? extends Type> typeClass;

  public String getTypeName() {
    val dataTypeDef =
        Arrays.stream(this.typeClass.getAnnotations())
            .filter(a -> a.annotationType().equals(DatatypeDef.class))
            .map(a -> (DatatypeDef) a)
            .findFirst()
            .orElseThrow();
    return dataTypeDef.name();
  }

  public static <T extends Type> FhirType fromClass(Class<T> type) {
    return Arrays.stream(FhirType.values())
        .filter(ft -> ft.typeClass.equals(type))
        .findFirst()
        .orElseThrow(
            () -> new FuzzerException(format("No FhirType found for {0}", type.getSimpleName())));
  }
}
