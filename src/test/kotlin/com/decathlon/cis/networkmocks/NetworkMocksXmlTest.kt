package com.decathlon.cis.networkmocks

import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader

class NetworkMocksXmlTest {
    @Test
    fun `export extracts only RuleData from NetworkInspectorRules`() {
        val workspace = parse(
            """
            <project>
              <component name="OtherComponent"><RuleData id="ignored"/></component>
              <component name="NetworkInspectorRules">
                <option name="rulesList"><list>
                  <RuleData id="first"/>
                  <wrapper><RuleData id="second"/></wrapper>
                  <OtherRule id="ignored"/>
                </list></option>
              </component>
            </project>
            """.trimIndent()
        )

        val rules = NetworkMocksXml.rulesForExport(workspace)

        assertEquals(listOf("first", "second"), rules.map { it.getAttributeValue("id") })
    }

    @Test
    fun `export document contains copies of selected rules`() {
        val original = Element("RuleData").setAttribute("id", "rule-1")

        val exported = NetworkMocksXml.createExportDocument(listOf(original))

        assertEquals("network-mocks", exported.rootElement.name)
        assertEquals("rule-1", exported.rootElement.getChild("RuleData")?.getAttributeValue("id"))
        assertNotSame(original, exported.rootElement.getChild("RuleData"))
    }

    @Test
    fun `import creates missing component option and list`() {
        val workspace = parse("<project/>")
        val rules = NetworkMocksXml.importedRules(parse("<network-mocks><RuleData id=\"rule-1\"/></network-mocks>"))

        val addedCount = NetworkMocksXml.appendImportedRules(workspace.rootElement, rules)

        val component = workspace.rootElement.getChild("component")
        val list = component?.getChild("option")?.getChild("list")
        assertEquals(1, addedCount)
        assertEquals("NetworkInspectorRules", component?.getAttributeValue("name"))
        assertEquals("rule-1", list?.getChild("RuleData")?.getAttributeValue("id"))
        assertNotSame(rules.first(), list?.getChild("RuleData"))
    }

    @Test
    fun `import appends rules without replacing existing rules`() {
        val workspace = parse(
            """
            <project>
              <component name="NetworkInspectorRules">
                <option name="rulesList"><list><RuleData id="existing"/></list></option>
              </component>
            </project>
            """.trimIndent()
        )
        val rules = NetworkMocksXml.importedRules(
            parse("<network-mocks><RuleData id=\"new-1\"/><RuleData id=\"new-2\"/></network-mocks>")
        )

        val addedCount = NetworkMocksXml.appendImportedRules(workspace.rootElement, rules)
        val importedIds = workspace.rootElement
            .getChild("component")
            .getChild("option")
            .getChild("list")
            .getChildren("RuleData")
            .map { it.getAttributeValue("id") }

        assertEquals(2, addedCount)
        assertEquals(listOf("existing", "new-1", "new-2"), importedIds)
    }

    @Test
    fun `import with no rules leaves workspace unchanged`() {
        val workspace = parse("<project/>")

        val addedCount = NetworkMocksXml.appendImportedRules(workspace.rootElement, emptyList())

        assertEquals(0, addedCount)
        assertTrue(workspace.rootElement.getChildren("component").isEmpty())
    }

    private fun parse(xml: String) = SAXBuilder().build(StringReader(xml))
}
