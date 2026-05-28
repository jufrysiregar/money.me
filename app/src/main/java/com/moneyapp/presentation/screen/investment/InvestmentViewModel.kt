package com.moneyapp.presentation.screen.investment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Investment
import com.moneyapp.domain.repository.InvestmentRepository
import com.moneyapp.domain.usecase.investment.GetInvestmentsUseCase
import com.moneyapp.domain.usecase.investment.SaveInvestmentUseCase
import com.moneyapp.domain.usecase.investment.UpdateInvestmentValueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class InvestmentUiEvent {
    object Success : InvestmentUiEvent()
    data class Error(val message: String) : InvestmentUiEvent()
}

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val getInvestmentsUseCase: GetInvestmentsUseCase,
    private val saveInvestmentUseCase: SaveInvestmentUseCase,
    private val updateInvestmentValueUseCase: UpdateInvestmentValueUseCase,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    val investments: StateFlow<List<Investment>> = getInvestmentsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiEvent = Channel<InvestmentUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun saveInvestment(name: String, amount: Double, currentValue: Double, date: LocalDate) {
        viewModelScope.launch {
            if (name.isBlank() || amount <= 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nama tidak boleh kosong dan nominal harus lebih dari 0"))
                return@launch
            }

            val investment = Investment(
                name = name.trim(),
                amount = amount,
                currentValue = currentValue,
                date = date
            )

            when (val result = saveInvestmentUseCase(investment)) {
                is AppResult.Success -> _uiEvent.send(InvestmentUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }
        }
    }

    fun updateCurrentValue(id: Long, value: Double) {
        viewModelScope.launch {
            if (value < 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nilai sekarang tidak boleh negatif"))
                return@launch
            }
            when (val result = updateInvestmentValueUseCase(id, value)) {
                is AppResult.Success -> _uiEvent.send(InvestmentUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }
        }
    }

    fun updateInvestment(id: Long, additionalAmount: Double, currentValue: Double) {
        viewModelScope.launch {
            if (additionalAmount < 0 || currentValue < 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nominal tidak boleh negatif"))
                return@launch
            }

            val existing = investments.value.firstOrNull { it.id == id }
            if (existing == null) {
                _uiEvent.send(InvestmentUiEvent.Error("Investasi tidak ditemukan"))
                return@launch
            }

            val updatedInvestment = existing.copy(
                amount = existing.amount + additionalAmount,
                currentValue = currentValue
            )

            when (val result = saveInvestmentUseCase(updatedInvestment)) {
                is AppResult.Success -> _uiEvent.send(InvestmentUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }
        }
    }

    fun deleteInvestment(id: Long) {
        viewModelScope.launch {
            try {
                investmentRepository.deleteInvestment(id)
                _uiEvent.send(InvestmentUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(InvestmentUiEvent.Error("Gagal menghapus investasi"))
            }
        }
    }
}
