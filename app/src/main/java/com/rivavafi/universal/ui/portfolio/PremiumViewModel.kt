package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.repository.PremiumState
import com.rivavafi.universal.data.repository.UserEntitlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: UserEntitlementRepository
) : ViewModel() {

    val premiumState: StateFlow<PremiumState> = repository.premiumState

    init {
        viewModelScope.launch {
            repository.syncEntitlement()
        }
    }

    fun syncEntitlement() {
        viewModelScope.launch {
            repository.syncEntitlement()
        }
    }

    fun grantPremium(source: String, txnId: String? = null) {
        viewModelScope.launch {
            repository.grantPremium(source, txnId)
        }
    }
}
