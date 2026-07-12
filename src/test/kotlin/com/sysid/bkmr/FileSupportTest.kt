// File: src/test/kotlin/com/sysid/bkmr/FileSupportTest.kt
package com.sysid.bkmr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FileSupportTest {

    @Nested
    inner class SupportedExtensions {

        @Test
        fun `text file extensions are supported`() {
            assertTrue(FileSupport.isSupportedExtension("kt"))
            assertTrue(FileSupport.isSupportedExtension("rs"))
            assertTrue(FileSupport.isSupportedExtension("md"))
            assertTrue(FileSupport.isSupportedExtension("txt"))
        }

        @Test
        fun `binary file extensions are not supported`() {
            assertFalse(FileSupport.isSupportedExtension("png"))
            assertFalse(FileSupport.isSupportedExtension("zip"))
            assertFalse(FileSupport.isSupportedExtension("jar"))
            assertFalse(FileSupport.isSupportedExtension("dmg"))
        }

        @Test
        fun `svg is text (XML), not binary`() {
            assertTrue(FileSupport.isSupportedExtension("svg"))
        }

        @Test
        fun `extension check is case-insensitive`() {
            assertFalse(FileSupport.isSupportedExtension("PNG"))
            assertTrue(FileSupport.isSupportedExtension("KT"))
        }

        @Test
        fun `files without extension are supported`() {
            assertTrue(FileSupport.isSupportedExtension(null))
            assertTrue(FileSupport.isSupportedExtension(""))
        }
    }

    @Nested
    inner class CommentFormatting {

        @Test
        fun `c-style languages use double slash`() {
            assertEquals("// src/main.rs", FileSupport.formatComment("rs", "src/main.rs"))
            assertEquals("// App.kt", FileSupport.formatComment("kt", "App.kt"))
        }

        @Test
        fun `shell-style languages use hash`() {
            assertEquals("# run.sh", FileSupport.formatComment("sh", "run.sh"))
            assertEquals("# main.py", FileSupport.formatComment("py", "main.py"))
        }

        @Test
        fun `xml family uses block comment with closing marker`() {
            assertEquals("<!-- index.html -->", FileSupport.formatComment("html", "index.html"))
            assertEquals("<!-- icon.svg -->", FileSupport.formatComment("svg", "icon.svg"))
        }

        @Test
        fun `css family uses c-style block comment`() {
            assertEquals("/* style.css */", FileSupport.formatComment("css", "style.css"))
        }

        @Test
        fun `sql and lua use double dash`() {
            assertEquals("-- schema.sql", FileSupport.formatComment("sql", "schema.sql"))
            assertEquals("-- init.lua", FileSupport.formatComment("lua", "init.lua"))
        }

        @Test
        fun `unknown extensions default to hash`() {
            assertEquals("# data.unknownext", FileSupport.formatComment("unknownext", "data.unknownext"))
            assertEquals("# README", FileSupport.formatComment(null, "README"))
        }

        @Test
        fun `extension lookup is case-insensitive`() {
            assertEquals("// Main.KT", FileSupport.formatComment("KT", "Main.KT"))
        }
    }

    @Nested
    inner class Insertion {

        private val bom = 0xFEFF.toChar().toString()

        @Test
        fun `plain file inserts comment at offset zero`() {
            val insertion = FileSupport.buildInsertion("fun main() {}\n", "// app.kt")

            assertEquals(0, insertion?.offset)
            assertEquals("// app.kt\n", insertion?.text)
        }

        @Test
        fun `empty document inserts at offset zero`() {
            val insertion = FileSupport.buildInsertion("", "# run.sh")

            assertEquals(0, insertion?.offset)
            assertEquals("# run.sh\n", insertion?.text)
        }

        @Test
        fun `shebang line stays first - comment goes on the next line`() {
            val document = "#!/bin/bash\necho hi\n"

            val insertion = FileSupport.buildInsertion(document, "# run.sh")

            assertEquals("#!/bin/bash\n".length, insertion?.offset)
            assertEquals("# run.sh\n", insertion?.text)
        }

        @Test
        fun `shebang without trailing newline gets one prepended to the comment`() {
            val document = "#!/bin/bash"

            val insertion = FileSupport.buildInsertion(document, "# run.sh")

            assertEquals(document.length, insertion?.offset)
            assertEquals("\n# run.sh\n", insertion?.text)
        }

        @Test
        fun `BOM stays first - comment inserted after it`() {
            val document = bom + "fun main() {}\n"

            val insertion = FileSupport.buildInsertion(document, "// app.kt")

            assertEquals(1, insertion?.offset)
            assertEquals("// app.kt\n", insertion?.text)
        }

        @Test
        fun `BOM plus shebang - comment inserted after the shebang line`() {
            val document = bom + "#!/usr/bin/env python\nprint(1)\n"

            val insertion = FileSupport.buildInsertion(document, "# main.py")

            assertEquals((bom + "#!/usr/bin/env python\n").length, insertion?.offset)
            assertEquals("# main.py\n", insertion?.text)
        }

        @Test
        fun `duplicate comment in first lines yields no insertion`() {
            val document = "// app.kt\nfun main() {}\n"

            assertNull(FileSupport.buildInsertion(document, "// app.kt"))
        }

        @Test
        fun `duplicate comment after shebang yields no insertion`() {
            val document = "#!/bin/bash\n# run.sh\necho hi\n"

            assertNull(FileSupport.buildInsertion(document, "# run.sh"))
        }

        @Test
        fun `same path in a different comment position is not a duplicate`() {
            val document = "fun main() {\n    println(\"// app.kt\")\n}\n// app.kt is mentioned here\n"

            val insertion = FileSupport.buildInsertion(document, "// app.kt")

            assertEquals(0, insertion?.offset)
        }

        @Test
        fun `crlf shebang line is handled`() {
            val document = "#!/bin/bash\r\necho hi\r\n"

            val insertion = FileSupport.buildInsertion(document, "# run.sh")

            assertEquals("#!/bin/bash\r\n".length, insertion?.offset)
            assertEquals("# run.sh\n", insertion?.text)
        }
    }
}
