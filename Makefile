# ==============================================================================
# IntelliJ Platform Plugin Makefile for bkmr-intellij-plugin
# ==============================================================================
# This Makefile provides convenient targets for developing, building, testing,
# and distributing the bkmr IntelliJ Platform plugin.
#
# Key Features:
# - Unit testing with custom task (bypasses kotlinx.coroutines.debug issues)
# - Multi-IDE installation support (IntelliJ, PyCharm, RustRover)
# - LSP server integration and log monitoring
# - Complete build pipeline with signing and marketplace publishing
# - Version management with GitHub release integration
#
# Testing Solution:
# The 'make test' target uses './gradlew unitTest' instead of './gradlew test'
# to avoid JVM crashes caused by kotlinx.coroutines.debug agent conflicts with
# IntelliJ Platform's bundled coroutines library. The custom unitTest task
# excludes IntelliJ Platform dependencies and disables debug agents.
#
# Quick Start:
#   make help       # Show all available targets
#   make test       # Run unit tests
#   make test-ide   # Test plugin in sandbox IDE
#   make build      # Build plugin distribution
#
# Requirements:
# - JDK 17+
# - Gradle (via gradlew)
# - bkmr-lsp binary (for LSP integration)
# - Optional: GitHub CLI (gh) for release management
# ==============================================================================

.DEFAULT_GOAL := help

# Project version from VERSION file
VERSION = $(shell cat VERSION)

# Shell configuration for multi-line commands
SHELL = bash
.ONESHELL:

################################################################################
# Development \
DEVELOPMENT::  ## ##################################################################

.PHONY: init
init:  ## initialize development environment (clear sandbox logs)
	@echo "Clearing IDE sandbox logs..."
	@rm -fv build/idea-sandbox/*/log/idea.log 2>/dev/null || true
	@echo "Development environment initialized"

.PHONY: test
test:  ## run unit tests (custom task bypassing IntelliJ Platform issues)
	@echo "Running unit tests..."
	./gradlew unitTest

.PHONY: test-ide
test-ide:  ## run plugin in sandbox IDE for integration testing
	@echo "Launching IntelliJ IDEA with plugin in sandbox..."
	./gradlew runIde

.PHONY: log-plugin
log-plugin:  ## view plugin logs from sandbox IDE (filtered for completion events)
	@echo "Tailing sandbox IDE logs (filtering for completion events)..."
	@if [ ! -f build/idea-sandbox/*/log/idea.log ]; then \
		echo "No sandbox logs found. Run 'make test-ide' first."; \
		exit 1; \
	fi
	tail -f build/idea-sandbox/*/log/idea.log | grep -u completion

.PHONY: log-plugin-raw
log-plugin-raw:  ## view raw plugin logs from sandbox IDE (no filtering)
	@echo "Tailing raw sandbox IDE logs..."
	@if [ ! -f build/idea-sandbox/*/log/idea.log ]; then \
		echo "No sandbox logs found. Run 'make test-ide' first."; \
		exit 1; \
	fi
	tail -f build/idea-sandbox/*/log/idea.log

.PHONY: log-plugin-intellij
log-plugin-intellij:  ## view plugin logs from installed IntelliJ IDEA
	@echo "Tailing IntelliJ IDEA logs..."
	@LOG_FILE="$(HOME)/Library/Logs/JetBrains/IntelliJIdea2025.1/idea.log"; \
	if [ ! -f "$$LOG_FILE" ]; then \
		echo "IntelliJ logs not found at $$LOG_FILE"; \
		echo "Try starting IntelliJ IDEA first."; \
		exit 1; \
	fi; \
	tail -f "$$LOG_FILE"

################################################################################
# Installation \
INSTALLATION:  ## ##################################################################

.PHONY: install-local
install-local: build  ## install plugin in all local JetBrains IDEs
	@$(MAKE) install-intellij
	@$(MAKE) install-rustover
	@$(MAKE) install-pycharm

.PHONY: install-intellij
install-intellij: build  ## install plugin in IntelliJ IDEA
	@echo "Installing plugin in IntelliJ IDEA..."
	@PLUGIN_ZIP=$$(find build/distributions -name "*.zip" | head -1); \
	if [ -z "$$PLUGIN_ZIP" ]; then \
		echo "Error: Plugin ZIP not found. Run 'make build' first."; \
		exit 1; \
	fi; \
	for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/IntelliJIdea*/plugins; do \
		if [ -d "$$plugins_dir" ]; then \
			echo "Installing to $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
			mkdir -p "$$plugins_dir/com.sysid.bkmr"; \
			unzip -j "$$PLUGIN_ZIP" "bkmr-intellij-plugin/lib/*" -d "$$plugins_dir/com.sysid.bkmr/lib/"; \
		fi; \
	done

