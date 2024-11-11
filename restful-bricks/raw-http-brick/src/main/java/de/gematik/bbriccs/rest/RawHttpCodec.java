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

package de.gematik.bbriccs.rest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Interface for encoding and decoding {@link HttpBEntity} objects to and from their raw string
 * representation according to the RFC standards.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616">RFC-2616</a>
 */
public interface RawHttpCodec {

  /**
   * Encodes the given {@link HttpBRequest} object into its String representation according to the
   * RFC standards. This encoded HTTP request can then be used for logging purposes, transmission
   * over HTTP/VAU or any other use case where an encoded HTTP request is needed.
   *
   * @param request the {@link HttpBRequest} object holding the internal data of the HTTP request
   * @return the encoded HTTP request as a plain string representation
   */
  String encode(HttpBRequest request);

  /**
   * Encodes the given {@link HttpBResponse} object into its String representation according to the
   * RFC standards. This encoded HTTP response can then be used for logging purposes, transmission
   * over HTTP/VAU or any other use case where an encoded HTTP response is needed.
   *
   * @param response the {@link HttpBResponse} object holding the internal data of the HTTP response
   * @return the encoded HTTP response as a plain string representation
   */
  String encode(HttpBResponse response);

  /**
   * Encodes the given {@link HttpBRequest} object into its Base64 encoded String representation
   *
   * @param request the {@link HttpBRequest} object holding the internal data of the HTTP request
   * @return the encoded HTTP request as a Base64 encoded string representation
   */
  default String encodeB64(HttpBRequest request) {
    return Base64.getEncoder().encodeToString(encode(request).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Encodes the given {@link HttpBResponse} object into its Base64 encoded String representation
   *
   * @param response the {@link HttpBResponse} object holding the internal data of the HTTP response
   * @return the encoded HTTP response as a Base64 encoded string representation
   */
  default String encodeB64(HttpBResponse response) {
    return Base64.getEncoder().encodeToString(encode(response).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a raw HTTP response string into an {@link HttpBResponse} object.
   *
   * @param rawResponse the raw HTTP response string
   * @return the decoded {@link HttpBResponse} object
   */
  HttpBResponse decodeResponse(String rawResponse);

  /**
   * Decodes a raw HTTP response byte array into an {@link HttpBResponse} object.
   *
   * @param rawResponse the raw HTTP response byte array
   * @return the decoded {@link HttpBResponse} object
   */
  default HttpBResponse decodeResponse(byte[] rawResponse) {
    return decodeResponse(new String(rawResponse, StandardCharsets.UTF_8));
  }

  /**
   * Decodes a Base64 encoded HTTP response string into an {@link HttpBResponse} object.
   *
   * @param b64RawResponse the Base64 encoded HTTP response string
   * @return the decoded {@link HttpBResponse} object
   */
  default HttpBResponse decodeResponseB64(String b64RawResponse) {
    return decodeResponse(Base64.getDecoder().decode(b64RawResponse));
  }

  HttpBRequest decodeRequest(String rawRequest);

  default HttpBRequest decodeRequest(byte[] rawRequest) {
    return decodeRequest(new String(rawRequest, StandardCharsets.UTF_8));
  }

  default HttpBRequest decodeRequestB64(String b64RawRequest) {
    return decodeRequest(Base64.getDecoder().decode(b64RawRequest));
  }

  /**
   * Provides the default implementation of the {@link RawHttpCodec} interface.
   *
   * @return the default {@link RawHttpCodec} implementation
   */
  static RawHttpCodec defaultCodec() {
    return new DefaultRawHttpCodec();
  }
}
