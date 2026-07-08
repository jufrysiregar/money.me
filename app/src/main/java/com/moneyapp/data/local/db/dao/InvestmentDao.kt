package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyapp.data.local.db.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying investment records.
 *
 * Satisfies Requirements 3.4, 8.4.
 */
@Dao
interface InvestmentDao {

    // ============================================================
    //  QUERY OBSERVABLE (Flow)
    // ============================================================

    /**
     * Observe all investment records ordered by date descending.
     */
    @Query("SELECT * FROM investments ORDER BY date DESC")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    /**
     * Observe all active investments (not sold) ordered by date descending.
     */
    @Query("SELECT * FROM investments WHERE is_sold = 0 ORDER BY date DESC")
    fun getActiveInvestments(): Flow<List<InvestmentEntity>>

    /**
     * Observe all sold investments ordered by sold_date descending.
     */
    @Query("SELECT * FROM investments WHERE is_sold = 1 ORDER BY sold_date DESC")
    fun getSoldInvestments(): Flow<List<InvestmentEntity>>

    // ============================================================
    //  QUERY SUSPEND (Single Result)
    // ============================================================

    /**
     * Get investment by ID.
     */
    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getInvestmentById(id: Long): InvestmentEntity?

    /**
     * Get all active investments as list (suspend).
     */
    @Query("SELECT * FROM investments WHERE is_sold = 0 ORDER BY date DESC")
    suspend fun getActiveInvestmentsList(): List<InvestmentEntity>

    /**
     * Get all sold investments as list (suspend).
     */
    @Query("SELECT * FROM investments WHERE is_sold = 1 ORDER BY sold_date DESC")
    suspend fun getSoldInvestmentsList(): List<InvestmentEntity>

    // ============================================================
    //  INSERT / UPDATE / DELETE
    // ============================================================

    /**
     * Insert or replace an investment record.
     *
     * @return the row ID of the newly inserted row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InvestmentEntity): Long

    /**
     * Update an existing investment record.
     */
    @Update
    suspend fun update(entity: InvestmentEntity)

    /**
     * Update the current market value of an existing investment.
     * (Backward compatibility - uses current_value column)
     *
     * @param id           primary key of the investment to update
     * @param currentValue new current market value
     */
    @Query("UPDATE investments SET current_value = :currentValue WHERE id = :id")
    suspend fun updateCurrentValue(id: Long, currentValue: Double)

    /**
     * Update investment with new columns.
     * 
     * @param id Investment ID
     * @param averagePrice New average price (optional)
     * @param currentPrice New current market price (optional)
     * @param totalAmount New total investment amount (optional)
     */
    @Query("""
        UPDATE investments 
        SET average_price = COALESCE(:averagePrice, average_price),
            current_price = COALESCE(:currentPrice, current_price),
            total_amount = COALESCE(:totalAmount, total_amount),
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateInvestmentDetails(
        id: Long,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?,
        updatedAt: String
    )

    /**
     * Mark investment as sold.
     */
    @Query("""
        UPDATE investments 
        SET is_sold = 1, 
            sold_date = :soldDate, 
            sold_price = :soldPrice,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun markAsSold(
        id: Long,
        soldDate: String,
        soldPrice: Double,
        updatedAt: String
    )

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

    /**
     * Delete all investments (for testing / reset).
     */
    @Query("DELETE FROM investments")
    suspend fun deleteAll()

    // ============================================================
    //  SUMMARY QUERIES
    // ============================================================

    /**
     * Get summary of active investments.
     */
    @Query("""
        SELECT 
            COUNT(*) as totalCount,
            COALESCE(SUM(total_amount), 0) as totalAmount,
            COALESCE(SUM(current_value), 0) as totalCurrentValue
        FROM investments 
        WHERE is_sold = 0
    """)
    suspend fun getActiveInvestmentsSummary(): InvestmentSummary

    /**
     * Get summary of sold investments.
     */
    @Query("""
        SELECT 
            COUNT(*) as totalCount,
            COALESCE(SUM(total_amount), 0) as totalAmount
        FROM investments 
        WHERE is_sold = 1
    """)
    suspend fun getSoldInvestmentsSummary(): InvestmentSummary
}

/**
 * Data class for investment summary.
 */
data class InvestmentSummary(
    val totalCount: Int = 0,
    val totalAmount: Double = 0.0,
    val totalCurrentValue: Double = 0.0
)