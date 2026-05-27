package com.moneyapp.data.repository

import com.moneyapp.data.local.db.dao.TransactionDao
import com.moneyapp.data.mapper.TransactionMapper
import com.moneyapp.domain.model.MonthSummary
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [TransactionRepository] backed by Room via [TransactionDao].
 *
 * Satisfies Requirements 4.4, 6.5.
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Observe all transactions ordered by date descending. */
    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { TransactionMapper.toDomain(it) }
        }

    /** Observe transactions within an inclusive date range. */
    override fun getTransactionsByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        transactionDao.getByDateRange(
            startDate = start.format(dateFormatter),
            endDate = end.format(dateFormatter)
        ).map { entities ->
            entities.map { TransactionMapper.toDomain(it) }
        }

    /**
     * Observe the current month's income/expense summary.
     * Combines two separate sum queries (income + expense) and the full list
     * to derive the transaction count.
     */
    override fun getCurrentMonthSummary(): Flow<MonthSummary> {
        val now = YearMonth.now()
        val monthPrefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        val incomeFlow = transactionDao.sumByTypeAndMonth("income", monthPrefix)
        val expenseFlow = transactionDao.sumByTypeAndMonth("expense", monthPrefix)
        val countFlow = transactionDao.getByDateRange(
            startDate = now.atDay(1).format(dateFormatter),
            endDate = now.atEndOfMonth().format(dateFormatter)
        ).map { it.size }

        return combine(incomeFlow, expenseFlow, countFlow) { income, expense, count ->
            MonthSummary(
                totalIncome = income,
                totalExpense = expense,
                transactionCount = count
            )
        }
    }

    /** Persist a transaction (insert or replace). */
    override suspend fun saveTransaction(transaction: Transaction) {
        transactionDao.insert(TransactionMapper.toEntity(transaction))
    }

    /** Delete a transaction by its primary key. */
    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }
}
