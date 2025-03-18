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

package de.gematik.bbriccs.konnektor;

import static java.text.MessageFormat.format;

import com.sun.xml.ws.developer.JAXWSProperties;
import de.gematik.bbriccs.cfg.dto.BasicAuthConfiguration;
import de.gematik.bbriccs.cfg.dto.TLSConfiguration;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardService;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalService;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateService;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionService;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventService;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.servicedirectory.ConnectorServices;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureService;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDService;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import jakarta.annotation.Nullable;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.ws.BindingProvider;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RemoteKonServicePort extends ServicePort {

  private final URL baseUrl;
  @Nullable private final TrustProvider trustProvider;
  private final HostnameVerifier hostnameVerifier;
  @Nullable private final String username;
  @Nullable private final String password;

  private RemoteKonServicePort(Builder builder) {
    super(builder.sds);
    this.baseUrl = builder.baseUrl;
    this.trustProvider = builder.trustProvider;
    this.hostnameVerifier = builder.hostnameVerifier;
    this.username = builder.userName;
    this.password = builder.password;
  }

  @SneakyThrows
  public static Builder onRemote(String url) {
    return onRemote(new URL(url));
  }

  public static Builder onRemote(URL baseUrl) {
    return new Builder(baseUrl);
  }

  @Override
  public final SignatureServicePortType getSignatureService() {
    val service = new SignatureService();
    val servicePort = service.getSignatureServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getSignatureService());
    return servicePort;
  }

  @Override
  public final AuthSignatureServicePortType getAuthSignatureService() {
    val service = new AuthSignatureService();
    val servicePort = service.getAuthSignatureServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getAuthSignatureService());
    return servicePort;
  }

  @Override
  public final CertificateServicePortType getCertificateService() {
    val service = new CertificateService();
    val servicePort = service.getCertificateServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getCertificateService());
    return servicePort;
  }

  @Override
  public final EventServicePortType getEventService() {
    val service = new EventService();
    val servicePort = service.getEventServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getEventService());
    return servicePort;
  }

  @Override
  public final CardServicePortType getCardService() {
    val service = new CardService();
    val servicePort = service.getCardServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getCardService());
    return servicePort;
  }

  @Override
  public final CardTerminalServicePortType getCardTerminalService() {
    val service = new CardTerminalService();
    val servicePort = service.getCardTerminalServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getEventService());
    return servicePort;
  }

  @Override
  public VSDServicePortType getVSDServicePortType() {
    val service = new VSDService();
    val servicePort = service.getVSDServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getVsdService());
    return servicePort;
  }

  @Override
  public EncryptionServicePortType getEncryptionServicePortType() {
    val service = new EncryptionService();
    val servicePort = service.getEncryptionServicePort();
    setEndpointAddress((BindingProvider) servicePort, this.getSds().getEncryptionService());
    return servicePort;
  }

  @SuppressWarnings("java:S1874")
  private void setEndpointAddress(BindingProvider servicePort, String path) {
    log.info("Prepare ServicePort {} for {}", servicePort, path);
    servicePort.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, path);

    if (this.username != null) {
      servicePort.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, this.username);
    }
    if (this.password != null) {
      servicePort.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, this.password);
    }

    if (trustProvider != null) {
      servicePort
          .getRequestContext()
          .put(JAXWSProperties.SSL_SOCKET_FACTORY, trustProvider.getSocketFactory());

      servicePort.getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, this.hostnameVerifier);
    }
  }

  @Override
  public String toString() {
    return format("{0} at {1}", this.getSds().getProductName(), this.baseUrl);
  }

  public static class Builder {
    private final URL baseUrl;
    private KonnektorServiceDefinition sds;
    private String userName;
    private String password;
    @Nullable private TrustProvider trustProvider;

    // no HostNameVerification by default because of KonSim
    private HostnameVerifier hostnameVerifier = (hostname, session) -> true; // NOSONAR

    private Builder(URL baseUrl) {
      this.baseUrl = baseUrl;
    }

    public Builder tls(TLSConfiguration tlsConfig) {
      Optional.ofNullable(tlsConfig).ifPresent(tls -> this.trustProvider(TrustProvider.from(tls)));
      return this;
    }

    public Builder auth(BasicAuthConfiguration auth) {
      Optional.ofNullable(auth)
          .ifPresent(basicAuth -> this.auth(basicAuth.getUsername(), basicAuth.getPassword()));
      return this;
    }

    public Builder auth(String username, String password) {
      return this.username(username).password(password);
    }

    public Builder username(String username) {
      this.userName = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder trustProvider(TrustProvider trustProvider) {
      this.trustProvider = trustProvider;
      return this;
    }

    public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = hostnameVerifier;
      return this;
    }

    public RemoteKonServicePort build() {
      this.connect();
      return new RemoteKonServicePort(this);
    }

    @SneakyThrows
    private void connect() {
      val sdsUri = this.baseUrl.toURI().getPath() + "/connector.sds";
      val sdsUrl = this.baseUrl.toURI().resolve(sdsUri).toURL();

      HttpURLConnection con;
      if (this.baseUrl.getProtocol().equalsIgnoreCase("https")) {
        Objects.requireNonNull(this.trustProvider, "HTTPS protocol without a TrustProvider given");
        val httpsCon = (HttpsURLConnection) sdsUrl.openConnection();
        httpsCon.setHostnameVerifier((arg0, arg1) -> true);
        httpsCon.setSSLSocketFactory(this.trustProvider.getSocketFactory());
        con = httpsCon;
      } else {
        con = (HttpURLConnection) sdsUrl.openConnection();
      }
      con.setRequestMethod("GET");
      con.connect();

      val jaxb = JAXBContext.newInstance(ConnectorServices.class);
      val unmarshaller = jaxb.createUnmarshaller();
      val connectorServices =
          (ConnectorServices)
              unmarshaller.unmarshal(
                  new BufferedReader(new InputStreamReader(con.getInputStream())));
      con.disconnect();
      this.sds = KonnektorServiceDefinition.from(connectorServices);
      log.info("connected to {} on {}", this.sds.getProductName(), this.baseUrl);
    }
  }
}
