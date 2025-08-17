package com.sysid.bkmr.utils

/**
 * Utility functions for testing the bkmr IntelliJ plugin (no IntelliJ Platform dependencies)
 */
object TestUtils {

    /**
     * Common file extensions for testing
     */
    object FileExtensions {
        val TEXT_FILES = listOf(
            "txt", "md", "rst", "yaml", "yml", "json", "xml", "html", "css",
            "js", "ts", "py", "java", "kt", "rs", "go", "c", "cpp", "h", "hpp",
            "sh", "ps1", "vim", "lua", "sql", "r", "rb", "pl", "scala", "swift"
        )

        val BINARY_FILES = listOf(
            "jpg", "jpeg", "png", "gif", "bmp", "ico", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "tar", "gz", "rar", "7z", "bz2",
            "exe", "dll", "so", "dylib", "app", "dmg",
            "mp3", "mp4", "avi", "mov", "wav", "flac",
            "class", "jar", "war", "ear"
        )
    }

    /**
     * Common comment syntax mappings for testing
     */
    object CommentSyntax {
        val C_STYLE = mapOf(
            "rs" to "//", "java" to "//", "kt" to "//", "js" to "//", "ts" to "//",
            "cpp" to "//", "c" to "//", "go" to "//", "swift" to "//", "scala" to "//", "dart" to "//"
        )

        val SHELL_STYLE = mapOf(
            "py" to "#", "sh" to "#", "yaml" to "#", "yml" to "#", "toml" to "#",
            "rb" to "#", "pl" to "#", "r" to "#", "ps1" to "#"
        )

        val HTML_STYLE = mapOf(
            "html" to "<!--", "xml" to "<!--", "svg" to "<!--"
        )

        val CSS_STYLE = mapOf(
            "css" to "/*", "scss" to "/*", "sass" to "/*", "less" to "/*"
        )

        val SQL_STYLE = mapOf(
            "sql" to "--", "lua" to "--", "hs" to "--"
        )

        val OTHER = mapOf(
            "lisp" to ";", "cl" to ";", "el" to ";",
            "vim" to "\"",
            "bat" to "REM",
            "f90" to "!",
            "m" to "%",
            "tex" to "%"
        )

        val ALL_MAPPINGS = C_STYLE + SHELL_STYLE + HTML_STYLE + CSS_STYLE + SQL_STYLE + OTHER
    }

    /**
     * Test data for LSP integration
     */
    object LspTestData {
        fun createMockLspResponse(snippets: List<String>): String {
            return """
                [
                    ${snippets.joinToString(",") { snippet ->
                        """
                        {
                            "id": 1,
                            "title": "$snippet snippet",
                            "url": "$snippet content",
                            "description": "Test $snippet snippet",
                            "tags": ["_snip_", "test"],
                            "access_count": 0
                        }
                        """.trimIndent()
                    }}
                ]
            """.trimIndent()
        }

        val SAMPLE_INITIALIZATION_OPTIONS = mapOf(
            "bkmr" to mapOf(
                "maxCompletions" to 50
            )
        )
    }

    /**
     * Create a relative path from project root
     */
    fun createRelativePath(projectPath: String, filePath: String): String {
        return if (filePath.startsWith(projectPath)) {
            filePath.removePrefix("$projectPath/")
        } else {
            filePath
        }
    }

    /**
     * Validate that a file should be supported by the plugin
     */
    fun shouldSupportFile(filename: String): Boolean {
        if (filename.contains("/") && filename.split("/").last().isEmpty()) {
            return false // Directory-like
        }

        val extension = filename.substringAfterLast('.', "").lowercase()
        return extension !in FileExtensions.BINARY_FILES
    }
}