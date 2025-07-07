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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;

@Getter
public class ExtensionTypeMutatorProvider implements FhirTypeMutatorProvider<Extension> {

  private final List<FuzzingMutator<Extension>> mutators;

  public ExtensionTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Extension>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Extension>>();
    mutators.add((ctx, extension) -> ctx.fuzzIdElement(Extension.class, extension));
    mutators.add(
        (ctx, extension) ->
            ctx.fuzzChild(
                Extension.class, ensureNotNull(ctx.randomness(), extension).getUrlElement()));

    mutators.add(
        (ctx, extension) -> {
          extension = ensureNotNull(ctx.randomness(), extension);
          val id = extension.getId();
          return ctx.randomness()
              .chooseRandomly(extension.getExtension())
              .map(ext -> ctx.fuzzChild(Extension.class, ext))
              .orElseGet(
                  () ->
                      FuzzingLogEntry.noop(
                          format(
                              "Extension with ID ''{0}'' does not have any child extensions", id)));
        });

    mutators.add(
        (ctx, extension) -> {
          extension = ensureNotNull(ctx.randomness(), extension);
          val children = extension.getExtension();

          if (!children.isEmpty()) {
            ctx.randomness()
                .childResourceDice()
                .chooseRandomElements(children)
                .forEach(children::remove);
            return FuzzingLogEntry.operation(
                format("Remove randomly child extensions in extension {0}", extension.getId()));
          } else if (!extension.hasValue()) {
            val amount = ctx.randomness().source().nextInt(1, 10);

            for (var idx = 0; idx < amount; idx++) {
              val type = ctx.randomness().fhir().createType();
              type.setIdBase(ctx.randomness().uuid());
              extension.addExtension(ctx.randomness().url(idx), type);
            }

            return FuzzingLogEntry.operation(
                format(
                    "Add randomly {0} child extensions to extension {1}",
                    amount, extension.getId()));
          } else {
            return ctx.fuzzChild(extension.getClass(), extension.getValue());
          }
        });

    return mutators;
  }

  private static Extension ensureNotNull(Randomness randomness, Extension extension) {
    if (extension == null) {
      extension = randomness.fhir().createType(Extension.class);
    }

    if (extension.getValue() == null) {
      extension.setValue(new StringType(randomness.regexify("[A-Za-z0-9._%$+-]{1,10}")));
    }

    if (extension.getUrl() == null) {
      extension.setUrl(randomness.url());
    }

    // ensure children-extensions can be manipulated!
    val children = extension.getExtension();
    extension.setExtension(new ArrayList<>(children));

    return extension;
  }
}
