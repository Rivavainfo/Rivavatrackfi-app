package com.trackfi.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackfi.data.preferences.UserPreferencesRepository
import com.trackfi.domain.repository.TransactionRepository
import com.trackfi.domain.usecase.ExportCsvUseCase
import com.trackfi.domain.usecase.ImportCsvUseCase
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
    private val userCorrectionDao: com.trackfi.data.local.UserCorrectionDao
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

    private val _smartCategorizationEnabled = MutableStateFlow(true)
    val smartCategorizationEnabled: StateFlow<Boolean> = _smartCategorizationEnabled

    private val _biometricLockEnabled = MutableStateFlow(true)
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled

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

    fun setSmartCategorizationEnabled(enabled: Boolean) {
        _smartCategorizationEnabled.value = enabled
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        _biometricLockEnabled.value = enabled
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
