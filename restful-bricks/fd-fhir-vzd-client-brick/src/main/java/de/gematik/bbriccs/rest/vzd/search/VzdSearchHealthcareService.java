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

package de.gematik.bbriccs.rest.vzd.search;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.rest.fd.query.QueryParameter;
import de.gematik.bbriccs.rest.fd.query.SearchQueryParameter;
import de.gematik.bbriccs.rest.vzd.request.VzdRequestHealthcareServiceSearch;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VzdSearchHealthcareService implements VzdSearchRequestBuilder {

  private static final List<QueryParameter> DEFAULT_QUERY_PARAM =
      List.of(
          new SearchQueryParameter("organization.active", "true"),
          new SearchQueryParameter("_include", "*"));

  private static final String PARAM_TEXT = "_text";

  private final NumberFormat nf;
  private final List<QueryParameter> queryParameter;

  protected VzdSearchHealthcareService(QueryParameter... queryParameter) {
    this.queryParameter = new ArrayList<>(List.of(queryParameter));
    this.queryParameter.addAll(0, DEFAULT_QUERY_PARAM);

    /*
     * NumberFormat is required here to represent doubles properly and consistently without
     * relying on the system locale defaults, e.g. for Local.GERMAN
     * double 13.3914614 encoded to 13,3914614 and escaped to 13%2C3914614
     */
    this.nf = NumberFormat.getInstance(Locale.ENGLISH);

    /*
     * maximum fraction digits 12 is required because the default is set to 3 leading to issues like:
     * double 13.3914614 encoded to 13.391 instead of
     * double 13.3914614 encoded to 13.3914614
     *
     * Note: 12 is just a gues, adjusting the precision dynamically could be useful
     */
    this.nf.setMaximumFractionDigits(12);
  }

  public VzdSearchHealthcareService withTelematikId(TelematikID telematikId) {
    return withTelematikId(telematikId.getValue());
  }

  public VzdSearchHealthcareService withTelematikId(String telematikId) {
    queryParameter.add(
        new SearchQueryParameter(PARAM_TEXT, VzdSearchRequestBuilder.quote(telematikId)));
    return this;
  }

  public VzdSearchHealthcareService withIknr(IKNR iknr) {
    return withIknr(iknr.getValue());
  }

  public VzdSearchHealthcareService withIknr(String iknr) {
    val iknrSystem = DeBasisProfilStructDef.IDENTIFIER_IKNR.getCanonicalUrl();
    val orgIdentifier = format("{0}|{1}", iknrSystem, iknr);
    queryParameter.add(new SearchQueryParameter("organization.identifier", orgIdentifier));
    return this;
  }

  public VzdSearchHealthcareService withName(String name) {
    queryParameter.add(new SearchQueryParameter(PARAM_TEXT, VzdSearchRequestBuilder.quote(name)));
    return this;
  }

  public VzdSearchHealthcareService inCity(String city) {
    queryParameter.add(new SearchQueryParameter(PARAM_TEXT, VzdSearchRequestBuilder.quote(city)));
    return this;
  }

  public VzdSearchHealthcareService nearBy(double latitude, double longitude) {
    return nearBy(latitude, longitude, 10);
  }

  public VzdSearchHealthcareService nearBy(double latitude, double longitude, int radius) {
    val nearCoordinates =
        format("{0}|{1}|{2}|km", nf.format(latitude), nf.format(longitude), radius);
    queryParameter.add(new SearchQueryParameter("location.near", nearCoordinates));
    queryParameter.add(new SearchQueryParameter("_sortby", "near"));
    return this;
  }

  public VzdSearchHealthcareService withMaxCount(int maxCount) {
    queryParameter.add(new SearchQueryParameter("_count", nf.format(maxCount)));
    return this;
  }

  public VzdRequestHealthcareServiceSearch build() {
    return new VzdRequestHealthcareServiceSearch(queryParameter);
  }
}
