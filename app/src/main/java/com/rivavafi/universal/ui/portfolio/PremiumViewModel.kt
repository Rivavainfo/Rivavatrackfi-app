package com.rivavafi.universal.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.data.repository.PremiumState
import com.rivavafi.universal.data.repository.RazorpayOrderResult
import com.rivavafi.universal.data.repository.UserEntitlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentUiState {
    IDLE, CREATING_ORDER, CHECKOUT_READY, VERIFYING, SUCCESS, ERROR
}

data class PaymentState(
    val uiState: PaymentUiState = PaymentUiState.IDLE,
    val orderData: RazorpayOrderResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: UserEntitlementRepository
) : ViewModel() {

    val premiumState: StateFlow<PremiumState> = repository.premiumState

    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

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

    fun startPremiumPurchase(amountPaise: Int = 1100) {
        if (_paymentState.value.uiState == PaymentUiState.CREATING_ORDER ||
            _paymentState.value.uiState == PaymentUiState.VERIFYING) return

        _paymentState.value = PaymentState(uiState = PaymentUiState.CREATING_ORDER)

        viewModelScope.launch {
            val result = repository.createRazorpayOrder(amountPaise)
            if (result.success && result.orderId != null) {
                _paymentState.value = PaymentState(uiState = PaymentUiState.CHECKOUT_READY, orderData = result)
            } else {
                _paymentState.value = PaymentState(
                    uiState = PaymentUiState.ERROR,
                    errorMessage = result.error ?: "Failed to create order"
                )
            }
        }
    }

    fun unlockPremiumWithKey(key: String) {
        if (_paymentState.value.uiState == PaymentUiState.VERIFYING) return

        _paymentState.value = PaymentState(uiState = PaymentUiState.VERIFYING)
        viewModelScope.launch {
            val success = repository.unlockPremiumWithKey(key)
            _paymentState.value = if (success) {
                PaymentState(uiState = PaymentUiState.SUCCESS)
            } else {
                PaymentState(
                    uiState = PaymentUiState.ERROR,
                    errorMessage = "Invalid premium key"
                )
            }
        }
    }

    fun verifyRazorpayPayment(orderId: String, paymentId: String, signature: String) {
        if (_paymentState.value.uiState == PaymentUiState.VERIFYING) return

        _paymentState.value = _paymentState.value.copy(uiState = PaymentUiState.VERIFYING)

        viewModelScope.launch {
            val success = repository.verifyRazorpayPayment(orderId, paymentId, signature)
            if (success) {
                _paymentState.value = PaymentState(uiState = PaymentUiState.SUCCESS)
            } else {
                _paymentState.value = PaymentState(
                    uiState = PaymentUiState.ERROR,
                    errorMessage = "Payment verification failed"
                )
            }
        }
    }

    fun clearPaymentError() {
        if (_paymentState.value.uiState == PaymentUiState.ERROR || _paymentState.value.uiState == PaymentUiState.CHECKOUT_READY) {
            _paymentState.value = PaymentState(uiState = PaymentUiState.IDLE)
        }
    }
}
