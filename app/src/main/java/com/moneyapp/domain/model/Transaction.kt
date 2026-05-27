package com.moneyapp.domain.model

import java.time.LocalDate

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val date: LocalDate,
    val note: String = "",
    val photoPath: String? = null
)
