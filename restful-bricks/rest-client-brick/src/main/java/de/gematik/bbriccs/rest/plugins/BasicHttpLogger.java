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

package de.gematik.bbriccs.rest.plugins;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.RawHttpCodec;
import java.io.PrintStream;
import lombok.val;

public class BasicHttpLogger implements HttpBObserver {

  private final PrintStream out;
  private final RawHttpCodec httpCodec;

  private final String requestTag;
  private final String responseTag;

  @SuppressWarnings("java:S106")
  private BasicHttpLogger(String label) {
    this(label, System.out);
  }

  private BasicHttpLogger(String label, PrintStream out) {
    this.out = out;
    this.httpCodec = RawHttpCodec.defaultCodec();

    this.requestTag = format("--------- REQUEST {0} ---------", label);
    this.responseTag = format("--------- RESPONSE {0} ---------", label);
  }

  @Override
  public void onRequest(HttpBRequest httpBRequest) {
    val request = httpCodec.encode(httpBRequest);
    this.out.println(this.requestTag);
    this.out.println(request);
    this.out.println(format("X{0}\n", this.requestTag));
  }

  @Override
  public void onResponse(HttpBResponse httpBResponse) {
    var response = httpCodec.encode(httpBResponse);
    if (httpBResponse.body().length > 1000) {
      response = response.substring(0, 1000) + "\n...";
    }

    this.out.println(this.responseTag);
    this.out.println(response);
    this.out.println(format("X{0}\n", this.responseTag));
  }

  public static HttpBObserver toStdout() {
    return toStdout("");
  }

  public static HttpBObserver toStdout(String label) {
    return new BasicHttpLogger(label);
  }
}
