package com.moneyapp.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.User
import com.moneyapp.domain.usecase.user.SaveUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val nameError: String? = null,
        val isLoading: Boolean = false,
        val navigateToDashboard: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            val name = _uiState.value.name.trim()
            if (name.isBlank()) {
                _uiState.update { it.copy(nameError = "Nama tidak boleh kosong") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            when (val result = saveUserUseCase(User(fullName = name))) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, navigateToDashboard = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, nameError = result.message)
                }
            }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToDashboard = false) }
    }
}
