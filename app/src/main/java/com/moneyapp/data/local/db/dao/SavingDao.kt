package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyapp.data.local.db.entity.SavingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying savings goal records.
 *
 * Satisfies Requirements 3.5, 9.4, 9.5, 9.6.
 */
@Dao
interface SavingDao {

    /**
     * Observe all savings goal records ordered by target date ascending.
     */
    @Query("SELECT * FROM savings ORDER BY target_date ASC")
    fun getAllSavings(): Flow<List<SavingEntity>>

    /**
     * Insert or replace a savings goal record.
     *
     * @return the row ID of the newly inserted row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavingEntity): Long

    /**
     * Update the current saved amount for an existing savings goal.
     *
     * @param id            primary key of the savings goal to update
     * @param currentAmount new amount saved so far
     */
    @Query("UPDATE savings SET current_amount = :currentAmount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Long, currentAmount: Double)

    /**
     * Delete a savings goal record by its entity (matched by primary key).
     */
    @Delete
    suspend fun delete(entity: SavingEntity)

    /**
     * Delete a savings goal record directly by its primary key.
     *
     * @param id primary key of the savings goal to delete
     */
    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
