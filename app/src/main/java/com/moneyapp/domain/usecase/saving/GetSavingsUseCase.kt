package com.moneyapp.domain.usecase.saving

import com.moneyapp.domain.model.Saving
import com.moneyapp.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavingsUseCase @Inject constructor(
    private val savingRepository: SavingRepository
) {
    operator fun invoke(): Flow<List<Saving>> = savingRepository.getAllSavings()
}
