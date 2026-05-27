package com.moneyapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyapp.data.local.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying the single user profile row.
 *
 * The "user" table always contains at most one row (id = 1).
 * Satisfies Requirements 1.2, 1.5.
 */
@Dao
interface UserDao {

    /**
     * Observe the current user profile.
     * Emits null if no user has been saved yet (first launch).
     */
    @Query("SELECT * FROM user WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    /**
     * Insert or replace the user profile.
     * Because id is always 1, this effectively upserts the single row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserEntity)
}
