// File: src/main/kotlin/com/sysid/bkmr/FileSupport.kt
package com.sysid.bkmr

/**
 * Pure, platform-free file-type logic shared by the LSP provider and editor actions.
 * Kept free of IntelliJ types so it runs under the plain `unitTest` task.
 */
object FileSupport {

    private val BOM = 0xFEFF.toChar()

    /** Known binary file types — everything else is treated as text. */
    private val BINARY_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "ico",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "zip", "tar", "gz", "rar", "7z", "bz2",
        "exe", "dll", "so", "dylib", "app", "dmg",
        "mp3", "mp4", "avi", "mov", "wav", "flac",
        "class", "jar", "war", "ear",
    )

    fun isSupportedExtension(extension: String?): Boolean = extension?.lowercase() !in BINARY_EXTENSIONS

    /** Formats [text] as a line comment appropriate for the file type (no trailing newline). */
    fun formatComment(extension: String?, text: String): String = when (val prefix = commentPrefix(extension)) {
        "<!--" -> "<!-- $text -->"
        "/*" -> "/* $text */"
        else -> "$prefix $text"
    }

    /**
     * Where and what to insert so [comment] becomes the first comment line of the document:
     * after a BOM, after a `#!` shebang line, at offset 0 otherwise.
     * Returns null when the comment is already present in the document's first lines.
     */
    fun buildInsertion(documentText: String, comment: String): Insertion? {
        if (isDuplicateComment(documentText, comment)) {
            return null
        }

        val afterBom = if (documentText.startsWith(BOM)) 1 else 0
        val body = documentText.substring(afterBom)

        if (!body.startsWith("#!")) {
            return Insertion(afterBom, comment + "\n")
        }

        val newlineIndex = body.indexOf('\n')
        return if (newlineIndex >= 0) {
            Insertion(afterBom + newlineIndex + 1, comment + "\n")
        } else {
            // Shebang is the whole document — append the comment on a new line
            Insertion(documentText.length, "\n" + comment + "\n")
        }
    }

    data class Insertion(val offset: Int, val text: String)

    private fun isDuplicateComment(documentText: String, comment: String): Boolean = documentText.lineSequence()
        .take(HEAD_LINES_CHECKED_FOR_DUPLICATES)
        .any { it.trimStart(BOM).trimEnd('\r').trim() == comment }

    private const val HEAD_LINES_CHECKED_FOR_DUPLICATES = 3

    private fun commentPrefix(extension: String?): String = when (extension?.lowercase()) {
        // C-style languages
        "rs", "c", "cpp", "cc", "cxx", "h", "hpp", "java", "js", "ts", "jsx",
        "tsx", "cs", "go", "swift", "kt", "scala", "dart",
        -> "//"

        // Shell-style languages
        "sh", "bash", "zsh", "fish", "py", "rb", "pl", "r", "yaml", "yml", "toml",
        "cfg", "ini", "properties",
        -> "#"

        // HTML/XML
        "html", "htm", "xml", "xhtml", "svg" -> "<!--"

        // CSS
        "css", "scss", "sass", "less" -> "/*"

        // SQL, Lua, Haskell
        "sql", "lua", "hs" -> "--"

        // Lisp family
        "lisp", "cl", "clj", "cljs", "scm", "rkt" -> ";"

        // VimScript
        "vim" -> "\""

        // Batch files
        "bat", "cmd" -> "REM"

        // PowerShell
        "ps1", "psm1", "psd1" -> "#"

        // LaTeX
        "tex", "latex" -> "%"

        // Fortran
        "f", "f77", "f90", "f95", "f03", "f08" -> "!"

        // MATLAB (Objective-C .m intentionally not distinguished)
        "m" -> "%"

        // Default to hash for unknown file types
        else -> "#"
    }
}
