package com.moneyapp.domain.model

import java.time.LocalDate

data class Investment(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currentValue: Double,
    val date: LocalDate
)
