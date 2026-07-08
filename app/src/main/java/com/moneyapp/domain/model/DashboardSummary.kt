package com.moneyapp.domain.model

data class DashboardSummary(
    val userName: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val totalInvestment: Double,
    val totalSaving: Double,
    val transactionCount: Int,
    val totalInvestmentProfit: Double = 0.0  // Unrealized profit/loss dari investasi aktif
) {
    /**
     * Saldo bersih yang dihitung dari total pemasukan dikurangi pengeluaran,
     * ditambah nilai investasi dan jumlah tabungan.
     *
     * Validates: Requirements 4.3
     */
    val netBalance: Double get() =
        totalIncome - totalExpense + totalInvestment + totalSaving
}
