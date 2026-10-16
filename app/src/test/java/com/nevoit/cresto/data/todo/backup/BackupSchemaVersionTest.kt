package com.nevoit.cresto.data.todo.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSchemaVersionTest {

    @Test
    fun `current schema version is accepted`() {
        checkBackupSchemaVersion(BACKUP_SCHEMA_VERSION)
    }

    @Test
    fun `older schema versions are accepted`() {
        checkBackupSchemaVersion(1)
        checkBackupSchemaVersion(BACKUP_SCHEMA_VERSION - 1)
    }

    @Test
    fun `newer schema versions are rejected`() {
        val error = assertThrows(UnsupportedBackupVersionException::class.java) {
            checkBackupSchemaVersion(BACKUP_SCHEMA_VERSION + 1)
        }
        assertEquals(BACKUP_SCHEMA_VERSION + 1, error.foundVersion)
        assertEquals(BACKUP_SCHEMA_VERSION, error.supportedVersion)
    }

    @Test
    fun `rejection carries a readable message`() {
        val error = assertThrows(UnsupportedBackupVersionException::class.java) {
            checkBackupSchemaVersion(BACKUP_SCHEMA_VERSION + 5)
        }
        assertNotNull(error.message)
        assertTrue(error.message!!.contains("${BACKUP_SCHEMA_VERSION + 5}"))
        assertTrue(error.message!!.contains("$BACKUP_SCHEMA_VERSION"))
    }
}
