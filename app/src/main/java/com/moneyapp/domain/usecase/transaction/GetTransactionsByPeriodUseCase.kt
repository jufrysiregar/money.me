package com.moneyapp.domain.usecase.transaction

import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import javax.inject.Inject

enum class FilterPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

class GetTransactionsByPeriodUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Mengambil transaksi berdasarkan periode filter.
     * - DAILY  : hari ini
     * - WEEKLY : Senin s.d. Minggu minggu ini
     * - MONTHLY: awal s.d. akhir bulan ini
     * - YEARLY : 1 Januari s.d. 31 Desember tahun ini
     *
     * Validates: Requirements 4.5, 10.2, 10.3, 10.4, 10.5
     */
    operator fun invoke(period: FilterPeriod, referenceDate: LocalDate = LocalDate.now()): Flow<List<Transaction>> {
        val (start, end) = when (period) {
            FilterPeriod.DAILY -> referenceDate to referenceDate
            FilterPeriod.WEEKLY -> {
                val startOfWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                startOfWeek to endOfWeek
            }
            FilterPeriod.MONTHLY -> {
                val startOfMonth = referenceDate.with(TemporalAdjusters.firstDayOfMonth())
                val endOfMonth = referenceDate.with(TemporalAdjusters.lastDayOfMonth())
                startOfMonth to endOfMonth
            }
            FilterPeriod.YEARLY -> {
                val startOfYear = referenceDate.with(TemporalAdjusters.firstDayOfYear())
                val endOfYear = referenceDate.with(TemporalAdjusters.lastDayOfYear())
                startOfYear to endOfYear
            }
        }
        return transactionRepository.getTransactionsByDateRange(start, end)
    }
}
