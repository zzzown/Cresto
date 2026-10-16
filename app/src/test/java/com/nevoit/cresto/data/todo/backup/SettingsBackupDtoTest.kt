package com.nevoit.cresto.data.todo.backup

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class SettingsBackupDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `an empty object falls back to every default`() {
        val dto = json.decodeFromString<SettingsBackupDto>("{}")
        assertEquals(SettingsBackupDto(), dto)
        assertEquals(30, dto.aiResponseTimeoutSeconds)
        assertNull(dto.themePrimaryColor)
    }

    @Test
    fun `a fully populated snapshot survives a round trip`() {
        val original = SettingsBackupDto(
            colorMode = 1,
            themePrimaryColor = -16728876,
            isCustomPrimaryColorEnabled = true,
            isUseDynamicColor = true,
            isLiteMode = true,
            isLiquidGlass = true,
            sortOption = 2,
            sortOrder = 0,
            isDueTodayMarker = false,
            isOverdueMarker = false,
            isCompletionSoundEnabled = false,
            aiApiUrl = "https://example.com/v1",
            aiTextModel = "glm-4-plus",
            aiMultimodalModel = "glm-4v",
            aiResponseTimeoutSeconds = 120,
            isExtractScreenQuickTileEnabled = true,
            isAutoAddToSystemCalendar = true,
            isCheckUpdatesOnStartup = false
        )
        val restored = json.decodeFromString<SettingsBackupDto>(json.encodeToString(original))
        assertEquals(original, restored)
    }

    @Test
    fun `the api key is not part of the schema`() {
        val encoded = json.encodeToString(SettingsBackupDto(aiTextModel = "glm-4-flash"))
        assertFalse(encoded.contains("apiKey", ignoreCase = true))
        assertFalse(encoded.contains("ai_api", ignoreCase = true))
    }

    @Test
    fun `a schema 6 file has no settings section`() {
        val legacy = """
            {
              "schemaVersion": 6,
              "exportedAt": "2026-10-16T19:00:00",
              "todos": [],
              "subTodos": []
            }
        """.trimIndent()
        val backup = json.decodeFromString<TodoBackupFile>(legacy)
        assertEquals(6, backup.schemaVersion)
        assertNull(backup.settings)
    }

    @Test
    fun `a schema 7 file carries the settings section`() {
        val current = """
            {
              "schemaVersion": 7,
              "exportedAt": "2026-10-16T19:00:00",
              "todos": [],
              "subTodos": [],
              "settings": { "aiResponseTimeoutSeconds": 90 }
            }
        """.trimIndent()
        val backup = json.decodeFromString<TodoBackupFile>(current)
        assertEquals(7, backup.schemaVersion)
        assertEquals(90, backup.settings?.aiResponseTimeoutSeconds)
        // Fields absent from the file keep their defaults.
        assertEquals("", backup.settings?.aiTextModel)
        assertEquals(2, backup.settings?.colorMode)
    }

    @Test
    fun `a file newer than this build is rejected`() {
        val future = """
            {
              "schemaVersion": ${BACKUP_SCHEMA_VERSION + 1},
              "exportedAt": "2026-10-16T19:00:00",
              "todos": [],
              "subTodos": [],
              "settings": { "aiResponseTimeoutSeconds": 90 }
            }
        """.trimIndent()
        val backup = json.decodeFromString<TodoBackupFile>(future)
        assertThrows(UnsupportedBackupVersionException::class.java) {
            checkBackupSchemaVersion(backup.schemaVersion)
        }
    }
}
