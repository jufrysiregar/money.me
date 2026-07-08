package com.moneyapp.data.repository

import com.moneyapp.data.local.db.dao.InvestmentDao
import com.moneyapp.data.local.db.dao.TransactionDao
import com.moneyapp.data.mapper.InvestmentMapper
import com.moneyapp.data.mapper.TransactionMapper
import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.model.TransactionType
import com.moneyapp.domain.repository.InvestmentRepository
import com.moneyapp.domain.repository.InvestmentSummaryData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [InvestmentRepository] backed by Room via [InvestmentDao].
 *
 * Satisfies Requirements 4.4, 8.3.
 */
@Singleton
class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val transactionDao: TransactionDao
) : InvestmentRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // ============================================================
    //  GET / OBSERVE
    // ============================================================

    /** Observe all investment records ordered by date descending. */
    override fun getAllInvestments(): Flow<List<Investment>> =
        investmentDao.getAllInvestments().map { entities ->
            entities.map { InvestmentMapper.toDomain(it) }
        }

    /** Observe active (not sold) investments. */
    override fun getActiveInvestments(): Flow<List<Investment>> =
        investmentDao.getActiveInvestments().map { entities ->
            entities.map { InvestmentMapper.toDomain(it) }
        }

    /** Observe sold investments. */
    override fun getSoldInvestments(): Flow<List<Investment>> =
        investmentDao.getSoldInvestments().map { entities ->
            entities.map { InvestmentMapper.toDomain(it) }
        }

    /** Get single investment by ID. */
    override suspend fun getInvestmentById(id: Long): Investment? {
        return investmentDao.getInvestmentById(id)?.let { InvestmentMapper.toDomain(it) }
    }

    // ============================================================
    //  INSERT / UPDATE
    // ============================================================

    /** Persist an investment record (insert or replace). */
    override suspend fun saveInvestment(investment: Investment) {
        investmentDao.insert(InvestmentMapper.toEntity(investment))
    }

    /** Update an existing investment record. */
    override suspend fun updateInvestment(investment: Investment) {
        investmentDao.update(InvestmentMapper.toEntity(investment))
    }

    /** Update current value only (backward compatibility). */
    override suspend fun updateCurrentValue(id: Long, currentValue: Double) {
        investmentDao.updateCurrentValue(id, currentValue)
    }

    /** Update investment details (average price, current price, total amount). */
    override suspend fun updateInvestmentDetails(
        id: Long,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?
    ) {
        val now = java.time.LocalDateTime.now().format(timestampFormatter)
        investmentDao.updateInvestmentDetails(id, averagePrice, currentPrice, totalAmount, now)
    }

    /**
     * Mark investment as sold and create an INCOME transaction for the total sale value.
     */
    override suspend fun sellInvestment(
        id: Long,
        soldPrice: Double,
        soldDate: LocalDate
    ) {
        val now = java.time.LocalDateTime.now().format(timestampFormatter)
        val soldDateStr = soldDate.format(dateFormatter)

        // 1. Get the investment to compute total sale value
        val entity = investmentDao.getInvestmentById(id) ?: return
        val totalAmount = entity.totalAmount ?: entity.amount
        val averagePrice = entity.averagePrice

        // Compute total sale value: jumlah lembar × harga jual
        val totalSaleValue = if (averagePrice != null && averagePrice > 0.0) {
            (totalAmount / averagePrice) * soldPrice
        } else {
            soldPrice // fallback: treat soldPrice as total sale value
        }

        // 2. Mark investment as sold in DB
        investmentDao.markAsSold(id, soldDateStr, soldPrice, now)

        // 3. Create an INCOME transaction for the sale proceeds
        val profitLoss = totalSaleValue - totalAmount
        val noteText = buildString {
            append("Penjualan Saham: ${entity.name}")
            append(" @ Rp ${soldPrice.toLong()}/lembar")
            if (profitLoss >= 0) {
                append(" | Profit: +Rp ${profitLoss.toLong()}")
            } else {
                append(" | Loss: -Rp ${(-profitLoss).toLong()}")
            }
        }

        val transaction = Transaction(
            type = TransactionType.INCOME,
            amount = totalSaleValue,
            category = "Penjualan Saham",
            date = soldDate,
            note = noteText
        )
        transactionDao.insert(TransactionMapper.toEntity(transaction))
    }

    // ============================================================
    //  DELETE
    // ============================================================

    /** Delete an investment record by its primary key. */
    override suspend fun deleteInvestment(id: Long) {
        investmentDao.deleteById(id)
    }

    /** Delete all investments. */
    override suspend fun deleteAllInvestments() {
        investmentDao.deleteAll()
    }

    // ============================================================
    //  SUMMARY
    // ============================================================

    override suspend fun getActiveInvestmentsSummary(): InvestmentSummaryData {
        val summary = investmentDao.getActiveInvestmentsSummary()
        return InvestmentSummaryData(
            totalCount = summary.totalCount,
            totalAmount = summary.totalAmount,
            totalCurrentValue = summary.totalCurrentValue
        )
    }

    override suspend fun getSoldInvestmentsSummary(): InvestmentSummaryData {
        val summary = investmentDao.getSoldInvestmentsSummary()
        return InvestmentSummaryData(
            totalCount = summary.totalCount,
            totalAmount = summary.totalAmount
        )
    }
}