.PHONY: install-rustover
install-rustover: build  ## install plugin in RustRover
	@echo "Installing plugin in RustRover..."
	@PLUGIN_ZIP=$$(find build/distributions -name "*.zip" | head -1); \
	if [ -z "$$PLUGIN_ZIP" ]; then \
		echo "Error: Plugin ZIP not found. Run 'make build' first."; \
		exit 1; \
	fi; \
	for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/RustRover*/plugins; do \
		if [ -d "$$plugins_dir" ]; then \
			echo "Installing to $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
			mkdir -p "$$plugins_dir/com.sysid.bkmr"; \
			unzip -j "$$PLUGIN_ZIP" "bkmr-intellij-plugin/lib/*" -d "$$plugins_dir/com.sysid.bkmr/lib/"; \
		fi; \
	done

.PHONY: install-pycharm
install-pycharm: build  ## install plugin in PyCharm
	@echo "Installing plugin in PyCharm..."
	@PLUGIN_ZIP=$$(find build/distributions -name "*.zip" | head -1); \
	if [ -z "$$PLUGIN_ZIP" ]; then \
		echo "Error: Plugin ZIP not found. Run 'make build' first."; \
		exit 1; \
	fi; \
	for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/PyCharm*/plugins; do \
		if [ -d "$$plugins_dir" ]; then \
			echo "Installing to $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
			mkdir -p "$$plugins_dir/com.sysid.bkmr"; \
			unzip -j "$$PLUGIN_ZIP" "bkmr-intellij-plugin/lib/*" -d "$$plugins_dir/com.sysid.bkmr/lib/"; \
		fi; \
	done

.PHONY: uninstall-local
uninstall-local:  ## uninstall plugin from all local JetBrains IDEs
	@$(MAKE) uninstall-intellij
	@$(MAKE) uninstall-rustover
	@$(MAKE) uninstall-pycharm

.PHONY: uninstall-intellij
uninstall-intellij:  ## uninstall plugin from IntelliJ IDEA
	@echo "Uninstalling plugin from IntelliJ IDEA..."
	@for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/IntelliJIdea*/plugins; do \
		if [ -d "$$plugins_dir/com.sysid.bkmr" ]; then \
			echo "Removing from $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
		fi; \
	done

.PHONY: uninstall-rustover
uninstall-rustover:  ## uninstall plugin from RustRover
	@echo "Uninstalling plugin from RustRover..."
	@for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/RustRover*/plugins; do \
		if [ -d "$$plugins_dir/com.sysid.bkmr" ]; then \
			echo "Removing from $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
		fi; \
	done

.PHONY: uninstall-pycharm
uninstall-pycharm:  ## uninstall plugin from PyCharm
	@echo "Uninstalling plugin from PyCharm..."
	@for plugins_dir in "$$HOME/Library/Application Support/JetBrains"/PyCharm*/plugins; do \
		if [ -d "$$plugins_dir/com.sysid.bkmr" ]; then \
			echo "Removing from $$plugins_dir"; \
			rm -rf "$$plugins_dir/com.sysid.bkmr"; \
		fi; \
	done

################################################################################
# Building \
BUILDING:  ## ##################################################################

.PHONY: all
all: clean test build sign  ## build complete plugin pipeline (clean → test → build → sign)
	@echo "Plugin build pipeline completed successfully"

.PHONY: build
build:  ## build plugin distribution ZIP
	@echo "Building plugin..."
	./gradlew buildPlugin
	@echo "Plugin built successfully at: $$(find build/distributions -name '*.zip' | head -1)"

.PHONY: compile
compile:  ## compile plugin sources without building distribution
	@echo "Compiling plugin sources..."
	./gradlew compileKotlin

.PHONY: clean
clean:  ## clean all build artifacts and caches
	@echo "Cleaning build artifacts..."
	./gradlew clean
	@echo "Build artifacts cleaned"

.PHONY: sign
sign:  ## sign plugin for distribution (requires certificates)
	@echo "Signing plugin..."
	@if [ -z "$$CERTIFICATE_CHAIN" ] || [ -z "$$PRIVATE_KEY" ]; then \
		echo "Warning: CERTIFICATE_CHAIN and PRIVATE_KEY environment variables not set"; \
		echo "Plugin will be built unsigned."; \
	fi
	./gradlew signPlugin

