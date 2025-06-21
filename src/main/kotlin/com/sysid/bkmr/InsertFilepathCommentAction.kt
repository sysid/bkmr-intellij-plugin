// File: src/main/kotlin/com/sysid/bkmr/InsertFilepathCommentAction.kt
package com.sysid.bkmr

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * Action to insert filepath comment at the beginning of the current file.
 * This implementation directly inserts the comment without requiring LSP.
 */
class InsertFilepathCommentAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Always show the action in the action menu (Cmd+Shift+A)
        e.presentation.isVisible = true
        
        // Enable action only when we have a project and file
        e.presentation.isEnabled = project != null && 
            file != null && 
            !file.isDirectory &&
            isSupportedFile(file)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        // Get editor - try from event data first, then from file editor manager
        val editor = e.getData(CommonDataKeys.EDITOR) 
            ?: com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                .selectedTextEditor

        if (editor == null) {
            // Show notification if no editor is available
            com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("Bkmr Notifications")
                ?.createNotification(
                    "No editor available",
                    "Please open a file in the editor first",
                    com.intellij.notification.NotificationType.WARNING
                )?.notify(project)
            return
        }

        // Insert the filepath comment directly
        insertFilepathComment(project, editor, file)
    }

    private fun insertFilepathComment(project: Project, editor: Editor, file: VirtualFile) {
        val relativePath = getRelativePath(project, file)
        val commentSyntax = getCommentSyntax(file.path)
        
        val commentText = when (commentSyntax) {
            "<!--" -> "<!-- $relativePath -->\n"
            "/*" -> "/* $relativePath */\n"
            else -> "$commentSyntax $relativePath\n"
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            document.insertString(0, commentText)
        }
    }

    private fun getRelativePath(project: Project, file: VirtualFile): String {
        val projectBasePath = project.basePath ?: return file.name
        val projectPath = File(projectBasePath).toPath()
        val filePath = File(file.path).toPath()
        
        return try {
            projectPath.relativize(filePath).toString()
        } catch (e: Exception) {
            file.name
        }
    }

    private fun getCommentSyntax(filePath: String): String {
        val extension = Path.of(filePath).extension.lowercase()
        
        return when (extension) {
            // C-style languages
            "rs", "c", "cpp", "cc", "cxx", "h", "hpp", "java", "js", "ts", "jsx", 
            "tsx", "cs", "go", "swift", "kt", "scala", "dart" -> "//"
            // Shell-style languages
            "sh", "bash", "zsh", "fish", "py", "rb", "pl", "r", "yaml", "yml", "toml",
            "cfg", "ini", "properties" -> "#"
            // HTML/XML
            "html", "htm", "xml", "xhtml", "svg" -> "<!--"
            // CSS
            "css", "scss", "sass", "less" -> "/*"
            // SQL
            "sql" -> "--"
            // Lua
            "lua" -> "--"
            // Haskell
            "hs" -> "--"
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
            // MATLAB
            "m" -> "%"
            // Default to hash for unknown file types
            else -> "#"
        }
    }

    private fun isSupportedFile(file: VirtualFile): Boolean {
        // Support all text files, exclude only known binary types
        val extension = file.extension?.lowercase()

        // Exclude known binary file types
        val binaryExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "ico", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
            "zip", "tar", "gz", "rar", "7z", "bz2",
            "exe", "dll", "so", "dylib", "app", "dmg",
            "mp3", "mp4", "avi", "mov", "wav", "flac",
            "class", "jar", "war", "ear"
        )

        return !file.isDirectory && extension !in binaryExtensions
    }
}