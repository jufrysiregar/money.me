package com.moneyapp.domain.repository

import com.moneyapp.domain.model.Investment
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getAllInvestments(): Flow<List<Investment>>
    suspend fun saveInvestment(investment: Investment)
    suspend fun updateCurrentValue(id: Long, currentValue: Double)
    suspend fun deleteInvestment(id: Long)
}
