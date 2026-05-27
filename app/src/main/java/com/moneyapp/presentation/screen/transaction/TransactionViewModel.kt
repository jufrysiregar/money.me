package com.moneyapp.presentation.screen.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.model.TransactionType
import com.moneyapp.domain.repository.TransactionRepository
import com.moneyapp.domain.usecase.transaction.GetTransactionsUseCase
import com.moneyapp.domain.usecase.transaction.SaveTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * State untuk form input transaksi.
 *
 * Validates: Requirements 6.1
 */
data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val category: String = "",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val photoPath: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false
)

/**
 * Event satu kali untuk navigasi dan notifikasi UI.
 */
sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    /**
     * Daftar seluruh transaksi yang diperbarui secara reaktif dari database.
     *
     * Validates: Requirements 6.5
     */
    val transactions: StateFlow<List<Transaction>> = getTransactionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * State form transaksi untuk input pengguna.
     *
     * Validates: Requirements 6.1
     */
    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    /**
     * Channel untuk event satu kali (navigasi, snackbar).
     */
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // ── Form field updaters ──────────────────────────────────────────────────

    fun onTypeChange(type: TransactionType) {
        _formState.update { it.copy(type = type) }
    }

    fun onAmountChange(amount: String) {
        _formState.update { it.copy(amount = amount, amountError = null) }
    }

    fun onCategoryChange(category: String) {
        _formState.update { it.copy(category = category, categoryError = null) }
    }

    fun onDateChange(date: LocalDate) {
        _formState.update { it.copy(date = date) }
    }

    fun onNoteChange(note: String) {
        _formState.update { it.copy(note = note) }
    }

    fun onPhotoPathChange(photoPath: String?) {
        _formState.update { it.copy(photoPath = photoPath) }
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    /**
     * Menyimpan transaksi setelah validasi:
     * - amount harus > 0
     * - category tidak boleh kosong
     *
     * Validates: Requirements 6.3, 6.4, 6.5
     */
    fun saveTransaction() {
        viewModelScope.launch {
            val state = _formState.value

            // Validasi amount
            val amountValue = state.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                _formState.update {
                    it.copy(amountError = "Nominal tidak boleh kosong atau nol")
                }
                return@launch
            }

            // Validasi category
            if (state.category.isBlank()) {
                _formState.update {
                    it.copy(categoryError = "Kategori tidak boleh kosong")
                }
                return@launch
            }

            _formState.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                type = state.type,
                amount = amountValue,
                category = state.category.trim(),
                date = state.date,
                note = state.note.trim(),
                photoPath = state.photoPath
            )

            when (val result = saveTransactionUseCase(transaction)) {
                is AppResult.Success -> {
                    _formState.update { it.copy(isLoading = false) }
                    _uiEvent.send(UiEvent.NavigateBack)
                }
                is AppResult.Error -> {
                    _formState.update { it.copy(isLoading = false) }
                    _uiEvent.send(UiEvent.ShowError(result.message))
                }
            }
        }
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     *
     * Validates: Requirements 6.5
     */
    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(id)
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowError("Gagal menghapus transaksi"))
            }
        }
    }

    /**
     * Reset form ke state awal (digunakan saat membuka form baru).
     */
    fun resetForm() {
        _formState.update { TransactionFormState() }
    }
}
