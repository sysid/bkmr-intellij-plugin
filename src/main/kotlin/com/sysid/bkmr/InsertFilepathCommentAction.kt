// File: src/main/kotlin/com/sysid/bkmr/InsertFilepathCommentAction.kt
package com.sysid.bkmr

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Action to insert a filepath comment at the beginning of the current file.
 * This implementation directly inserts the comment without requiring LSP.
 */
class InsertFilepathCommentAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Always show the action in the action menu (Cmd+Shift+A)
        e.presentation.isVisible = true

        // Enable action only when we have a project and file
        e.presentation.isEnabled = project != null &&
            file != null &&
            !file.isDirectory &&
            FileSupport.isSupportedExtension(file.extension)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        // Get editor - try from event data first, then from file editor manager
        val editor = e.getData(CommonDataKeys.EDITOR)
            ?: FileEditorManager.getInstance(project).selectedTextEditor

        if (editor == null) {
            notify(project, "No editor available", "Please open a file in the editor first", NotificationType.WARNING)
            return
        }

        insertFilepathComment(project, editor, file)
    }

    private fun insertFilepathComment(project: Project, editor: Editor, file: VirtualFile) {
        val relativePath = getRelativePath(project, file)
        val comment = FileSupport.formatComment(file.extension, relativePath)

        val insertion = FileSupport.buildInsertion(editor.document.text, comment)
        if (insertion == null) {
            notify(project, "Filepath comment already present", relativePath)
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(insertion.offset, insertion.text)
        }
    }

    private fun getRelativePath(project: Project, file: VirtualFile): String {
        val projectBasePath = project.basePath ?: return file.name
        val projectPath = File(projectBasePath).toPath()
        val filePath = File(file.path).toPath()

        return try {
            projectPath.relativize(filePath).toString()
        } catch (e: IllegalArgumentException) {
            // relativize fails when the file is on a different root than the project
            file.name
        }
    }

    private fun notify(
        project: Project,
        title: String,
        content: String,
        type: NotificationType = NotificationType.INFORMATION,
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Bkmr Notifications")
            .createNotification(title, content, type)
            .notify(project)
    }
}
