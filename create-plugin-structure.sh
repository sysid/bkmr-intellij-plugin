#!/bin/bash

# Create bkmr-intellij-plugin directory structure
mkdir -p bkmr-intellij-plugin/{gradle/wrapper,.github/workflows}

# Main source directories
mkdir -p bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/{lsp,completion,actions,settings,icons,toolwindow,documentation}
mkdir -p bkmr-intellij-plugin/src/main/resources/{META-INF,icons,messages}
mkdir -p bkmr-intellij-plugin/src/test/kotlin/com/sysid/bkmr

# Create placeholder files
touch bkmr-intellij-plugin/{build.gradle.kts,gradle.properties,settings.gradle.kts}
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/BkmrPlugin.kt
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/lsp/{BkmrLspClient.kt,BkmrLspServerDescriptor.kt,BkmrLspSupportProvider.kt}
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/completion/{BkmrCompletionContributor.kt,BkmrLookupElement.kt}
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/actions/{OpenSnippetAction.kt,RefreshSnippetsAction.kt,SearchSnippetsAction.kt}
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/settings/{BkmrSettings.kt,BkmrConfigurable.kt}
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/icons/BkmrIcons.kt
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/toolwindow/BkmrToolWindow.kt
touch bkmr-intellij-plugin/src/main/kotlin/com/sysid/bkmr/documentation/BkmrDocumentationProvider.kt
touch bkmr-intellij-plugin/src/main/resources/META-INF/plugin.xml
touch bkmr-intellij-plugin/src/main/resources/messages/BkmrBundle.properties
touch bkmr-intellij-plugin/src/test/kotlin/com/sysid/bkmr/BkmrPluginTest.kt
touch bkmr-intellij-plugin/.github/workflows/build.yml

echo "Directory structure created successfully!"
tree bkmr-intellij-plugin
