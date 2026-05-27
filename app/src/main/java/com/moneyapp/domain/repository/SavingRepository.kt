package com.moneyapp.domain.repository

import com.moneyapp.domain.model.Saving
import kotlinx.coroutines.flow.Flow

interface SavingRepository {
    fun getAllSavings(): Flow<List<Saving>>
    suspend fun saveSaving(saving: Saving)
    suspend fun updateCurrentAmount(id: Long, currentAmount: Double)
    suspend fun deleteSaving(id: Long)
}
