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
import com.trackfi.data.local.TransactionDao

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Success(val summary: FinancialSummaryState, val transactions: List<TransactionEntity>) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val transactionDao: TransactionDao
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

    fun syncMockData() {
        viewModelScope.launch {
            val mockData = listOf(
                TransactionEntity(merchantName = "Mock Store 1", amount = 50.0, type = "EXPENSE", category = "Food", date = System.currentTimeMillis()),
                TransactionEntity(merchantName = "Mock Store 2", amount = 100.0, type = "EXPENSE", category = "Shopping", date = System.currentTimeMillis())
            )
            mockData.forEach { transactionDao.insertTransaction(it) }
        }
    }
}
