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

package de.gematik.bbriccs.fhir.codec;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.exceptions.FhirCodecException;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.validation.*;
import de.gematik.refv.SupportedValidationModule;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.experimental.Delegate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;

public class FhirCodec {

  private final FhirContext ctx;
  private IParser xmlParser;
  private IParser jsonParser;

  @Delegate private final ValidatorFhir validator;

  protected FhirCodec(FhirContext ctx, ValidatorFhir validator) {
    this.ctx = ctx;
    this.validator = validator;
  }

  public String encode(IBaseResource resource, EncodingType encoding) {
    return this.encode(resource, encoding, false);
  }

  public String encode(IBaseResource resource, EncodingType encoding, boolean prettyPrint) {
    if (resource instanceof EmptyResource) {
      return "";
    }
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);
    parser.setPrettyPrint(prettyPrint);
    return parser.encodeResourceToString(resource);
  }

  public <T extends Resource> T decode(Class<T> expectedClass, String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(expectedClass, content, encoding);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T extends Resource> T decode(
      Class<T> expectedClass, String content, EncodingType encoding) {
    if (StringUtils.isBlank(content)) {
      // if the content is empty, there is no need to bother HAPI and just simply return an
      // EmptyResource
      return (T) new EmptyResource();
    }
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);

    try {
      return parser.parseResource(expectedClass, content);
    } catch (Throwable t) {
      throw new FhirCodecException(
          format(
              "Error while decoding content of length {0} as {1}", content.length(), expectedClass),
          t);
    }
  }

  public Resource decode(String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(content, encoding);
  }

  /**
   * This method will decode the content to base resource without returning the concrete type of the
   * resource. Whenever the expected type of the concrete resource is known, it is recommended to
   * use the decode-variant with type expectation
   *
   * @param content to be decoded to a FHIR-Resource
   * @param encoding type of content
   * @return decoded base FHIR-Resource
   */
  public Resource decode(final String content, EncodingType encoding) {
    return this.decode(null, content, encoding);
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser = ctx.newXmlParser();
    }
    return this.xmlParser;
  }

  private IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = ctx.newJsonParser();
    }
    return this.jsonParser;
  }

  public static FhirCodecBuilder forR4() {
    return new FhirCodecBuilder(FhirContext.forR4());
  }

  public static class FhirCodecBuilder {

    private final FhirContext ctx;
    private final List<ResourceTypeHint<?, ?>> typeHints;

    private FhirCodecBuilder(FhirContext ctx) {
      this.ctx = ctx;
      this.typeHints = new LinkedList<>();
    }

    public FhirCodecBuilder disableErrors() {
      val errorHandler = new LenientErrorHandler();
      errorHandler.disableAllErrors();
      ctx.setParserErrorHandler(errorHandler);

      return this;
    }

    public <T extends ProfileVersion, R extends Resource> FhirCodecBuilder withTypeHint(
        WithStructureDefinition<T> definition, Class<R> mappingClass) {
      return withTypeHint(ResourceTypeHint.forStructure(definition).mappingTo(mappingClass));
    }

    public <T extends ProfileVersion, R extends Resource> FhirCodecBuilder withTypeHint(
        WithStructureDefinition<T> definition, @Nullable T version, Class<R> mappingClass) {
      return withTypeHint(
          ResourceTypeHint.forStructure(definition).forVersion(version).mappingTo(mappingClass));
    }

    public <T extends ProfileVersion, R extends Resource> FhirCodecBuilder withTypeHint(
        ResourceTypeHint<T, R> typeHint) {
      this.typeHints.add(typeHint);
      return this;
    }

    public <T extends ResourceTypeHint<? extends ProfileVersion, ? extends Resource>>
        FhirCodecBuilder withTypeHints(List<T> typeHints) {
      this.typeHints.addAll(typeHints);
      return this;
    }

    public FhirCodec andNonProfiledValidator() {
      val validator = new NonProfiledValidator(this.ctx);
      return andCustomValidator(validator);
    }

    public FhirCodec andDummyValidator() {
      val validator = new DummyValidator(this.ctx);
      return andCustomValidator(validator);
    }

    public FhirCodec andBbriccsValidator() {
      val validator = ValidatorFhirFactory.createValidator(this.ctx);
      return andCustomValidator(validator);
    }

    public FhirCodec andReferenzValidator(SupportedValidationModule svm) {
      val validator = ReferenzValidator.withValidationModule(this.ctx, svm);
      return andCustomValidator(validator);
    }

    public FhirCodec andCustomValidator(ValidatorFhir validator) {
      this.typeHints.forEach(th -> th.register(this.ctx));
      return new FhirCodec(this.ctx, validator);
    }
  }
}
