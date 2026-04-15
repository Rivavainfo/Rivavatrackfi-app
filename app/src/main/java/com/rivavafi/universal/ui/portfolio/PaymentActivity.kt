package com.rivavafi.universal.ui.portfolio

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener

class PaymentActivity : ComponentActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_test_R9ah3nIr4tg8s4")

        try {
            val options = org.json.JSONObject()
            options.put("name", "Rivava Premium")
            options.put("description", "Unlock Portfolio Insights")
            options.put("currency", "INR")
            options.put("amount", "1100") // 1100 paise = ₹11

            val preFill = org.json.JSONObject()
            preFill.put("email", "test@example.com")
            preFill.put("contact", "9999999999")
            options.put("prefill", preFill)

            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        val prefs = getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("portfolio_unlocked", true).apply()
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_SHORT).show()
        setResult(RESULT_CANCELED)
        finish()
    }
}
