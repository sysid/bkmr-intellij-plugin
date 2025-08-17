package com.sysid.bkmr.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SimpleUtilsTest {

    @Test
    fun `should support text files`() {
        shouldSupportFile("test.kt") shouldBe true
        shouldSupportFile("test.java") shouldBe true
        shouldSupportFile("test.py") shouldBe true
        shouldSupportFile("test.js") shouldBe true
        shouldSupportFile("test.txt") shouldBe true
    }

    @Test
    fun `should not support binary files`() {
        shouldSupportFile("test.jpg") shouldBe false
        shouldSupportFile("test.pdf") shouldBe false
        shouldSupportFile("test.exe") shouldBe false
        shouldSupportFile("test.zip") shouldBe false
        shouldSupportFile("test.mp3") shouldBe false
    }

    @Test
    fun `should create relative path correctly`() {
        val projectPath = "/test/project"
        val filePath = "/test/project/src/main/kotlin/Test.kt"
        val expected = "src/main/kotlin/Test.kt"
        
        createRelativePath(projectPath, filePath) shouldBe expected
    }

    @Test
    fun `should handle file path outside project`() {
        val projectPath = "/test/project"
        val filePath = "/other/path/Test.kt"
        
        createRelativePath(projectPath, filePath) shouldBe filePath
    }

    // Inline utility functions for testing (no IntelliJ Platform dependencies)
    private fun shouldSupportFile(filename: String): Boolean {
        if (filename.contains("/") && filename.split("/").last().isEmpty()) {
            return false // Directory-like
        }

        val extension = filename.substringAfterLast('.', "").lowercase()
        return extension !in TestUtils.FileExtensions.BINARY_FILES
    }

    private fun createRelativePath(projectPath: String, filePath: String): String {
        return if (filePath.startsWith(projectPath)) {
            filePath.removePrefix("$projectPath/")
        } else {
            filePath
        }
    }

    @Test
    fun `should have correct comment syntax mappings`() {
        TestUtils.CommentSyntax.C_STYLE["kt"] shouldBe "//"
        TestUtils.CommentSyntax.C_STYLE["java"] shouldBe "//"
        TestUtils.CommentSyntax.C_STYLE["rs"] shouldBe "//"
        
        TestUtils.CommentSyntax.SHELL_STYLE["py"] shouldBe "#"
        TestUtils.CommentSyntax.SHELL_STYLE["sh"] shouldBe "#"
        
        TestUtils.CommentSyntax.HTML_STYLE["html"] shouldBe "<!--"
        TestUtils.CommentSyntax.CSS_STYLE["css"] shouldBe "/*"
        TestUtils.CommentSyntax.SQL_STYLE["sql"] shouldBe "--"
    }

    @Test
    fun `should contain all expected file extensions`() {
        TestUtils.FileExtensions.TEXT_FILES.contains("kt") shouldBe true
        TestUtils.FileExtensions.TEXT_FILES.contains("java") shouldBe true
        TestUtils.FileExtensions.TEXT_FILES.contains("py") shouldBe true
        
        TestUtils.FileExtensions.BINARY_FILES.contains("jpg") shouldBe true
        TestUtils.FileExtensions.BINARY_FILES.contains("pdf") shouldBe true
        TestUtils.FileExtensions.BINARY_FILES.contains("exe") shouldBe true
    }
}