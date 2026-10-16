package com.nevoit.cresto.data.todo.backup

import kotlinx.serialization.Serializable

/**
 * Highest backup schema this build can read and write.
 *
 * Bump it in the same change that alters [TodoBackupFile]; older builds will then reject newer
 * backups with a clear "please update the app" message instead of silently dropping unknown data.
 */
const val BACKUP_SCHEMA_VERSION = 7

/**
 * Thrown when a backup was written by a newer build than the one reading it.
 */
class UnsupportedBackupVersionException(
    val foundVersion: Int,
    val supportedVersion: Int
) : IllegalArgumentException(
    "Backup schema version $foundVersion is newer than the supported version $supportedVersion"
)

/**
 * Rejects backups written by a newer build.
 *
 * Deliberately dependency free so it can be unit tested on a plain JVM.
 */
fun checkBackupSchemaVersion(schemaVersion: Int) {
    if (schemaVersion > BACKUP_SCHEMA_VERSION) {
        throw UnsupportedBackupVersionException(
            foundVersion = schemaVersion,
            supportedVersion = BACKUP_SCHEMA_VERSION
        )
    }
}

@Serializable
data class TodoBackupFile(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val exportedAt: String,     // ISO_LOCAL_DATE_TIME
    val todos: List<TodoBackupDto>,
    val subTodos: List<SubTodoBackupDto>,
    val repeatRules: List<RepeatRuleBackupDto> = emptyList(),
    val groups: List<TodoGroupBackupDto> = emptyList(),
    /** Present from schema 7 onwards; null for older files, which then leave settings untouched. */
    val settings: SettingsBackupDto? = null
)

/**
 * App preferences carried alongside the to-do data.
 *
 * Every field has a default so a file written by a newer build (with extra fields) still parses.
 * Deliberately excludes:
 * - the API key, unless the user explicitly opts in (see [aiApiKey])
 * - `isFirstRun` / `lastUpdateCheckAt`, which are runtime state rather than preferences
 * - `appIcon`, whose restore switches an Activity alias and can disable the running component
 */
@Serializable
data class SettingsBackupDto(
    val colorMode: Int = 2,
    val themePrimaryColor: Int? = null,
    val isCustomPrimaryColorEnabled: Boolean = false,
    val isUseDynamicColor: Boolean = false,
    val isLiteMode: Boolean = false,
    val isLiquidGlass: Boolean = false,
    val sortOption: Int = 0,
    val sortOrder: Int = 1,
    val isDueTodayMarker: Boolean = true,
    val isOverdueMarker: Boolean = true,
    val isCompletionSoundEnabled: Boolean = true,
    val aiApiUrl: String = "",
    val aiTextModel: String = "",
    val aiMultimodalModel: String = "",
    /** Only present when the user opted in; absent means "leave the stored key alone". */
    val aiApiKey: String? = null,
    val aiResponseTimeoutSeconds: Int = 30,
    val isExtractScreenQuickTileEnabled: Boolean = false,
    val isAutoAddToSystemCalendar: Boolean = false,
    val isCheckUpdatesOnStartup: Boolean = true
)

@Serializable
data class TodoBackupDto(
    val id: Int,
    val title: String,
    val dueDate: String?,       // ISO_LOCAL_DATE
    val creationDateTime: String,   // ISO_LOCAL_DATE_TIME
    val isCompleted: Boolean,
    val isPinned: Boolean = false,
    val flag: Int,
    val completedDateTime: String?,  // ISO_LOCAL_DATE_TIME
    val startTime: String?,     // ISO_LOCAL_TIME
    val endTime: String?,       // ISO_LOCAL_TIME
    val reminderMode: String? = null,
    val reminderOffsetMinutes: Int? = null,
    val reminderDayOffset: Int? = null,
    val reminderTime: String? = null, // ISO_LOCAL_TIME
    val reminderPersistent: Boolean = false,
    val reminderStrong: Boolean = false,
    val repeatRuleId: String? = null,
    val seriesId: String? = null,
    val occurrenceDate: String? = null, // ISO_LOCAL_DATE
    val generatedFromTodoId: Int? = null,
    val occurrenceEditedAt: String? = null, // ISO_LOCAL_DATE_TIME
    val groupId: Int? = null,
    val deletedAt: String? = null // ISO_LOCAL_DATE_TIME
)

@Serializable
data class SubTodoBackupDto(
    val id: Int,
    val parentId: Int,
    val description: String,
    val isCompleted: Boolean
)

@Serializable
data class RepeatRuleBackupDto(
    val id: String,
    val seriesId: String,
    val frequency: String,
    val interval: Int = 1,
    val weekdays: String? = null,
    val monthDay: Int? = null,
    val monthDays: String? = null,
    val months: String? = null,
    val endDate: String? = null, // ISO_LOCAL_DATE
    val maxOccurrences: Int? = null,
    val anchorDate: String, // ISO_LOCAL_DATE
    val createNextOnCompletion: Boolean = true
)

@Serializable
data class TodoGroupBackupDto(
    val id: Int,
    val name: String,
    val color: Int = 0,
    val sortOrder: Int = 0
)
