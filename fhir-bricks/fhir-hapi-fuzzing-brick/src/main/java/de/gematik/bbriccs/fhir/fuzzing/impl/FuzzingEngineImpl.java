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

package de.gematik.bbriccs.fhir.fuzzing.impl;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.*;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveType;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingSessionLogbook;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.primitive.CodeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.primitive.PlainTextMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.primitive.PrimitiveUriMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources.*;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types.*;
import de.gematik.bbriccs.fhir.fuzzing.impl.rnd.ProbabilityDiceImpl;
import de.gematik.bbriccs.fhir.fuzzing.impl.rnd.RandomnessImpl;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class FuzzingEngineImpl implements FuzzingEngine {

  private final List<FuzzingSessionLogbook> fuzzingSessionHistory;
  private final FuzzingContext ctx;

  private FuzzingEngineImpl(FuzzingContext ctx) {
    this.ctx = ctx;
    this.fuzzingSessionHistory = new LinkedList<>();
  }

  @Override
  public <R extends Resource> FuzzingSessionLogbook fuzz(R resource) {
    return fuzz(resource, 0);
  }

  private <R extends Resource> FuzzingSessionLogbook fuzz(R resource, int recursionDepth) {
    val message =
        format(
            "Start Resource Fuzzer for {0} with ID {1}",
            resource.getClass().getSimpleName(), resource.getId());
    log.info(message);

    val start = Instant.now();
    val entry = this.ctx.startFuzzingSession(resource);
    val finish = Instant.now();
    val duration = java.time.Duration.between(start, finish);

    val sessionLog = FuzzingSessionLogbook.logSession(message, duration, entry);
    this.fuzzingSessionHistory.add(0, sessionLog);

    if (sessionLog.changes() == 0 && recursionDepth < 10) {
      log.info("recursively repeat fuzzing iteration {}", recursionDepth);
      val rsl = this.fuzz(resource, recursionDepth + 1);
      val rslEntries = rsl.getSessionLog().getChildren();
      val completeLog = new ArrayList<FuzzingLogEntry>(rslEntries.size() + entry.size());
      completeLog.addAll(rslEntries);
      completeLog.addAll(entry);
      return FuzzingSessionLogbook.logSession(
          message, rsl.getDuration().plus(sessionLog.getDuration()), completeLog);
    }

    log.info(
        "Finish Fuzzing Session for {} after {}: added {} / mutated {}",
        resource.getClass().getSimpleName(),
        duration,
        sessionLog.getAdded(),
        sessionLog.getMutations());
    return sessionLog;
  }

  @Override
  public FuzzingSessionLogbook getLastSessionLog() {
    if (this.fuzzingSessionHistory.isEmpty()) {
      throw new FuzzerException(format("no sessions started so far"));
    }
    return this.fuzzingSessionHistory.get(0);
  }

  @Override
  public List<FuzzingSessionLogbook> getSessionHistory() {
    return this.fuzzingSessionHistory;
  }

  @Override
  public FuzzingContext getContext() {
    return this.ctx;
  }

  public static Builder builder(double probability) {
    return builder(new SecureRandom(), probability);
  }

  public static Builder builder(Random rnd, double probability) {
    val pdMutator = new ProbabilityDiceImpl(rnd, probability);
    val pdChildResources = new ProbabilityDiceImpl(rnd, probability);
    val randomness = new RandomnessImpl(rnd, pdMutator, pdChildResources);
    return builder(randomness);
  }

  public static Builder builder(Randomness randomness) {
    val ctx = new FuzzingContextImpl(randomness);
    return new Builder(ctx);
  }

  public static class Builder {
    private final FuzzingContextImpl ctx;

    private Builder(FuzzingContextImpl ctx) {
      this.ctx = ctx;
    }

    public Builder withDefaultFuzzers() {
      // default Resource-Fuzzer
      return this.registerResourceFuzzer(Account.class, AccountMutatorProvider::new)
          .registerResourceFuzzer(AllergyIntolerance.class, AllergyIntoleranceMutatorProvider::new)
          .registerResourceFuzzer(Appointment.class, AppointmentMutatorProvider::new)
          .registerResourceFuzzer(
              AppointmentResponse.class, AppointmentResponseMutatorProvider::new)
          .registerResourceFuzzer(AuditEvent.class, AuditEventMutatorProvider::new)
          .registerResourceFuzzer(Binary.class, BinaryMutatorProvider::new)
          .registerResourceFuzzer(
              BiologicallyDerivedProduct.class, BiologicallyDerivedProductMutatorProvider::new)
          .registerResourceFuzzer(BodyStructure.class, BodyStructureMutatorProvider::new)
          .registerResourceFuzzer(Bundle.class, BundleMutatorProvider::new)
          .registerResourceFuzzer(
              CapabilityStatement.class, CapabilityStatementMutatorProvider::new)
          .registerResourceFuzzer(CarePlan.class, CarePlanMutatorProvider::new)
          .registerResourceFuzzer(CareTeam.class, CareTeamMutatorProvider::new)
          .registerResourceFuzzer(ChargeItem.class, ChargeItemMutatorProvider::new)
          .registerResourceFuzzer(
              ChargeItemDefinition.class, ChargeItemDefinitionMutatorProvider::new)
          .registerResourceFuzzer(Claim.class, ClaimMutatorProvider::new)
          .registerResourceFuzzer(ClaimResponse.class, ClaimResponseMutatorProvider::new)
          .registerResourceFuzzer(Composition.class, CompositionMutatorProvider::new)
          .registerResourceFuzzer(Condition.class, ConditionMutatorProvider::new)
          .registerResourceFuzzer(Communication.class, CommunicationMutatorProvider::new)
          .registerResourceFuzzer(
              CommunicationRequest.class, CommunicationRequestMutatorProvider::new)
          .registerResourceFuzzer(Consent.class, ConsentMutatorProvider::new)
          .registerResourceFuzzer(Contract.class, ContractMutatorProvider::new)
          .registerResourceFuzzer(Coverage.class, CoverageMutatorProvider::new)
          .registerResourceFuzzer(DetectedIssue.class, DetectedIssueMutatorProvider::new)
          .registerResourceFuzzer(Device.class, DeviceMutatorProvider::new)
          .registerResourceFuzzer(DocumentReference.class, DocumentReferenceMutatorProvider::new)
          .registerResourceFuzzer(Encounter.class, EncounterMutatorProvider::new)
          .registerResourceFuzzer(Goal.class, GoalMutatorProvider::new)
          .registerResourceFuzzer(Group.class, GroupMutatorProvider::new)
          .registerResourceFuzzer(InsurancePlan.class, InsurancePlanMutatorProvider::new)
          .registerResourceFuzzer(Invoice.class, InvoiceMutatorProvider::new)
          .registerResourceFuzzer(Location.class, LocationMutatorProvider::new)
          .registerResourceFuzzer(Media.class, MediaMutatorProvider::new)
          .registerResourceFuzzer(Medication.class, MedicationMutatorProvider::new)
          .registerResourceFuzzer(MedicationDispense.class, MedicationDispenseMutatorProvider::new)
          .registerResourceFuzzer(MedicationRequest.class, MedicationRequestMutatorProvider::new)
          .registerResourceFuzzer(Observation.class, ObservationMutatorProvider::new)
          .registerResourceFuzzer(Organization.class, OrganizationMutatorProvider::new)
          .registerResourceFuzzer(Parameters.class, ParametersMutatorProvider::new)
          .registerResourceFuzzer(Patient.class, PatientMutatorProvider::new)
          .registerResourceFuzzer(Person.class, PersonMutatorProvider::new)
          .registerResourceFuzzer(Practitioner.class, PractitionerMutatorProvider::new)
          .registerResourceFuzzer(Provenance.class, ProvenanceMutatorProvider::new)
          .registerResourceFuzzer(PractitionerRole.class, PractitionerRoleMutatorProvider::new)
          .registerResourceFuzzer(Specimen.class, SpecimenMutatorProvider::new)
          .registerResourceFuzzer(Subscription.class, SubscriptionMutatorProvider::new)
          .registerResourceFuzzer(
              SubstanceReferenceInformation.class,
              SubstanceReferenceInformationMutatorProvider::new)
          .registerResourceFuzzer(SupplyDelivery.class, SupplyDeliveryMutatorProvider::new)
          .registerResourceFuzzer(SupplyRequest.class, SupplyRequestMutatorProvider::new)
          .registerResourceFuzzer(Task.class, TaskMutatorProvider::new)
          .registerTypeFuzzer(Address.class, AddressTypeMutatorProvider::new)
          .registerTypeFuzzer(Annotation.class, AnnotationMutatorProvider::new)
          .registerTypeFuzzer(Attachment.class, AttachmentMutatorProvider::new)
          .registerTypeFuzzer(Base64BinaryType.class, Base64BinaryTypeMutatorProvider::new)
          .registerTypeFuzzer(BooleanType.class, BooleanTypeMutatorProvider::new)
          .registerTypeFuzzer(CanonicalType.class, CanonicalTypeMutatorProvider::new)
          .registerTypeFuzzer(CodeType.class, CodeTypeMutatorProvider::new)
          .registerTypeFuzzer(CodeableConcept.class, CodeableConceptTypeMutatorProvider::new)
          .registerTypeFuzzer(Coding.class, CodingMutatorProvider::new)
          .registerTypeFuzzer(ContactDetail.class, ContactDetailMutatorProvider::new)
          .registerTypeFuzzer(ContactPoint.class, ContactPointMutatorProvider::new)
          .registerTypeFuzzer(Contributor.class, ContributorMutatorProvider::new)
          .registerTypeFuzzer(DataRequirement.class, DataRequirementMutatorProvider::new)
          .registerTypeFuzzer(DateTimeType.class, DateTimeTypeMutatorProvider::new)
          .registerTypeFuzzer(DateType.class, DateTypeMutatorProvider::new)
          .registerTypeFuzzer(DecimalType.class, DecimalTypeMutatorProvider::new)
          .registerTypeFuzzer(Dosage.class, DosageMutatorProvider::new)
          .registerTypeFuzzer(ElementDefinition.class, ElementDefinitionMutatorProvider::new)
          .registerTypeFuzzer(Expression.class, ExpressionMutatorProvider::new)
          .registerTypeFuzzer(Extension.class, ExtensionTypeMutatorProvider::new)
          .registerTypeFuzzer(HumanName.class, HumanNameTypeMutatorProvider::new)
          .registerTypeFuzzer(Identifier.class, IdentifierTypeMutatorProvider::new)
          .registerTypeFuzzer(IdType.class, IdTypeMutatorProvider::new)
          .registerTypeFuzzer(InstantType.class, InstantTypeMutatorProvider::new)
          .registerTypeFuzzer(IntegerType.class, IntegerTypeMutatorProvider::new)
          .registerTypeFuzzer(MarketingStatus.class, MarketingStatusMutatorProvider::new)
          .registerTypeFuzzer(Meta.class, MetaTypeMutatorProvider::new)
          .registerTypeFuzzer(Money.class, MoneyMutatorProvider::new)
          .registerTypeFuzzer(Narrative.class, NarrativeMutatorProvider::new)
          .registerTypeFuzzer(ParameterDefinition.class, ParameterDefinitionMutatorProvider::new)
          .registerTypeFuzzer(Period.class, PeriodTypeMutatorProvider::new)
          .registerTypeFuzzer(Population.class, PopulationMutatorProvider::new)
          .registerTypeFuzzer(PositiveIntType.class, PositiveIntTypeMutatorProvider::new)
          .registerTypeFuzzer(ProdCharacteristic.class, ProdCharacteristicMutatorProvider::new)
          .registerTypeFuzzer(ProductShelfLife.class, ProductShelfLifeMutatorProvider::new)
          .registerTypeFuzzer(Quantity.class, QuantityMutatorProvider::new)
          .registerTypeFuzzer(Ratio.class, RatioMutatorProvider::new)
          .registerTypeFuzzer(Range.class, RangeMutatorProvider::new)
          .registerTypeFuzzer(Reference.class, ReferenceTypeMutatorProvider::new)
          .registerTypeFuzzer(RelatedArtifact.class, RelatedArtifactMutatorProvider::new)
          .registerTypeFuzzer(SampledData.class, SampleDataMutatorProvider::new)
          .registerTypeFuzzer(Signature.class, SignatureMutatorProvider::new)
          .registerTypeFuzzer(StringType.class, StringTypeMutatorProvider::new)
          .registerTypeFuzzer(SubstanceAmount.class, SubstanceAmountMutatorProvider::new)
          .registerTypeFuzzer(TimeType.class, TimeTypeMutatorProvider::new)
          .registerTypeFuzzer(Timing.class, TimingTypeMutatorProvider::new)
          .registerTypeFuzzer(TriggerDefinition.class, TriggerDefinitionMutatorProvider::new)
          .registerTypeFuzzer(UnsignedIntType.class, UnsignedIntTypeMutatorProvider::new)
          .registerTypeFuzzer(UriType.class, UriTypeMutatorProvider::new)
          .registerTypeFuzzer(UsageContext.class, UsageContextMutatorProvider::new)
          .registerPrimitiveTypeFuzzer(PrimitiveStringTypes.URI, PrimitiveUriMutatorProvider::new)
          .registerPrimitiveTypeFuzzer(PrimitiveStringTypes.TEXT, PlainTextMutatorProvider::new)
          .registerPrimitiveTypeFuzzer(PrimitiveStringTypes.CODE, CodeMutatorProvider::new);
    }

    public <R extends Resource> Builder registerResourceFuzzer(
        Class<R> rClass, Supplier<FhirResourceMutatorProvider<R>> fuzzerConstructor) {

      ctx.getResourceFuzzer()
          .computeIfAbsent(rClass, k -> new LinkedList<>())
          .add(fuzzerConstructor.get());
      return this;
    }

    public <R extends Resource> Builder registerResourceMutator(
        Class<R> rClass, FuzzingMutator<R> mutator) {
      ctx.getResourceFuzzerFor(rClass)
          .orElseThrow(
              () ->
                  new FuzzerException(
                      format("No Fuzzer registered for resource {0}", rClass.getSimpleName())))
          .getMutators()
          .add(mutator);
      return this;
    }

    public <T extends Type> Builder registerTypeFuzzer(
        Class<T> tClass, Supplier<FhirTypeMutatorProvider<T>> fuzzerConstructor) {
      ctx.getTypeFuzzer()
          .computeIfAbsent(tClass, k -> new LinkedList<>())
          .add(fuzzerConstructor.get());
      return this;
    }

    public <T extends Type> Builder registerTypeMutator(
        Class<T> tClass, FuzzingMutator<T> mutator) {
      ctx.getTypeFuzzerFor(tClass)
          .orElseThrow(
              () ->
                  new FuzzerException(
                      format("No Fuzzer registered for type {0}", tClass.getSimpleName())))
          .getMutators()
          .add(mutator);
      return this;
    }

    public <P> Builder registerPrimitiveTypeFuzzer(
        PrimitiveType<P> pType, Supplier<PrimitiveMutatorProvider<P>> fuzzerConstructor) {
      ctx.getPrimitiveFuzzer()
          .computeIfAbsent(pType, k -> new LinkedList<>())
          .add(fuzzerConstructor.get());
      return this;
    }

    public <P> Builder registerPrimitiveMutator(
        PrimitiveType<P> pType, PrimitiveTypeMutator<P> mutator) {
      ctx.getPrimitiveFuzzerFor(pType)
          .orElseThrow(
              () ->
                  new FuzzerException(
                      format("No Fuzzer registered for type {0}", pType.getTypeClass())))
          .getMutators()
          .add(mutator);
      return this;
    }

    public FuzzingEngine build() {
      return new FuzzingEngineImpl(ctx);
    }
  }
}
