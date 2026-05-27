package com.moneyapp.domain.usecase.saving

import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Saving
import com.moneyapp.domain.repository.SavingRepository
import javax.inject.Inject

class SaveSavingUseCase @Inject constructor(
    private val savingRepository: SavingRepository
) {
    /**
     * Menyimpan data tabungan ke repository.
     * Validasi:
     * - nama target tidak boleh kosong atau hanya whitespace
     * - targetAmount harus > 0
     *
     * Validates: Requirements 9.3
     */
    suspend operator fun invoke(saving: Saving): AppResult<Unit> {
        if (saving.targetName.isBlank()) {
            return AppResult.Error("Nama target tabungan tidak boleh kosong")
        }
        if (saving.targetAmount <= 0) {
            return AppResult.Error("Target nominal tabungan harus lebih dari 0")
        }
        return try {
            savingRepository.saveSaving(saving)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Gagal menyimpan tabungan", e)
        }
    }
}
