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
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.io.File
import java.io.FileOutputStream
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class ExportMocksAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val workspaceFile = File("$basePath/.idea/workspace.xml")

        if (!workspaceFile.exists()) {
            showNotification(project, NetworkMocksBundle.message("export.workspace.missing"), NotificationType.ERROR)
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
                showNotification(project, NetworkMocksBundle.message("export.configuration.missing"), NotificationType.WARNING)
                return
            }

            if (rulesList.isEmpty()) {
                showNotification(project, NetworkMocksBundle.message("export.rules.empty"), NotificationType.WARNING)
                return
            }

            val dialog = RuleSelectionDialog(rulesList)
            if (dialog.showAndGet()) {
                val selectedRules = dialog.getSelectedRules()
                if (selectedRules.isEmpty()) return

                val descriptor = FileSaverDescriptor(
                    NetworkMocksBundle.message("export.dialog.title"),
                    NetworkMocksBundle.message("export.dialog.description"),
                    "xml"
                )
                val saveDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
                val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
                val fileWrapper = saveDialog.save(baseDir, "network-mocks.xml") ?: return

                val outputter = XMLOutputter(Format.getPrettyFormat())
                FileOutputStream(fileWrapper.file).use { fos ->
                    outputter.output(NetworkMocksXml.createExportDocument(selectedRules), fos)
                }

                showNotification(project, NetworkMocksBundle.message("export.success", selectedRules.size), NotificationType.INFORMATION)
            }
        } catch (ex: Exception) {
            showNotification(project, NetworkMocksBundle.message("export.error", ex.message ?: ex.toString()), NotificationType.ERROR)
        }
    }

    private fun showNotification(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance().getNotificationGroup("NetworkMocksNotifications").createNotification(message, type).notify(project)
    }
}

class RuleSelectionDialog(private val rules: List<Element>) : DialogWrapper(true) {
    private val checkBoxes = mutableListOf<Pair<JBCheckBox, Element>>()

    init {
        title = NetworkMocksBundle.message("selection.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(0, 1))
        rules.forEach { rule ->
            // Extraction du nom de la règle depuis la balise <option name="name" value="Mon Mock" />
            var ruleName = NetworkMocksBundle.message("selection.rule.unnamed")
            for (child in rule.children) {
                if (child.name == "option" && child.getAttributeValue("name") == "name") {
                    ruleName = child.getAttributeValue("value") ?: ruleName
                    break
                }
            }

            val checkBox = JBCheckBox(ruleName, false)
            checkBoxes.add(Pair(checkBox, rule))
            panel.add(checkBox)
        }
        val selectionButtons = JPanel(FlowLayout(FlowLayout.LEADING)).apply {
            add(JButton(NetworkMocksBundle.message("selection.select.all")).apply {
                addActionListener { checkBoxes.forEach { it.first.isSelected = true } }
            })
            add(JButton(NetworkMocksBundle.message("selection.deselect.all")).apply {
                addActionListener { checkBoxes.forEach { it.first.isSelected = false } }
            })
        }
        return JPanel(BorderLayout()).apply {
            add(selectionButtons, BorderLayout.NORTH)
            add(JBScrollPane(panel), BorderLayout.CENTER)
        }
    }

    fun getSelectedRules(): List<Element> {
        return checkBoxes.filter { it.first.isSelected }.map { it.second }
    }
}