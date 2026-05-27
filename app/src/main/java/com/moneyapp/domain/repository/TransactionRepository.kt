package com.moneyapp.domain.repository

import com.moneyapp.domain.model.MonthSummary
import com.moneyapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    fun getCurrentMonthSummary(): Flow<MonthSummary>
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
}
