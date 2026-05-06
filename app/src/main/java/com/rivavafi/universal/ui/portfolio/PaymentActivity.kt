package com.rivavafi.universal.ui.portfolio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.rivavafi.universal.BuildConfig

class PaymentActivity : ComponentActivity(), PaymentResultWithDataListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amount = intent.getIntExtra("amount", 0)
        val orderId = intent.getStringExtra("order_id")
        var keyId = intent.getStringExtra("key_id")

        if (keyId.isNullOrEmpty()) {
            keyId = BuildConfig.RAZORPAY_KEY_ID
        }

        if (amount <= 0 || orderId.isNullOrEmpty() || keyId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid payment details.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID(keyId)

        try {
            val options = org.json.JSONObject()
            options.put("name", "Rivava Premium")
            options.put("description", "Unlock Portfolio Insights")
            options.put("currency", "INR")
            options.put("amount", amount.toString())
            options.put("order_id", orderId)

            // Ideally fetch these from user profile if available
            // For now, these are optional, but helpful for Razorpay UI
            val preFill = org.json.JSONObject()
            // preFill.put("email", "test@example.com")
            // preFill.put("contact", "9999999999")
            options.put("prefill", preFill)

            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
            val resultIntent = Intent().apply { putExtra("error", e.message) }
            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    override fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment Authorized", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent().apply {
            putExtra("payment_id", paymentId)
            putExtra("order_id", paymentData?.orderId)
            putExtra("signature", paymentData?.signature)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent().apply { putExtra("error", response) }
        setResult(RESULT_CANCELED, resultIntent)
        finish()
    }
}
