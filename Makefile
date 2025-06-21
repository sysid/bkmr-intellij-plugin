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

.PHONY: log-plugin
log-plugin:  ## view plugin logs
	tail -f build/idea-sandbox/*/log/idea.log | grep -u completion

################################################################################
# Building \
BUILDING:  ## ##################################################################

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
