package com.decathlon.cis.networkmocks

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.io.File
import java.io.FileOutputStream

class ImportMocksAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val workspaceFile = File("$basePath/.idea/workspace.xml")

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml").apply {
            title = "Sélectionner le Fichier de Mocks à Importer"
        }
        val virtualFile = FileChooser.chooseFile(descriptor, project, null) ?: return
        val sourceGitFile = File(virtualFile.path)

        try {
            val builder = SAXBuilder()

            val importDoc = builder.build(sourceGitFile)
            val importedRules = importDoc.rootElement.children

            if (importedRules.isEmpty()) {
                showNotification(project, "Aucune règle trouvée dans le fichier d'import.", NotificationType.WARNING)
                return
            }

            val workspaceDoc = builder.build(workspaceFile)
            val rootNode = workspaceDoc.rootElement

            // 1. Trouver ou créer <component name="NetworkInspectorRules">
            var rulesComponent = rootNode.getChildren("component").find {
                it.getAttributeValue("name") == "NetworkInspectorRules"
            }
            if (rulesComponent == null) {
                rulesComponent = Element("component").setAttribute("name", "NetworkInspectorRules")
                rootNode.addContent(rulesComponent)
            }

            // 2. Trouver ou créer <option name="rulesList">
            var rulesListOption = rulesComponent.getChildren("option").find {
                it.getAttributeValue("name") == "rulesList"
            }
            if (rulesListOption == null) {
                rulesListOption = Element("option").setAttribute("name", "rulesList")
                rulesComponent.addContent(rulesListOption)
            }

            // 3. Trouver ou créer <list>
            var listElement = rulesListOption.getChild("list")
            if (listElement == null) {
                listElement = Element("list")
                rulesListOption.addContent(listElement)
            }

            // 4. Injecter les RuleData dans <list>
            var addedCount = 0
            for (rule in importedRules) {
                listElement.addContent(rule.clone() as Element)
                addedCount++
            }

            val outputter = XMLOutputter(Format.getPrettyFormat())
            FileOutputStream(workspaceFile).use { fos ->
                outputter.output(workspaceDoc, fos)
            }

            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(workspaceFile)?.refresh(false, false)
            showNotification(project, "$addedCount mock(s) importé(s) ! (Redémarrez Android Studio si les règles n'apparaissent pas)", NotificationType.INFORMATION)

        } catch (ex: Exception) {
            showNotification(project, "Erreur d'import: ${ex.message}", NotificationType.ERROR)
        }
    }

    private fun showNotification(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance().getNotificationGroup("NetworkMocksNotifications").createNotification(message, type).notify(project)
    }
}