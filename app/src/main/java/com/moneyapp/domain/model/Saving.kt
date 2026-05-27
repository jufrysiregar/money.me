package com.moneyapp.domain.model

import java.time.LocalDate

data class Saving(
    val id: Long = 0,
    val targetName: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: LocalDate
) {
    /**
     * Persentase kemajuan tabungan terhadap target.
     * Nilai dibatasi maksimal 100.0 meskipun currentAmount melebihi targetAmount.
     * Mengembalikan 0.0 jika targetAmount <= 0.
     *
     * Validates: Requirements 9.5
     */
    val progressPercent: Double get() =
        if (targetAmount > 0) (currentAmount / targetAmount * 100).coerceAtMost(100.0) else 0.0

    /**
     * Status apakah target tabungan sudah tercapai.
     * Bernilai true jika currentAmount >= targetAmount.
     *
     * Validates: Requirements 9.6
     */
    val isAchieved: Boolean get() = currentAmount >= targetAmount
}
