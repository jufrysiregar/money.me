package com.moneyapp.domain.usecase.transaction

import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.repository.TransactionRepository
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Menyimpan transaksi ke repository.
     * Validasi:
     * - amount harus > 0
     * - category tidak boleh kosong atau hanya whitespace
     *
     * Validates: Requirements 6.3, 6.4
     */
    suspend operator fun invoke(transaction: Transaction): AppResult<Unit> {
        if (transaction.amount <= 0) {
            return AppResult.Error("Nominal transaksi harus lebih dari 0")
        }
        if (transaction.category.isBlank()) {
            return AppResult.Error("Kategori tidak boleh kosong")
        }
        return try {
            transactionRepository.saveTransaction(transaction)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Gagal menyimpan transaksi", e)
        }
    }
}
