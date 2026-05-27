package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an investment record.
 *
 * - [amount] is the initial capital invested.
 * - [currentValue] is the current market value of the investment.
 * - [date] is stored as an ISO-8601 string in "yyyy-MM-dd" format.
 *
 * Satisfies Requirements 8.4.
 */
@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "current_value")
    val currentValue: Double,

    @ColumnInfo(name = "date")
    val date: String                            // ISO-8601: "yyyy-MM-dd"
)
