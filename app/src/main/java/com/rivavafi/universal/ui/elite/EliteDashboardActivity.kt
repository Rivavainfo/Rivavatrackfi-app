package com.rivavafi.universal.ui.elite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.rivavafi.universal.data.repository.EliteRepository
import com.rivavafi.universal.ui.portfolio.PaymentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EliteDashboardActivity : ComponentActivity() {

    @Inject
    lateinit var eliteRepository: EliteRepository

    private var isProcessingPayment by mutableStateOf(false)

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val orderId = currentOrderId
            if (orderId != null) {
                isProcessingPayment = true
                lifecycleScope.launch {
                    val success = eliteRepository.verifyElitePayment(orderId)
                    isProcessingPayment = false
                    if (success) {
                        Toast.makeText(this@EliteDashboardActivity, "Welcome to Rivava Elite!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@EliteDashboardActivity, "Payment verification failed or membership full.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private var currentOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startPayment = intent.getBooleanExtra("start_payment", false)
        if (startPayment) {
            startPaymentFlow()
        }

        setContent {
            if (isProcessingPayment) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD4AF37))
                }
            } else {
                EliteDashboardScreen(
                    onBack = { finish() }
                )
            }
        }
    }

    private fun startPaymentFlow() {
        isProcessingPayment = true
        lifecycleScope.launch {
            val orderResult = eliteRepository.createEliteOrder()
            if (orderResult.success && orderResult.orderId != null && orderResult.paymentUrl != null) {
                currentOrderId = orderResult.orderId
                val intent = Intent(this@EliteDashboardActivity, PaymentActivity::class.java).apply {
                    putExtra("payment_url", orderResult.paymentUrl)
                }
                paymentLauncher.launch(intent)
            } else {
                Toast.makeText(this@EliteDashboardActivity, "Failed to start payment: ${orderResult.error}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
