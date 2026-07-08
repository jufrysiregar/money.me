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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // ============================================================
    //  STATE
    // ============================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _investments = MutableStateFlow<List<Investment>>(emptyList())
    val investments: StateFlow<List<Investment>> = _investments

    // Gabungan dari aktif + sudah dijual
    val allInvestments: StateFlow<List<Investment>> = combine(
        investmentRepository.getActiveInvestments().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        ),
        investmentRepository.getSoldInvestments().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    ) { active, sold ->
        active + sold
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val activeInvestments: StateFlow<List<Investment>> = investmentRepository.getActiveInvestments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val soldInvestments: StateFlow<List<Investment>> = investmentRepository.getSoldInvestments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiEvent = Channel<InvestmentUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // ============================================================
    //  FUNGSI SAVE INVESTMENT (FORM BARU)
    // ============================================================

    /**
     * Simpan investasi saham baru.
     *
     * @param name Nama saham (wajib)
     * @param averagePrice Harga rata-rata beli per lembar (opsional)
     * @param currentPrice Harga saham saat ini (opsional)
     * @param totalAmount Total uang yang diinvestasikan (wajib)
     * @param date Tanggal beli (opsional, default hari ini)
     * @param notes Catatan (opsional)
     * @param photoPath Path foto (opsional)
     */
    fun saveInvestmentNew(
        name: String,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double,
        date: LocalDate = LocalDate.now(),
        notes: String? = null,
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            if (name.isBlank()) {
                _uiEvent.send(InvestmentUiEvent.Error("Nama saham tidak boleh kosong"))
                _isLoading.value = false
                return@launch
            }
            if (totalAmount <= 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Total uang investasi harus lebih dari 0"))
                _isLoading.value = false
                return@launch
            }

            val now = LocalDateTime.now().format(timestampFormatter)
            val investment = Investment(
                name = name.trim().uppercase(),
                amount = totalAmount,
                currentValue = currentPrice ?: 0.0,
                date = date,
                notes = notes?.trim()?.ifBlank { null },
                photoPath = photoPath,
                averagePrice = averagePrice,
                currentPrice = currentPrice,
                totalAmount = totalAmount,
                isSold = false,
                soldDate = null,
                soldPrice = null,
                createdAt = now,
                updatedAt = now
            )

            when (val result = saveInvestmentUseCase(investment)) {
                is AppResult.Success -> _uiEvent.send(InvestmentUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }

            _isLoading.value = false
        }
    }

    /**
     * Update data investasi yang sudah ada.
     *
     * @param investment Investment lama (akan di-copy dengan data baru)
     * @param name Nama saham baru (opsional, jika null pakai yang lama)
     * @param averagePrice Harga rata-rata beli baru (opsional)
     * @param currentPrice Harga saat ini baru (opsional)
     * @param totalAmount Total uang baru (opsional)
     * @param date Tanggal baru (opsional)
     * @param notes Catatan baru (opsional)
     */
    fun updateInvestmentFull(
        investment: Investment,
        name: String?,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?,
        date: LocalDate?,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val newName = name?.trim()?.uppercase()?.ifBlank { null } ?: investment.name
            val newTotal = totalAmount ?: investment.totalAmount ?: investment.amount

            if (newTotal <= 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Total uang investasi harus lebih dari 0"))
                _isLoading.value = false
                return@launch
            }

            val now = LocalDateTime.now().format(timestampFormatter)
            val updated = investment.copy(
                name = newName,
                amount = newTotal,
                currentValue = currentPrice ?: investment.currentValue,
                date = date ?: investment.date,
                notes = notes?.trim()?.ifBlank { null } ?: investment.notes,
                averagePrice = averagePrice ?: investment.averagePrice,
                currentPrice = currentPrice ?: investment.currentPrice,
                totalAmount = newTotal,
                updatedAt = now
            )

            try {
                investmentRepository.updateInvestment(updated)
                _uiEvent.send(InvestmentUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(InvestmentUiEvent.Error("Gagal update investasi: ${e.message}"))
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    //  FUNGSI SAVE INVESTMENT (BACKWARD COMPATIBLE — LAMA)
    // ============================================================

    /** Save investment (backward compatible with existing form). */
    fun saveInvestment(name: String, amount: Double, currentValue: Double, date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true

            if (name.isBlank() || amount <= 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nama tidak boleh kosong dan nominal harus lebih dari 0"))
                _isLoading.value = false
                return@launch
            }

            val investment = Investment(
                name = name.trim(),
                amount = amount,
                currentValue = currentValue,
                date = date,
                averagePrice = null,
                currentPrice = null,
                totalAmount = null,
                isSold = false,
                soldDate = null,
                soldPrice = null
            )

            when (val result = saveInvestmentUseCase(investment)) {
                is AppResult.Success -> {
                    _uiEvent.send(InvestmentUiEvent.Success)
                    refreshInvestments()
                }
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }

            _isLoading.value = false
        }
    }

    /** Save investment with full data (new columns). */
    fun saveInvestmentFull(
        name: String,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?,
        date: LocalDate,
        notes: String? = null,
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            if (name.isBlank()) {
                _uiEvent.send(InvestmentUiEvent.Error("Nama investasi tidak boleh kosong"))
                _isLoading.value = false
                return@launch
            }

            val amount = totalAmount ?: 0.0
            val currentValue = currentPrice ?: 0.0

            val investment = Investment(
                name = name.trim(),
                amount = amount,
                currentValue = currentValue,
                date = date,
                notes = notes,
                photoPath = photoPath,
                averagePrice = averagePrice,
                currentPrice = currentPrice,
                totalAmount = totalAmount,
                isSold = false,
                soldDate = null,
                soldPrice = null
            )

            when (val result = saveInvestmentUseCase(investment)) {
                is AppResult.Success -> {
                    _uiEvent.send(InvestmentUiEvent.Success)
                    refreshInvestments()
                }
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    //  FUNGSI UPDATE
    // ============================================================

    /** Update current value only (backward compatibility). */
    fun updateCurrentValue(id: Long, value: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            if (value < 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nilai sekarang tidak boleh negatif"))
                _isLoading.value = false
                return@launch
            }

            when (val result = updateInvestmentValueUseCase(id, value)) {
                is AppResult.Success -> {
                    _uiEvent.send(InvestmentUiEvent.Success)
                    refreshInvestments()
                }
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }

            _isLoading.value = false
        }
    }

    /** Update investment details (average price, current price, total amount). */
    fun updateInvestmentDetails(
        id: Long,
        averagePrice: Double?,
        currentPrice: Double?,
        totalAmount: Double?
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                investmentRepository.updateInvestmentDetails(id, averagePrice, currentPrice, totalAmount)
                _uiEvent.send(InvestmentUiEvent.Success)
                refreshInvestments()
            } catch (e: Exception) {
                _uiEvent.send(InvestmentUiEvent.Error("Gagal update investasi: ${e.message}"))
            }

            _isLoading.value = false
        }
    }

    /** Update investment (backward compatibility with additionalAmount). */
    fun updateInvestment(id: Long, additionalAmount: Double, currentValue: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            if (additionalAmount < 0 || currentValue < 0) {
                _uiEvent.send(InvestmentUiEvent.Error("Nominal tidak boleh negatif"))
                _isLoading.value = false
                return@launch
            }

            val existing = investments.value.firstOrNull { it.id == id }
            if (existing == null) {
                _uiEvent.send(InvestmentUiEvent.Error("Investasi tidak ditemukan"))
                _isLoading.value = false
                return@launch
            }

            val updatedInvestment = existing.copy(
                amount = existing.amount + additionalAmount,
                currentValue = currentValue
            )

            when (val result = saveInvestmentUseCase(updatedInvestment)) {
                is AppResult.Success -> {
                    _uiEvent.send(InvestmentUiEvent.Success)
                    refreshInvestments()
                }
                is AppResult.Error -> _uiEvent.send(InvestmentUiEvent.Error(result.message))
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    //  FUNGSI JUAL INVESTASI
    // ============================================================

    /**
     * Sell investment: mark as sold and create income transaction.
     *
     * @param investment Investment yang akan dijual
     * @param soldPrice Harga jual per lembar (jika null, gunakan currentPrice)
     */
    fun sellInvestment(investment: Investment, soldPrice: Double? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Gunakan harga jual yang diberikan, atau harga saat ini, atau 0
                val effectiveSoldPrice = soldPrice
                    ?: investment.currentPrice
                    ?: investment.currentValue

                investmentRepository.sellInvestment(
                    id = investment.id,
                    soldPrice = effectiveSoldPrice,
                    soldDate = LocalDate.now()
                )
                _uiEvent.send(InvestmentUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(InvestmentUiEvent.Error("Gagal menjual investasi: ${e.message}"))
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    //  FUNGSI DELETE
    // ============================================================

    fun deleteInvestment(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                investmentRepository.deleteInvestment(id)
                _uiEvent.send(InvestmentUiEvent.Success)
                refreshInvestments()
            } catch (e: Exception) {
                _uiEvent.send(InvestmentUiEvent.Error("Gagal menghapus investasi"))
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    //  FUNGSI HELPER
    // ============================================================

    private fun refreshInvestments() {
        viewModelScope.launch {
            investmentRepository.getAllInvestments().collect { list ->
                _investments.value = list
            }
        }
    }

    fun refresh() {
        refreshInvestments()
    }
}