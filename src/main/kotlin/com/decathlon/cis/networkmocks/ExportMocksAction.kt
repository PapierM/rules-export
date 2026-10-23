package com.decathlon.cis.networkmocks

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.awt.GridLayout
import java.io.File
import java.io.FileOutputStream
import javax.swing.JComponent
import javax.swing.JPanel

class ExportMocksAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val workspaceFile = File("$basePath/.idea/workspace.xml")

        if (!workspaceFile.exists()) {
            showNotification(project, "Fichier workspace.xml introuvable.", NotificationType.ERROR)
            return
        }

        try {
            val builder = SAXBuilder()
            val doc = builder.build(workspaceFile)
            val rootNode = doc.rootElement

            val rulesList = NetworkMocksXml.rulesForExport(doc)
            if (rulesList.isEmpty() && rootNode.getChildren("component").none {
                    it.getAttributeValue("name") == "NetworkInspectorRules"
                }) {
                showNotification(project, "Aucune configuration de NetworkInspectorRules trouvée.", NotificationType.WARNING)
                return
            }

            if (rulesList.isEmpty()) {
                showNotification(project, "Aucun mock trouvé.", NotificationType.WARNING)
                return
            }

            val dialog = RuleSelectionDialog(rulesList)
            if (dialog.showAndGet()) {
                val selectedRules = dialog.getSelectedRules()
                if (selectedRules.isEmpty()) return

                val descriptor = FileSaverDescriptor("Exporter les Mocks", "Sauvegarder les règles", "xml")
                val saveDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
                val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
                val fileWrapper = saveDialog.save(baseDir, "network-mocks.xml") ?: return

                val outputter = XMLOutputter(Format.getPrettyFormat())
                FileOutputStream(fileWrapper.file).use { fos ->
                    outputter.output(NetworkMocksXml.createExportDocument(selectedRules), fos)
                }

                showNotification(project, "${selectedRules.size} mock(s) exporté(s) !", NotificationType.INFORMATION)
            }
        } catch (ex: Exception) {
            showNotification(project, "Erreur lors de l'export: ${ex.message}", NotificationType.ERROR)
        }
    }

    private fun showNotification(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance().getNotificationGroup("NetworkMocksNotifications").createNotification(message, type).notify(project)
    }
}

class RuleSelectionDialog(private val rules: List<Element>) : DialogWrapper(true) {
    private val checkBoxes = mutableListOf<Pair<JBCheckBox, Element>>()

    init {
        title = "Sélectionner les Mocks à Exporter"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(0, 1))
        rules.forEach { rule ->
            // Extraction du nom de la règle depuis la balise <option name="name" value="Mon Mock" />
            var ruleName = "Mock sans nom"
            for (child in rule.children) {
                if (child.name == "option" && child.getAttributeValue("name") == "name") {
                    ruleName = child.getAttributeValue("value") ?: ruleName
                    break
                }
            }

            val checkBox = JBCheckBox(ruleName, true)
            checkBoxes.add(Pair(checkBox, rule))
            panel.add(checkBox)
        }
        return JBScrollPane(panel)
    }

    fun getSelectedRules(): List<Element> {
        return checkBoxes.filter { it.first.isSelected }.map { it.second }
    }
}