package com.rivavafi.universal.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.usecase.FinancialSummaryState
import com.rivavafi.universal.domain.usecase.GetFinancialSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.domain.usecase.GetTransactionsUseCase

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
    private val preferencesRepository: com.rivavafi.universal.data.preferences.UserPreferencesRepository
) : ViewModel() {

    val terminologyMode = preferencesRepository.terminologyModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "CREDIT_DEBIT"
    )

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth = _selectedMonth.asStateFlow()

    fun previousMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, -1)
        _selectedMonth.value = newCal
    }

    fun nextMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, 1)
        _selectedMonth.value = newCal
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getFinancialSummaryUseCase(),
        getTransactionsUseCase(),
        _selectedMonth
    ) { summary, allTransactions, monthCal ->

        val selectedYear = monthCal.get(Calendar.YEAR)
        val selectedMonthNum = monthCal.get(Calendar.MONTH)

        val monthlyTransactions = allTransactions.filter { txn ->
            val txnCal = Calendar.getInstance().apply { timeInMillis = txn.date }
            txnCal.get(Calendar.YEAR) == selectedYear && txnCal.get(Calendar.MONTH) == selectedMonthNum
        }

        val monthlyCredit = monthlyTransactions.filter { it.type == "CREDIT" || it.type == "INCOME" || it.type == "REWARD" || it.type == "INCOME" || it.type == "REWARD" }.sumOf { it.amount }
        val monthlyDebit = monthlyTransactions.filter { it.type == "DEBIT" || it.type == "EXPENSE" || it.type == "BILL_PENDING" || it.type == "EXPENSE" || it.type == "BILL_PENDING" }.sumOf { it.amount }

        // We override the global summary with the monthly specific one for the UI.
        val monthlySummary = com.rivavafi.universal.domain.usecase.FinancialSummaryState(
            totalCredit = monthlyCredit,
            totalDebit = monthlyDebit,
            netSavings = monthlyCredit - monthlyDebit
        )

        if (monthlyTransactions.isEmpty()) {
            AnalyticsUiState.Empty
        } else {
            AnalyticsUiState.Success(monthlySummary, monthlyTransactions)
        }
    }.catch { e ->
        emit(AnalyticsUiState.Error(e.message ?: "An unknown error occurred"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )
}
