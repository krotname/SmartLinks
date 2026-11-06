#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"
crane_dir="$RUNNER_TEMP/smartlinks-crane"
mkdir -p "$crane_dir"
curl --fail --location --retry 2 --output "$crane_dir/release.tar.gz" https://github.com/google/go-containerregistry/releases/download/v0.22.1/go-containerregistry_Linux_x86_64.tar.gz
tar -xzf "$crane_dir/release.tar.gz" -C "$crane_dir" crane
"$crane_dir/crane" version
/opt/arc-cache/arc-cache oci-pull --crane "$crane_dir/crane" --image eclipse-temurin:25-jre@sha256:ecdc0adb37bde1a281c4d245a3cd535004b1fc8322d560f5b09463b248c81df0 --output "$RUNNER_TEMP/smartlinks-runtime"
digest=$(jq -er '.manifests[0].digest' "$RUNNER_TEMP/smartlinks-runtime/index.json")
[[ "$digest" =~ ^sha256:[a-f0-9]{64}$ ]]
printf 'RUNTIME_OCI_CONTEXT=oci-layout://%s@%s\n' "$RUNNER_TEMP/smartlinks-runtime" "$digest" >> "$GITHUB_ENV"