.PHONY: publish
publish:  ## publish plugin to JetBrains Marketplace (requires token)
	@echo "Publishing plugin to JetBrains Marketplace..."
	@if [ -z "$$JETBRAINS_MARKETPLACE_TOKEN" ]; then \
		echo "Error: JETBRAINS_MARKETPLACE_TOKEN environment variable not set"; \
		exit 1; \
	fi
	./gradlew publishPlugin

################################################################################
# Version Management \
VERSION_MGMT:  ## ##################################################################

.PHONY: bump-patch
bump-patch: check-github-token  ## bump patch version (1.0.0 → 1.0.1)
	bump-my-version bump patch
	git push
	git push --tags
	@$(MAKE) create-release

.PHONY: bump-minor
bump-minor: check-github-token  ## bump minor version (1.0.0 → 1.1.0)
	bump-my-version bump minor
	git push
	git push --tags
	@$(MAKE) create-release

.PHONY: bump-major
bump-major: check-github-token  ## bump major version (1.0.0 → 2.0.0)
	bump-my-version bump major
	git push
	git push --tags
	@$(MAKE) create-release

.PHONY: create-release
create-release: check-github-token  ## create GitHub release
	@if ! command -v gh &>/dev/null; then \
		echo "GitHub CLI (gh) not installed. Please create release manually."; \
		exit 1; \
	else \
		echo "Creating GitHub release for v$(VERSION)"; \
		gh release create "v$(VERSION)" --generate-notes --latest; \
	fi

.PHONY: check-github-token
check-github-token:  ## check if GITHUB_TOKEN is set
	@if [ -z "$$GITHUB_TOKEN" ]; then \
		echo "GITHUB_TOKEN not set. Please export your GitHub token."; \
		exit 1; \
	fi

################################################################################
# LSP Integration \
LSP_INTEGRATION:  ## ##################################################################

.PHONY: log-lsp
log-lsp:  ## view LSP server logs (from bkmr-lsp project)
	@echo "Viewing LSP server logs..."
	@if [ -f "/tmp/lsp-bkmr.log" ]; then \
		tail -f /tmp/lsp-bkmr.log; \
	else \
		echo "LSP log file not found at /tmp/lsp-bkmr.log"; \
		echo "Make sure bkmr-lsp server is running and configured for logging."; \
	fi

.PHONY: log-lsp-err
log-lsp-err:  ## view LSP server error logs
	@echo "Viewing LSP server error logs..."
	@if [ -f "/tmp/lsp-bkmr-err.log" ]; then \
		tail -f /tmp/lsp-bkmr-err.log; \
	else \
		echo "LSP error log file not found at /tmp/lsp-bkmr-err.log"; \
		echo "Check if bkmr-lsp server is configured for error logging."; \
	fi

################################################################################
# Utilities \
UTILITIES:  ## ##################################################################

.PHONY: deps
deps:  ## show plugin dependencies
	@echo "Showing plugin dependencies..."
	./gradlew dependencies --configuration runtimeClasspath

.PHONY: check
check:  ## run all checks (compile + test + build verification)
	@echo "Running all checks..."
	@$(MAKE) compile
	@$(MAKE) test
	@$(MAKE) build
	@echo "All checks passed successfully"

.PHONY: info
info:  ## show project information
	@echo "=== Plugin Information ==="
	@echo "Version: $(VERSION)"
	@echo "Gradle version: $$(./gradlew --version | grep 'Gradle' | head -1)"
	@echo "Kotlin version: $$(./gradlew --version | grep 'Kotlin' | head -1)"
	@echo "JVM version: $$(./gradlew --version | grep 'JVM:' | head -1)"
	@echo ""
	@echo "=== Build Status ==="
	@if [ -d "build/distributions" ] && [ -n "$$(find build/distributions -name '*.zip' 2>/dev/null)" ]; then \
		echo "Plugin ZIP: $$(find build/distributions -name '*.zip' | head -1)"; \
		echo "Size: $$(du -h $$(find build/distributions -name '*.zip' | head -1) | cut -f1)"; \
	else \
		echo "Plugin ZIP: Not built (run 'make build')"; \
	fi

define PRINT_HELP_PYSCRIPT
import re, sys

for line in sys.stdin:
	match = re.match(r'^([%a-zA-Z0-9_-]+):.*?## (.*)$$', line)
	if match:
		target, help = match.groups()
		if target != "dummy":
			print("\033[36m%-20s\033[0m %s" % (target, help))
endef
export PRINT_HELP_PYSCRIPT

.PHONY: help
help:  ## show this help
	@python -c "$$PRINT_HELP_PYSCRIPT" < $(MAKEFILE_LIST)
