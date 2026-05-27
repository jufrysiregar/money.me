package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyapp.data.local.db.entity.BackupMetadataEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying the single backup metadata row.
 *
 * The "backup_metadata" table always contains at most one row (id = 1).
 * Satisfies Requirements 13.4.
 */
@Dao
interface BackupMetadataDao {

    /**
     * Observe the current backup metadata.
     * Emits null if no backup has been performed yet.
     */
    @Query("SELECT * FROM backup_metadata WHERE id = 1")
    fun getMetadata(): Flow<BackupMetadataEntity?>

    /**
     * Insert or replace the backup metadata.
     * Because id is always 1, this effectively upserts the single row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BackupMetadataEntity)
}
