package com.rivavafi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.data.local.TransactionEntity
import com.rivavafi.domain.usecase.AddTransactionUseCase
import com.rivavafi.domain.usecase.FinancialSummaryState
import com.rivavafi.domain.usecase.GetFinancialSummaryUseCase
import com.rivavafi.data.local.CategoryEntity
import com.rivavafi.domain.usecase.AddCategoryUseCase
import com.rivavafi.data.preferences.UserPreferencesRepository
import com.rivavafi.domain.usecase.GetCategoriesUseCase
import com.rivavafi.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _dailyBudget = MutableStateFlow(800.0)
    val dailyBudget: StateFlow<Double> = _dailyBudget

    private val _homeLayoutPreset = MutableStateFlow("Daily Tracker")
    val homeLayoutPreset: StateFlow<String> = _homeLayoutPreset

    private val _summary = MutableStateFlow(FinancialSummaryState())
    val summary: StateFlow<FinancialSummaryState> = _summary

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    val showSmsDetails = userPreferencesRepository.showSmsDetailsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val userName = userPreferencesRepository.userNameFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val isSmsTrackingEnabled = userPreferencesRepository.isSmsTrackingEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val isPremiumUser = userPreferencesRepository.isPremiumUserFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val profileImageUri = userPreferencesRepository.profileImageUriFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.dailyBudgetFlow.collectLatest {
                _dailyBudget.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.homeLayoutPresetFlow.collectLatest {
                _homeLayoutPreset.value = it
            }
        }
        viewModelScope.launch {
            getCategoriesUseCase.initialize()
            getCategoriesUseCase().collectLatest {
                _categories.value = it
            }
        }
        viewModelScope.launch {
            getTransactionsUseCase().collectLatest {
                _transactions.value = it
            }
        }
        viewModelScope.launch {
            getFinancialSummaryUseCase().collectLatest {
                _summary.value = it
            }
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            addCategoryUseCase(
                CategoryEntity(
                    name = name,
                    type = type
                )
            )
        }
    }

    fun updateDailyBudget(budget: Double) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyBudget(budget)
        }
    }

    fun updateHomeLayoutPreset(preset: String) {
        viewModelScope.launch {
            userPreferencesRepository.setHomeLayoutPreset(preset)
        }
    }

    fun setPremiumUser(isPremium: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPremiumUserForCurrent(isPremium)
        }
    }

    fun setProfileImageUri(uri: String) {
        viewModelScope.launch {
            userPreferencesRepository.setProfileImageUri(uri)
        }
    }

    fun setSmsTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSmsTrackingEnabled(enabled)
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch {
            addTransactionUseCase(
                TransactionEntity(
                    merchantName = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = System.currentTimeMillis()
                )
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            userPreferencesRepository.setOnboardingCompleted(false)
            userPreferencesRepository.saveUserName("")
        }
    }
}
