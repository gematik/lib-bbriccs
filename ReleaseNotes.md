# Release Notes $B^2riC^2s$


## Release 0.12.1

### FHIR-Bricks
- Implement `fhir-smaps-transformation-brick` for automatic transformation of FHIR resources based on StructureMap rulesets
- Implement `fhir-comparator-bricks` for comparing FHIR resources automatically based on FHIR rules
  - `fhir-comparator-api-brick` providing a common API for comparing FHIR resources
  - `fhir-comparator-fhirpatch-brick` providing a default implementation based on HAPI's FHIR Patch capability
  - `fhir-comparator-jsondiff-brick` providing an alternative lightweight implementation based on JSON-Patch and Jackson libraries

### RESTful-Bricks
- Extend `RequestHeaderProvider` to allow request plugins to provide multiple request headers for outgoing requests

### Build
- Update Dependencies
- Upgrade to Java 21

## Release 0.11.0
- Update from Jackson 2 to Jackson 3
- Update more critical dependencies to latest versions

## Release 0.9.0

### FHIR-Bricks
- Implement `PrePopulatedValidationSupportBrick` to enable FHIR validation with pre-populated profiles loaded from `.tgz`-Files.

## Release 0.5.0

### RESTful-Bricks
- Extend `restful-bricks` for ePA-Bricks

## Release 0.4.0

### FHIR-Bricks
- Preference for newer SID system identifiers as defaults and abandonment of old system identifiers

## Release 0.2.0

### FHIR-Bricks
- Extend `fhir-bricks` for integration with `erp-e2e-testsuite`

### Utility-Bricks
- Implement the `vsdm-check-digit-brick` for validating VSDM++ check digits

## Release 0.1.10

### FHIR-Bricks
- Extend `fhir-de-basisprofil-r4-brick` with `ASK` and `ATC` codings

### RESTful-Bricks
- Implement the `raw-http-brick` for encoding and decoding raw HTTP messages

### Configuration-Bricks
- Implement the `feature-toggle-brick` for reusable feature toggles

## Release 0.1.8
- initial implementation of the core framework
