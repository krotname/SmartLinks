#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"
/opt/arc-cache/arc-cache oci-pull --image eclipse-temurin:25-jre@sha256:7ea65de6187ad8fbcc0ad155950c38664a7371148bb3ccf1ec1e1b286b44ad08 --output "$RUNNER_TEMP/smartlinks-runtime"
digest=$(jq -er '.manifests[0].digest' "$RUNNER_TEMP/smartlinks-runtime/index.json")
[[ "$digest" =~ ^sha256:[a-f0-9]{64}$ ]]
printf 'RUNTIME_OCI_CONTEXT=oci-layout://%s@%s\n' "$RUNNER_TEMP/smartlinks-runtime" "$digest" >> "$GITHUB_ENV"
