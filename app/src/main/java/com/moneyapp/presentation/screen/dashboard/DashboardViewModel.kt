package com.moneyapp.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.domain.model.DashboardSummary
import com.moneyapp.domain.usecase.dashboard.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {

    /**
     * StateFlow yang memancarkan DashboardSummary terbaru secara reaktif.
     * Diperbarui otomatis setiap kali data di repository berubah.
     *
     * Validates: Requirements 2.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4
     */
    val dashboardSummary: StateFlow<DashboardSummary?> = getDashboardSummaryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
