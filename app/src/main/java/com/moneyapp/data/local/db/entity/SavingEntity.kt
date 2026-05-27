package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a savings goal record.
 *
 * - [targetAmount] is the total amount the user wants to save.
 * - [currentAmount] is the amount saved so far.
 * - [targetDate] is the deadline stored as an ISO-8601 string in "yyyy-MM-dd" format.
 *
 * Satisfies Requirements 9.4.
 */
@Entity(tableName = "savings")
data class SavingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "target_name")
    val targetName: String,

    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,

    @ColumnInfo(name = "current_amount")
    val currentAmount: Double,

    @ColumnInfo(name = "target_date")
    val targetDate: String                      // ISO-8601: "yyyy-MM-dd"
)
