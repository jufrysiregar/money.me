package com.moneyapp.domain.usecase.investment

import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvestmentsUseCase @Inject constructor(
    private val investmentRepository: InvestmentRepository
) {
    operator fun invoke(): Flow<List<Investment>> = investmentRepository.getAllInvestments()
}
