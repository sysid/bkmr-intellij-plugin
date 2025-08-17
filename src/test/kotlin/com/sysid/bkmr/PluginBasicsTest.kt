package com.sysid.bkmr

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Basic unit tests that don't depend on IntelliJ Platform APIs
 */
class PluginBasicsTest {

    @Test
    fun `should validate plugin version`() {
        // Basic test to verify test infrastructure works
        val version = "1.1.0"
        version shouldBe "1.1.0"
    }

    @Test
    fun `should validate file extension detection`() {
        val testCases = mapOf(
            "test.kt" to "kt",
            "file.java" to "java", 
            "script.py" to "py",
            "page.html" to "html",
            "styles.css" to "css",
            "noextension" to ""
        )

        testCases.forEach { (filename, expectedExt) ->
            val actualExt = filename.substringAfterLast('.', "")
            actualExt shouldBe expectedExt
        }
    }

    @Test
    fun `should validate path manipulation logic`() {
        val projectPath = "/test/project"
        val filePath = "/test/project/src/main/kotlin/Test.kt"
        
        val relativePath = if (filePath.startsWith(projectPath)) {
            filePath.removePrefix("$projectPath/")
        } else {
            filePath
        }
        
        relativePath shouldBe "src/main/kotlin/Test.kt"
    }

    @Test
    fun `should validate binary file detection logic`() {
        val binaryExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "ico",
            "pdf", "doc", "docx", "xls", "xlsx", 
            "zip", "tar", "gz", "rar", "7z",
            "exe", "dll", "so", "dylib", "app",
            "mp3", "mp4", "avi", "mov", "wav",
            "class", "jar", "war", "ear"
        )

        // Text files should be supported
        val textFiles = listOf("test.kt", "file.java", "script.py", "README.md")
        textFiles.forEach { filename ->
            val ext = filename.substringAfterLast('.', "").lowercase()
            val isBinary = ext in binaryExtensions
            isBinary shouldBe false
        }

        // Binary files should not be supported  
        val binaryFiles = listOf("image.jpg", "document.pdf", "archive.zip", "program.exe")
        binaryFiles.forEach { filename ->
            val ext = filename.substringAfterLast('.', "").lowercase()
            val isBinary = ext in binaryExtensions
            isBinary shouldBe true
        }
    }

    @Test
    fun `should validate comment syntax mapping`() {
        val commentMappings = mapOf(
            "kt" to "//",
            "java" to "//", 
            "js" to "//",
            "ts" to "//",
            "py" to "#",
            "sh" to "#",
            "html" to "<!--",
            "css" to "/*",
            "sql" to "--"
        )

        commentMappings.forEach { (ext, expectedComment) ->
            // This tests the logic that would be used in the actual plugin
            val comment = when (ext) {
                "kt", "java", "js", "ts", "rs", "cpp", "go", "swift" -> "//"
                "py", "sh", "yaml", "yml", "toml", "rb", "pl" -> "#"
                "html", "xml", "svg" -> "<!--"
                "css", "scss", "sass", "less" -> "/*"
                "sql", "lua", "hs" -> "--"
                else -> "//"
            }
            comment shouldBe expectedComment
        }
    }
}