package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that stores metadata about the last backup operation.
 *
 * The table always contains exactly one row (id = 1).
 * - [lastBackupDate] is stored as an ISO-8601 string.
 * - [backupVersion] tracks the schema version of the backup file for compatibility checks.
 *
 * Satisfies Requirements 13.4 (backup_metadata table).
 */
@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey
    val id: Int = 1,                            // Always a single row

    @ColumnInfo(name = "last_backup_date")
    val lastBackupDate: String,

    @ColumnInfo(name = "backup_version")
    val backupVersion: Int
)
