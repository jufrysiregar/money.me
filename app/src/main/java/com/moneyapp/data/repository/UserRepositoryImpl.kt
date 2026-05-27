package com.moneyapp.data.repository

import com.moneyapp.data.local.db.dao.UserDao
import com.moneyapp.data.mapper.UserMapper
import com.moneyapp.domain.model.User
import com.moneyapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [UserRepository] backed by Room via [UserDao].
 *
 * Satisfies Requirements 4.4, 6.5.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    /**
     * Observe the current user profile as a reactive [Flow].
     * Emits null if no user has been saved yet (first launch).
     */
    override fun getUser(): Flow<User?> =
        userDao.getUser().map { entity ->
            entity?.let { UserMapper.toDomain(it) }
        }

    /**
     * Persist the user profile (insert or replace).
     */
    override suspend fun saveUser(user: User) {
        userDao.insert(UserMapper.toEntity(user))
    }
}
