package com.moneyapp.domain.usecase.investment

import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.repository.InvestmentRepository
import javax.inject.Inject

class UpdateInvestmentValueUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository
) {
    /**
     * Memperbarui nilai investasi saat ini (current value).
     * Validates: Requirements 8.3
     */
    suspend operator fun invoke(id: Long, currentValue: Double): AppResult<Unit> {
        if (currentValue < 0) {
            return AppResult.Error("Nilai investasi tidak boleh negatif")
        }
        return try {
            investmentRepository.updateCurrentValue(id, currentValue)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Gagal memperbarui nilai investasi", e)
        }
    }
}
