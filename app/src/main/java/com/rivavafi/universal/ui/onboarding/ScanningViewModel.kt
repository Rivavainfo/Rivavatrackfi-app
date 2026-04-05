package com.rivavafi.universal.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.usecase.ScanProgress
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.domain.usecase.ScanSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanningViewModel @Inject constructor(
    private val scanSmsUseCase: ScanSmsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow(ScanProgress())
    val scanState: StateFlow<ScanProgress> = _scanState

    init {
        startScan()
    }

    private fun startScan() {
        viewModelScope.launch {
            scanSmsUseCase().collectLatest { progress ->
                _scanState.value = progress
                if (progress.isComplete) {
                    userPreferencesRepository.setSmsScanCompleted(true)
                }
            }
        }
    }
}
