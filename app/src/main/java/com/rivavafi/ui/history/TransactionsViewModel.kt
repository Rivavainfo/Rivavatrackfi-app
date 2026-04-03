package com.rivavafi.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.data.local.TransactionEntity
import com.rivavafi.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.rivavafi.domain.repository.TransactionRepository
import com.rivavafi.data.local.UserCorrectionDao
import com.rivavafi.data.local.UserCorrectionEntity
import com.rivavafi.data.local.CategoryEntity
import com.rivavafi.domain.usecase.GetCategoriesUseCase
import com.rivavafi.domain.usecase.AddCategoryUseCase
import com.rivavafi.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed class TransactionsUiState {
    object Loading : TransactionsUiState()
    object Empty : TransactionsUiState()
    data class Success(val transactions: List<TransactionEntity>) : TransactionsUiState()
    data class Error(val message: String) : TransactionsUiState()
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val repository: TransactionRepository,
    private val userCorrectionDao: UserCorrectionDao,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun getTransactionById(id: Long) = repository.getTransactionById(id)

    fun updateTransaction(transaction: TransactionEntity, createRule: Boolean = true, ruleKeyword: String? = null) {
        viewModelScope.launch {
            // Save transaction changes
            repository.addTransaction(transaction)

            if (createRule) {
                // Layer 3 Learning: Save user correction mapping so future SMS gets categorized correctly
                val correction = UserCorrectionEntity(
                    merchantName = transaction.merchantName,
                    category = transaction.category,
                    subcategory = transaction.subcategory,
                    keyword = ruleKeyword
                )
                userCorrectionDao.insertCorrection(correction)
            }
        }
    }

    private val _uiState = MutableStateFlow<TransactionsUiState>(TransactionsUiState.Loading)
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    val showSmsDetails = userPreferencesRepository.showSmsDetailsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    private var allTransactions: List<TransactionEntity> = emptyList()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase.initialize()
            getCategoriesUseCase().collectLatest {
                _categories.value = it
            }
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            addCategoryUseCase(CategoryEntity(name = name, type = type))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
        applyFilters()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collectLatest { transactions ->
                allTransactions = transactions
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        var filteredList = allTransactions
        val query = _searchQuery.value.trim().lowercase()

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.merchantName.lowercase().contains(query) ||
                it.category.lowercase().contains(query) ||
                it.amount.toString().contains(query)
            }
        }

        filteredList = when (_sortOrder.value) {
            SortOrder.DATE_DESC -> filteredList.sortedByDescending { it.date }
            SortOrder.DATE_ASC -> filteredList.sortedBy { it.date }
            SortOrder.AMOUNT_DESC -> filteredList.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> filteredList.sortedBy { it.amount }
            SortOrder.MERCHANT_ASC -> filteredList.sortedBy { it.merchantName.lowercase() }
            SortOrder.CATEGORY_ASC -> filteredList.sortedBy { it.category.lowercase() }
        }

        if (filteredList.isEmpty()) {
            _uiState.value = TransactionsUiState.Empty
        } else {
            _uiState.value = TransactionsUiState.Success(filteredList)
        }
    }
}

enum class SortOrder {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, MERCHANT_ASC, CATEGORY_ASC
}
