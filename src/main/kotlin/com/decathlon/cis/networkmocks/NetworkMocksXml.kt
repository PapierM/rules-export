package com.decathlon.cis.networkmocks

import org.jdom.Document
import org.jdom.Element

internal object NetworkMocksXml {
    fun rulesForExport(workspaceDoc: Document): List<Element> {
        val rulesComponent = workspaceDoc.rootElement.getChildren("component").find {
            it.getAttributeValue("name") == "NetworkInspectorRules"
        } ?: return emptyList()

        val rules = mutableListOf<Element>()
        for (node in rulesComponent.descendants) {
            if (node is Element && node.name == "RuleData") {
                rules.add(node)
            }
        }
        return rules
    }

    fun createExportDocument(selectedRules: List<Element>): Document {
        val exportRoot = Element("network-mocks")
        selectedRules.forEach { rule ->
            exportRoot.addContent(rule.clone() as Element)
        }
        return Document(exportRoot)
    }

    fun importedRules(importDoc: Document): List<Element> = importDoc.rootElement.children

    fun appendImportedRules(rootNode: Element, importedRules: List<Element>): Int {
        if (importedRules.isEmpty()) return 0

        var rulesComponent = rootNode.getChildren("component").find {
            it.getAttributeValue("name") == "NetworkInspectorRules"
        }
        if (rulesComponent == null) {
            rulesComponent = Element("component").setAttribute("name", "NetworkInspectorRules")
            rootNode.addContent(rulesComponent)
        }

        var rulesListOption = rulesComponent.getChildren("option").find {
            it.getAttributeValue("name") == "rulesList"
        }
        if (rulesListOption == null) {
            rulesListOption = Element("option").setAttribute("name", "rulesList")
            rulesComponent.addContent(rulesListOption)
        }

        var listElement = rulesListOption.getChild("list")
        if (listElement == null) {
            listElement = Element("list")
            rulesListOption.addContent(listElement)
        }

        importedRules.forEach { rule ->
            listElement.addContent(rule.clone() as Element)
        }
        return importedRules.size
    }
}
