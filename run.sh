#!/bin/sh

docker run -p 5002:8080 \
  -v $PWD/sample-config/hxp-idp-config.json:/app/identity-resources.json \
  -e IdpConfiguration__Host__RequireHttps=false \
  ghcr.io/hylandsoftware/hxp/iam/api.mock:latest
