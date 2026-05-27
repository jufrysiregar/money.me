package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyapp.data.local.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying transaction records.
 *
 * Satisfies Requirements 3.2, 3.3, 4.1, 4.2, 10.2.
 */
@Dao
interface TransactionDao {

    /**
     * Observe all transactions ordered by date descending.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Observe transactions within an inclusive date range.
     *
     * @param startDate ISO-8601 date string "yyyy-MM-dd" (inclusive lower bound)
     * @param endDate   ISO-8601 date string "yyyy-MM-dd" (inclusive upper bound)
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC
        """
    )
    fun getByDateRange(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    /**
     * Observe the sum of [amount] for a given [type] within a specific month.
     *
     * @param type        "income" or "expense"
     * @param monthPrefix ISO-8601 year-month prefix, e.g. "2024-01"
     * @return 0.0 when no matching rows exist (COALESCE ensures non-null)
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND date LIKE :monthPrefix || '%'
        """
    )
    fun sumByTypeAndMonth(type: String, monthPrefix: String): Flow<Double>

    /**
     * Insert or replace a transaction.
     *
     * @return the row ID of the newly inserted row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    /**
     * Delete a transaction by its entity (matched by primary key).
     */
    @Delete
    suspend fun delete(entity: TransactionEntity)

    /**
     * Delete a transaction directly by its primary key.
     *
     * @param id primary key of the transaction to delete
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
