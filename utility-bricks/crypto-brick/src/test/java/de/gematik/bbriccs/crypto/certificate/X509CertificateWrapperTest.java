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

package de.gematik.bbriccs.crypto.certificate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.naming.InvalidNameException;
import javax.security.auth.x500.X500Principal;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class X509CertificateWrapperTest {

  private static X509Certificate testCert;

  @SneakyThrows
  @BeforeAll
  public static void setup() {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    val keyPair = keyPairGenerator.generateKeyPair();

    val notBefore = new Date();
    val notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L); // 1 year

    val issuer = new X500Name("CN=Issuer");
    val subject = new X500Name("CN=Subject");
    val serial = BigInteger.valueOf(System.currentTimeMillis());

    val certBuilder =
        new JcaX509v3CertificateBuilder(
            issuer, serial, notBefore, notAfter, subject, keyPair.getPublic());
    val basicConstraints = new BasicConstraints(false); // true if it's a CA certificate
    certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);

    val signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
    byte[] certBytes = certBuilder.build(signer).getEncoded();
    val certificateFactory = CertificateFactory.getInstance("X.509");
    testCert =
        (X509Certificate)
            certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
  }

  @SneakyThrows
  @Test
  void shouldProcessPEMFormat() {
    val stringWriter = new StringWriter();
    try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
      pemWriter.writeObject(testCert);
    }
    val pem = stringWriter.toString();
    assertDoesNotThrow(() -> X509CertificateWrapper.fromPem(pem));
  }

  @Test
  void shouldThrowOnInvalidPem() {
    val pem = "hello world";
    assertThrows(CertificateException.class, () -> X509CertificateWrapper.fromPem(pem));
  }

  @SneakyThrows
  @Test
  @DisplayName("should return issuer CN when present")
  void shouldReturnIssuerCNWhenPresent() {
    val mockCert = mock(X509Certificate.class);
    when(mockCert.getIssuerX500Principal())
        .thenReturn(new X500Principal("CN=Test, O=Test Org, C=DE"));
    when(mockCert.getEncoded()).thenReturn(testCert.getEncoded());
    val wrapper = new X509CertificateWrapper(mockCert);
    val issuerCN = wrapper.getIssuerCN();
    assertTrue(issuerCN.isPresent());
    assertEquals("Test", issuerCN.get());
  }

  @SneakyThrows
  @Test
  @DisplayName("should throw if certificate principal contains invalid LDAP")
  void shouldThrowOnInvalidLdap() {
    val mockCert = mock(X509Certificate.class);
    val mockPrincipal = mock(X500Principal.class);
    when(mockCert.getIssuerX500Principal()).thenReturn(mockPrincipal);
    when(mockPrincipal.getName()).thenReturn("hello world");
    when(mockCert.getEncoded()).thenReturn(testCert.getEncoded());
    val wrapper = new X509CertificateWrapper(mockCert);
    assertThrows(InvalidNameException.class, wrapper::getIssuerCN);
  }

  @SneakyThrows
  @Test
  @DisplayName("should return empty when issuer CN not present")
  void shouldReturnEmptyWhenIssuerCNNotPresent() {
    val mockCert = mock(X509Certificate.class);
    when(mockCert.getIssuerX500Principal()).thenReturn(new X500Principal("O=Test Org, C=DE"));
    when(mockCert.getEncoded()).thenReturn(testCert.getEncoded());
    val wrapper = new X509CertificateWrapper(mockCert);
    val issuerCN = wrapper.getIssuerCN();
    assertFalse(issuerCN.isPresent());
  }
}
