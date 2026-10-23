package com.decathlon.cis.networkmocks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.MessageFormat
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

class NetworkMocksBundleTest {
    @Test
    fun `French translations cover every English key`() {
        val english = bundle(Locale.ENGLISH)
        val french = bundle(Locale.FRENCH)
        val frenchKeys = requireNotNull(javaClass.getResourceAsStream("/messages/MyBundle_fr.properties")).use {
            PropertyResourceBundle(it).keySet()
        }

        assertEquals(english.keySet(), french.keySet())
        assertEquals(english.keySet(), frenchKeys)
        assertTrue(french.keySet().all { french.getString(it).isNotBlank() })
    }

    @Test
    fun `action labels and selection buttons are localized`() {
        assertEquals("Export Mocks (IDE -> Git)", bundle(Locale.ENGLISH).getString("action.ExportMocksAction.text"))
        assertEquals("Exporter les Mocks (IDE -> Git)", bundle(Locale.FRANCE).getString("action.ExportMocksAction.text"))
        assertEquals("Select all", bundle(Locale.ENGLISH).getString("selection.select.all"))
        assertEquals("S\u00e9lectionner tous", bundle(Locale.FRANCE).getString("selection.select.all"))
    }

    @Test
    fun `notifications preserve counts error details and French apostrophes`() {
        assertEquals("3 mock(s) exported!", format(Locale.ENGLISH, "export.success", 3))
        assertEquals("3 mock(s) export\u00e9(s) !", format(Locale.FRENCH, "export.success", 3))
        assertEquals("Export failed: disk full", format(Locale.ENGLISH, "export.error", "disk full"))
        assertEquals("Erreur lors de l'export : disk full", format(Locale.FRENCH, "export.error", "disk full"))
        assertEquals("Erreur d'import : invalid XML", format(Locale.FRENCH, "import.error", "invalid XML"))
        assertEquals(
            "2 mock(s) import\u00e9(s) ! (Red\u00e9marrez Android Studio si les r\u00e8gles n'apparaissent pas)",
            format(Locale.FRENCH, "import.success", 2)
        )
    }

    @Test
    fun `unsupported languages use the English base bundle`() {
        val english = bundle(Locale.ENGLISH)
        val unsupported = bundle(Locale.GERMAN)

        assertEquals(Locale.ROOT, unsupported.locale)
        english.keySet().forEach { key ->
            assertEquals(english.getString(key), unsupported.getString(key))
        }
    }

    private fun bundle(locale: Locale): ResourceBundle = ResourceBundle.getBundle(
        "messages.MyBundle",
        locale,
        javaClass.classLoader,
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
    )

    private fun format(locale: Locale, key: String, vararg params: Any): String =
        MessageFormat(bundle(locale).getString(key), locale).format(params)
}
