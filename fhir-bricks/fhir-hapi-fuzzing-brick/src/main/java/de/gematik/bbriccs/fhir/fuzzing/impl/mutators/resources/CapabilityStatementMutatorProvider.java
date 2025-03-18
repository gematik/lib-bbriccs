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
 */

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CapabilityStatement;

public class CapabilityStatementMutatorProvider
    extends BaseDomainResourceMutatorProvider<CapabilityStatement> {

  public CapabilityStatementMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<CapabilityStatement>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CapabilityStatement>>();

    mutators.add(
        (ctx, cs) -> ctx.fuzzChildTypes(cs.getClass(), cs.getContact(), cs::getContactFirstRep));
    mutators.add(
        (ctx, cs) ->
            ctx.fuzzChildTypes(cs.getClass(), cs.getJurisdiction(), cs::getJurisdictionFirstRep));
    mutators.add(
        (ctx, cs) ->
            ctx.fuzzChildTypes(cs.getClass(), cs.getUseContext(), cs::getUseContextFirstRep));
    mutators.add(
        (ctx, cs) -> ctx.fuzzChildTypes(cs.getClass(), cs.getFormat(), cs::addFormatElement));
    mutators.add(
        (ctx, cs) ->
            ctx.fuzzChildTypes(cs.getClass(), cs.getPatchFormat(), cs::addPatchFormatElement));
    mutators.add(
        (ctx, cs) -> ctx.fuzzChildTypes(cs.getClass(), cs.getImports(), cs::addImportsElement));
    mutators.add(
        (ctx, cs) ->
            ctx.fuzzChildTypes(cs.getClass(), cs.getInstantiates(), cs::addInstantiatesElement));
    mutators.add(
        (ctx, cs) ->
            ctx.fuzzChildTypes(
                cs.getClass(), cs.getImplementationGuide(), cs::addImplementationGuideElement));

    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasTitle, cs::getTitleElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasName, cs::getNameElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasVersion, cs::getVersionElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasDescription, cs::getDescriptionElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasCopyright, cs::getCopyrightElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasDate, cs::getDateElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasPublisher, cs::getPublisherElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasExperimental, cs::getExperimentalElement));
    mutators.add((ctx, cs) -> ctx.fuzzChild(cs, cs::hasUrl, cs::getUrlElement));

    return mutators;
  }
}
