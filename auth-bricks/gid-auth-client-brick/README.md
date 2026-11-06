# gid-auth-brick

Das Modul kapselt den GesundheitsID-/GSI-Login für Versicherte auf Basis einer `Egk`.

## Aktuelle API

- `GidAuthenticationConfig` per Builder für
  - `fedAuthEndpoint`
  - `tokenEndpoint`
  - `sekIdpIss`
  - `clientId`
  - `redirectUri`
  - `scope` (mindestens ein Wert, z. B. `Scope.openId(...)`)
  - PKCE `codeChallenge` und `codeVerifier`
  - `keyVerifier` (optional, Default: `codeVerifier`)
  - `tokenKey` (Base64-URL-String oder `byte[]`)
- `GidClient.create(config).initialize()`
- `login(Egk egk)`

`build()` prüft alle Pflichtwerte und wirft eine `GidClientConfigurationException`, die
sämtliche fehlenden Werte auf einmal benennt.

## Beispiel

```java
var config = GidAuthenticationConfig.builder()
    .fedAuthEndpoint("https://idp-test.app.ti-dienste.de/fedauth")
    .tokenEndpoint("https://idp-test.app.ti-dienste.de/token")
    .sekIdpIss("https://gsi.dev.gematik.solutions")
    .clientId("eRezeptApp")
    .redirectUri("https://redirect.example.org/callback")
    .scope(Scope.openId(GidScopeErp.E_REZEPT))
    .codeChallenge("<code_challenge>")
    .codeVerifier("<code_verifier>")
    .tokenKey("<base64url token key>")
    .build();

var result = GidClient.create(config)
    .initialize()
    .login(egk);

var nestedJwt = result.getNestedJwt();
```

## Wiederverwendung vorhandener Bricks

- `idp-flow-api-brick`: Redirect-/Query-Parsing via `RedirectLocation`
- `jwt-brick`: Access-Token-Auswertung und JWE-Entschlüsselung via `JweTokenParser`

