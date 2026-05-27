package com.moneyapp.data.repository

import com.moneyapp.data.local.db.dao.SavingDao
import com.moneyapp.data.mapper.SavingMapper
import com.moneyapp.domain.model.Saving
import com.moneyapp.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [SavingRepository] backed by Room via [SavingDao].
 *
 * Satisfies Requirements 4.4, 9.3.
 */
@Singleton
class SavingRepositoryImpl @Inject constructor(
    private val savingDao: SavingDao
) : SavingRepository {

    /** Observe all savings goal records ordered by target date ascending. */
    override fun getAllSavings(): Flow<List<Saving>> =
        savingDao.getAllSavings().map { entities ->
            entities.map { SavingMapper.toDomain(it) }
        }

    /** Persist a savings goal record (insert or replace). */
    override suspend fun saveSaving(saving: Saving) {
        savingDao.insert(SavingMapper.toEntity(saving))
    }

    /** Update the current saved amount for an existing savings goal. */
    override suspend fun updateCurrentAmount(id: Long, currentAmount: Double) {
        savingDao.updateCurrentAmount(id, currentAmount)
    }

    /** Delete a savings goal record by its primary key. */
    override suspend fun deleteSaving(id: Long) {
        savingDao.deleteById(id)
    }
}
