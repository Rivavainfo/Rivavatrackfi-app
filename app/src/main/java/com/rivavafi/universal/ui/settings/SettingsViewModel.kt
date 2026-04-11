package com.rivavafi.universal.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.domain.repository.TransactionRepository
import com.rivavafi.universal.domain.usecase.ExportCsvUseCase
import com.rivavafi.universal.domain.usecase.ImportCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val importCsvUseCase: ImportCsvUseCase,
    private val userCorrectionDao: com.rivavafi.universal.data.local.UserCorrectionDao
) : ViewModel() {

    val isSmsTrackingEnabled = preferencesRepository.isSmsTrackingEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val homeLayoutPreset = preferencesRepository.homeLayoutPresetFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Daily Tracker"
    )

    val showSmsDetails = preferencesRepository.showSmsDetailsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    private val _banksDetected = MutableStateFlow<List<String>>(emptyList())
    val banksDetected: StateFlow<List<String>> = _banksDetected

    init {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                _banksDetected.value = transactions.mapNotNull { it.bankName }.distinct().sorted()
            }
        }
    }

    fun setSmsTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmsTrackingEnabled(enabled)
        }
    }

    fun setHomeLayoutPreset(preset: String) {
        viewModelScope.launch {
            preferencesRepository.setHomeLayoutPreset(preset)
        }
    }

    fun setShowSmsDetails(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowSmsDetails(show)
        }
    }

    fun exportCsv(context: Context, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = exportCsvUseCase(context)
            onResult(result)
        }
    }

    fun importCsv(context: Context, uri: Uri, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = importCsvUseCase(context, uri)
            onResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            preferencesRepository.setOnboardingCompleted(false)
            preferencesRepository.saveUserName("")
        }
    }

    fun clearTransactionHistory() {
        viewModelScope.launch {
            transactionRepository.deleteAllTransactions()
        }
    }

    fun clearAiLearning() {
        viewModelScope.launch {
            userCorrectionDao.clearAllCorrections()
        }
    }
}
