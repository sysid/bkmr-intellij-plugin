#!/bin/bash
# Provision the JDK 21 toolchain required by build.gradle.kts (jvmToolchain(21))
# in Claude Code on the web sessions.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Architecture-independent check (the JDK directory name ends in -amd64/-arm64)
if ! dpkg -s openjdk-21-jdk-headless >/dev/null 2>&1; then
  export DEBIAN_FRONTEND=noninteractive
  # Try from the existing package index first; refresh it only if needed.
  # `apt-get update` can exit non-zero when third-party PPAs are unreachable.
  if ! apt-get install -y -q openjdk-21-jdk-headless >/dev/null; then
    apt-get update -q >/dev/null || true
    apt-get install -y -q openjdk-21-jdk-headless >/dev/null
  fi
fi
# Gradle's toolchain auto-detection scans /usr/lib/jvm, so no extra config is needed.
