package com.moneyapp.domain.repository

import com.moneyapp.domain.model.Investment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InvestmentRepository {
    
    // ============================================================
    //  GET / OBSERVE
    // ============================================================
    
    fun getAllInvestments(): Flow<List<Investment>>
    
    fun getActiveInvestments(): Flow<List<Investment>>
    
    fun getSoldInvestments(): Flow<List<Investment>>
    
    suspend fun getInvestmentById(id: Long): Investment?
    
    // ============================================================
    //  INSERT / UPDATE
    // ============================================================
    
    suspend fun saveInvestment(investment: Investment)
    
    suspend fun updateInvestment(investment: Investment)
    
    /**
     * Update current value only (backward compatibility).
     */
    suspend fun updateCurrentValue(id: Long, currentValue: Double)
    
    /**
     * Update investment details (average price, current price, total amount).
     */
    suspend fun updateInvestmentDetails(
        id: Long,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?
    )
    
    /**
     * Mark investment as sold and create transaction record.
     */
    suspend fun sellInvestment(
        id: Long,
        soldPrice: Double,
        soldDate: LocalDate
    )
    
    // ============================================================
    //  DELETE
    // ============================================================
    
    suspend fun deleteInvestment(id: Long)
    
    suspend fun deleteAllInvestments()
    
    // ============================================================
    //  SUMMARY
    // ============================================================
    
    suspend fun getActiveInvestmentsSummary(): InvestmentSummaryData
    
    suspend fun getSoldInvestmentsSummary(): InvestmentSummaryData
}

/**
 * Data class for investment summary.
 */
data class InvestmentSummaryData(
    val totalCount: Int = 0,
    val totalAmount: Double = 0.0,
    val totalCurrentValue: Double = 0.0
)