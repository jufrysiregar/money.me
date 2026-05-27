package com.moneyapp.domain.usecase.user

import com.moneyapp.domain.model.User
import com.moneyapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> = userRepository.getUser()
}
