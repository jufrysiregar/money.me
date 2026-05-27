package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyapp.data.local.db.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying investment records.
 *
 * Satisfies Requirements 3.4, 8.4.
 */
@Dao
interface InvestmentDao {

    /**
     * Observe all investment records ordered by date descending.
     */
    @Query("SELECT * FROM investments ORDER BY date DESC")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    /**
     * Insert or replace an investment record.
     *
     * @return the row ID of the newly inserted row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InvestmentEntity): Long

    /**
     * Update the current market value of an existing investment.
     *
     * @param id           primary key of the investment to update
     * @param currentValue new current market value
     */
    @Query("UPDATE investments SET current_value = :currentValue WHERE id = :id")
    suspend fun updateCurrentValue(id: Long, currentValue: Double)

    /**
     * Delete an investment record by its entity (matched by primary key).
     */
    @Delete
    suspend fun delete(entity: InvestmentEntity)

    /**
     * Delete an investment record directly by its primary key.
     *
     * @param id primary key of the investment to delete
     */
    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
