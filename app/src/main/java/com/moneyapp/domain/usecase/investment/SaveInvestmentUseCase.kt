package com.moneyapp.domain.usecase.investment

import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.repository.InvestmentRepository
import javax.inject.Inject

class SaveInvestmentUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository
) {
    /**
     * Menyimpan data investasi ke repository.
     * Validasi:
     * - nama tidak boleh kosong atau hanya whitespace
     * - amount harus > 0
     *
     * Validates: Requirements 8.3
     */
    suspend operator fun invoke(investment: Investment): AppResult<Unit> {
        if (investment.name.isBlank()) {
            return AppResult.Error("Nama investasi tidak boleh kosong")
        }
        if (investment.amount <= 0) {
            return AppResult.Error("Nominal investasi harus lebih dari 0")
        }
        return try {
            investmentRepository.saveInvestment(investment)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Gagal menyimpan investasi", e)
        }
    }
}
