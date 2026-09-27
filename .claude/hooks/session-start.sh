#!/bin/bash
# Provision the JDK 17 toolchain required by build.gradle.kts (jvmToolchain(17))
# in Claude Code on the web sessions.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

JDK_DIR=/usr/lib/jvm/java-17-openjdk-amd64

if [ ! -x "$JDK_DIR/bin/javac" ]; then
  export DEBIAN_FRONTEND=noninteractive
  # Try from the existing package index first; refresh it only if needed.
  # `apt-get update` can exit non-zero when third-party PPAs are unreachable.
  if ! apt-get install -y -q openjdk-17-jdk-headless >/dev/null; then
    apt-get update -q >/dev/null || true
    apt-get install -y -q openjdk-17-jdk-headless >/dev/null
  fi
fi
# Gradle's toolchain auto-detection scans /usr/lib/jvm, so no extra config is needed.
