#!/bin/sh


#docker create --name bbriccs_docs eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest /document/user_manual.adoc
#docker cp ../adocs bbriccs_docs:/document/
#docker start --attach bbriccs_docs
#mkdir ./target
#docker cp bbriccs_docs:/document/user_manual.pdf ./target/
#docker rm bbriccs_docs

docker run --rm -it  -e PLANTUML_URL="https://plantuml.gematik.solutions/plantuml/" -v .:/document/ eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest /document/user_manual.adoc gematik
