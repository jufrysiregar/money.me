package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a financial transaction (income or expense).
 *
 * - [type] must be either "income" or "expense".
 * - [date] is stored as an ISO-8601 string in "yyyy-MM-dd" format.
 * - [photoPath] is optional; stores the local file path of an attached photo.
 *
 * Satisfies Requirements 6.6.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,                           // "income" | "expense"

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "date")
    val date: String,                           // ISO-8601: "yyyy-MM-dd"

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null
)
