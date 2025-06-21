.DEFAULT_GOAL := help

VERSION = $(shell cat VERSION)

SHELL = bash
.ONESHELL:

################################################################################
# Development \
DEVELOPMENT::  ## ##################################################################

.PHONY: init
init:  ## initialize development environment
	@rm -fv build/idea-sandbox/*/log/idea.log 2>/dev/null || true

.PHONY: test
test:  ## run plugin in sandbox IDE
	./gradlew runIde

.PHONY: log-plugin-sandbox
log-plugin:  ## view plugin logs
	tail -f build/idea-sandbox/*/log/idea.log | grep -u completion

.PHONY: log-plugin-intellij
log-plugin-intellij:  ## log-plugin-intellij
	tail -f "$(HOME)/Library/Logs/JetBrains/IntelliJIdea2025.1/idea.log"

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
all:  clean build sign  # all: clean build sing
	:

.PHONY: build
build:  ## build plugin
	./gradlew buildPlugin

.PHONY: clean
clean:  ## clean build artifacts
	./gradlew clean

.PHONY: sign
sign:  ## sign plugin for distribution
	./gradlew signPlugin

.PHONY: publish
publish:  ## publish plugin to marketplace
	@./gradlew publishPlugin

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
# Utilities \
UTILITIES:  ## ##################################################################

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
