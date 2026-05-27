package com.moneyapp.data.repository

import com.moneyapp.data.local.db.dao.InvestmentDao
import com.moneyapp.data.mapper.InvestmentMapper
import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [InvestmentRepository] backed by Room via [InvestmentDao].
 *
 * Satisfies Requirements 4.4, 8.3.
 */
@Singleton
class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao
) : InvestmentRepository {

    /** Observe all investment records ordered by date descending. */
    override fun getAllInvestments(): Flow<List<Investment>> =
        investmentDao.getAllInvestments().map { entities ->
            entities.map { InvestmentMapper.toDomain(it) }
        }

    /** Persist an investment record (insert or replace). */
    override suspend fun saveInvestment(investment: Investment) {
        investmentDao.insert(InvestmentMapper.toEntity(investment))
    }

    /** Update the current market value of an existing investment. */
    override suspend fun updateCurrentValue(id: Long, currentValue: Double) {
        investmentDao.updateCurrentValue(id, currentValue)
    }

    /** Delete an investment record by its primary key. */
    override suspend fun deleteInvestment(id: Long) {
        investmentDao.deleteById(id)
    }
}
