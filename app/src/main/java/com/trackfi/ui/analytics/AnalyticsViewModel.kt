package com.trackfi.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackfi.domain.usecase.FinancialSummaryState
import com.trackfi.domain.usecase.GetFinancialSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.trackfi.data.local.TransactionEntity
import com.trackfi.domain.usecase.GetTransactionsUseCase

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Success(val summary: FinancialSummaryState, val transactions: List<TransactionEntity>) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getFinancialSummaryUseCase(),
        getTransactionsUseCase()
    ) { summary, transactions ->
        if (summary.totalIncome == 0.0 && summary.totalExpense == 0.0 && transactions.isEmpty()) {
            AnalyticsUiState.Empty
        } else {
            AnalyticsUiState.Success(summary, transactions)
        }
    }.catch { e ->
        emit(AnalyticsUiState.Error(e.message ?: "An unknown error occurred"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )
}
