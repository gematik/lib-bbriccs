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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveStringTypes;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;

@Getter
public class InvoiceMutatorProvider extends BaseDomainResourceMutatorProvider<Invoice> {

  public InvoiceMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Invoice>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Invoice>>();

    mutators.add(
        (ctx, invoice) ->
            ctx.fuzzChildTypes(
                invoice.getClass(), invoice.getIdentifier(), invoice::getIdentifierFirstRep));
    mutators.add(
        (ctx, invoice) ->
            ctx.fuzzChildTypes(invoice.getClass(), invoice.getNote(), invoice::getNoteFirstRep));

    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasAccount, invoice::getAccount));
    mutators.add(
        (ctx, invoice) ->
            ctx.fuzzChild(
                invoice, invoice::hasCancelledReason, invoice::getCancelledReasonElement));
    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasDate, invoice::getDateElement));
    mutators.add((ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasIssuer, invoice::getIssuer));
    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasRecipient, invoice::getRecipient));
    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasSubject, invoice::getSubject));
    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasTotalGross, invoice::getTotalGross));
    mutators.add(
        (ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasTotalNet, invoice::getTotalNet));
    mutators.add((ctx, invoice) -> ctx.fuzzChild(invoice, invoice::hasType, invoice::getType));

    mutators.add(
        (ctx, invoice) -> {
          val response =
              ctx.fuzzPrimitiveType(
                  format("Fuzz PaymentTerms of Invoice {0}", invoice.getId()),
                  PrimitiveStringTypes.TEXT,
                  invoice.getPaymentTerms());
          invoice.setPaymentTerms(response.getFuzzedValue());
          return response.getLogEntry();
        });

    mutators.add(
        (ctx, invoice) -> {
          val otpc = invoice.getTotalPriceComponent();
          val children =
              ctx.randomness().childResourceDice().chooseRandomElements(otpc).stream()
                  .map(ilipcc -> ctx.fuzzChild(invoice.getClass(), ilipcc.getCode()))
                  .toList();
          return FuzzingLogEntry.parent(
              format("Fuzz TotalPriceComponents of Invoice {0}", invoice.getId()), children);
        });

    return mutators;
  }
}
