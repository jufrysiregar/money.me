package com.moneyapp.domain.usecase.user

import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.User
import com.moneyapp.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Menyimpan data user ke repository.
     * Validasi: nama tidak boleh kosong atau hanya whitespace.
     *
     * Validates: Requirements 1.2, 1.3
     */
    suspend operator fun invoke(user: User): AppResult<Unit> {
        if (user.fullName.isBlank()) {
            return AppResult.Error("Nama tidak boleh kosong")
        }
        return try {
            userRepository.saveUser(user)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Gagal menyimpan data pengguna", e)
        }
    }
}
