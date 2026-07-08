package com.moneyapp.domain.usecase.dashboard

import com.moneyapp.domain.model.DashboardSummary
import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.model.TransactionType
import com.moneyapp.domain.repository.InvestmentRepository
import com.moneyapp.domain.repository.SavingRepository
import com.moneyapp.domain.repository.TransactionRepository
import com.moneyapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val investmentRepository: InvestmentRepository,
    private val savingRepository: SavingRepository
) {
    /**
     * Menggabungkan data dari semua repository menjadi satu DashboardSummary.
     * Menggunakan combine untuk reaktif terhadap perubahan data apapun.
     *
     * totalInvestment = total modal dari investasi aktif (totalAmount)
     * totalInvestmentProfit = total unrealized profit/loss dari investasi yang ada harga saat ini
     *
     * Validates: Requirements 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3
     */
    operator fun invoke(): Flow<DashboardSummary> = combine(
        userRepository.getUser(),
        transactionRepository.getAllTransactions(),
        investmentRepository.getActiveInvestments(),
        savingRepository.getAllSavings()
    ) { user, transactions, investments, savings ->
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Total modal investasi aktif
        val totalInvestment = investments.sumOf { it.totalAmount ?: it.amount }

        // Total unrealized profit/loss (hanya investasi yang ada harga saat ini)
        val totalInvestmentProfit = investments.sumOf { inv -> calculateProfitLoss(inv) }

        val totalSaving = savings.sumOf { it.currentAmount }

        DashboardSummary(
            userName = user?.fullName ?: "",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalInvestment = totalInvestment,
            totalSaving = totalSaving,
            transactionCount = transactions.size,
            totalInvestmentProfit = totalInvestmentProfit
        )
    }

    /**
     * Hitung profit/loss dari satu investasi.
     * Jika tidak ada harga saat ini, return 0.
     */
    private fun calculateProfitLoss(inv: Investment): Double {
        val total = inv.totalAmount ?: inv.amount
        val avg = inv.averagePrice ?: return 0.0
        val cur = inv.currentPrice ?: return 0.0
        if (avg <= 0.0) return 0.0
        val lembar = total / avg
        val nilaiSekarang = lembar * cur
        return nilaiSekarang - total
    }
}
