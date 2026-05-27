package com.moneyapp.domain.usecase.dashboard

import com.moneyapp.domain.model.DashboardSummary
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
     * Validates: Requirements 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3
     */
    operator fun invoke(): Flow<DashboardSummary> = combine(
        userRepository.getUser(),
        transactionRepository.getAllTransactions(),
        investmentRepository.getAllInvestments(),
        savingRepository.getAllSavings()
    ) { user, transactions, investments, savings ->
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val totalInvestment = investments.sumOf { it.currentValue }

        val totalSaving = savings.sumOf { it.currentAmount }

        DashboardSummary(
            userName = user?.fullName ?: "",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalInvestment = totalInvestment,
            totalSaving = totalSaving,
            transactionCount = transactions.size
        )
    }
}
