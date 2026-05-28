package com.moneyapp.presentation.screen.saving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Saving
import com.moneyapp.domain.repository.SavingRepository
import com.moneyapp.domain.usecase.saving.GetSavingsUseCase
import com.moneyapp.domain.usecase.saving.SaveSavingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class SavingUiEvent {
    object Success : SavingUiEvent()
    data class Error(val message: String) : SavingUiEvent()
}

@HiltViewModel
class SavingViewModel @Inject constructor(
    private val getSavingsUseCase: GetSavingsUseCase,
    private val saveSavingUseCase: SaveSavingUseCase,
    private val savingRepository: SavingRepository
) : ViewModel() {

    val savings: StateFlow<List<Saving>> = getSavingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiEvent = Channel<SavingUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun saveSaving(targetName: String, targetAmount: Double, currentAmount: Double, targetDate: LocalDate) {
        viewModelScope.launch {
            if (targetName.isBlank() || targetAmount <= 0) {
                _uiEvent.send(SavingUiEvent.Error("Nama target dan nominal target harus lebih dari 0"))
                return@launch
            }

            val saving = Saving(
                targetName = targetName.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate
            )

            when (val result = saveSavingUseCase(saving)) {
                is AppResult.Success -> _uiEvent.send(SavingUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(SavingUiEvent.Error(result.message))
            }
        }
    }

    fun topUpSaving(id: Long, currentAmount: Double, topUpAmount: Double) {
        viewModelScope.launch {
            if (topUpAmount <= 0) {
                _uiEvent.send(SavingUiEvent.Error("Nominal top up harus lebih dari 0"))
                return@launch
            }
            try {
                val newAmount = currentAmount + topUpAmount
                savingRepository.updateCurrentAmount(id, newAmount)
                _uiEvent.send(SavingUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(SavingUiEvent.Error("Gagal menambah tabungan"))
            }
        }
    }

    fun updateSavingGoal(id: Long, topUpAmount: Double, targetAmount: Double?) {
        viewModelScope.launch {
            if (topUpAmount < 0) {
                _uiEvent.send(SavingUiEvent.Error("Nominal top up tidak boleh negatif"))
                return@launch
            }

            val existing = savings.value.firstOrNull { it.id == id }
            if (existing == null) {
                _uiEvent.send(SavingUiEvent.Error("Target tabungan tidak ditemukan"))
                return@launch
            }

            val newTargetAmount = targetAmount ?: existing.targetAmount
            if (newTargetAmount <= 0) {
                _uiEvent.send(SavingUiEvent.Error("Target nominal harus lebih dari 0"))
                return@launch
            }

            val updatedSaving = existing.copy(
                targetAmount = newTargetAmount,
                currentAmount = existing.currentAmount + topUpAmount
            )

            when (val result = saveSavingUseCase(updatedSaving)) {
                is AppResult.Success -> _uiEvent.send(SavingUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(SavingUiEvent.Error(result.message))
            }
        }
    }

    fun deleteSaving(id: Long) {
        viewModelScope.launch {
            try {
                savingRepository.deleteSaving(id)
                _uiEvent.send(SavingUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(SavingUiEvent.Error("Gagal menghapus tabungan"))
            }
        }
    }
}
