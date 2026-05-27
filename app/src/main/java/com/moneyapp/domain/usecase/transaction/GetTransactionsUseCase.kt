package com.moneyapp.domain.usecase.transaction

import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> = transactionRepository.getAllTransactions()
}
