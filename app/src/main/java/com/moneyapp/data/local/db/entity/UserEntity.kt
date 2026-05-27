package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing the single user profile stored in the database.
 *
 * The table always contains exactly one row (id = 1).
 * Satisfies Requirements 1.5 and 6.6.
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1,                                    // Always a single row

    @ColumnInfo(name = "full_name")
    val fullName: String
)
