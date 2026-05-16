package com.rivavafi.universal.ui.portfolio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.rivavafi.universal.BuildConfig

class PaymentActivity : ComponentActivity() {

    private val TAG = "PaymentActivity"
    private var hasOpenedUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentUrl = intent.getStringExtra("payment_url")

        if (paymentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Payment URL missing", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (!hasOpenedUrl) {
            hasOpenedUrl = true
            try {
                Log.i(TAG, "OPENING_PAYMENT_URL")
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(this, Uri.parse(paymentUrl))
            } catch (e: Exception) {
                Log.e(TAG, "Error opening payment URL via Custom Tabs, falling back to browser", e)
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                    startActivity(browserIntent)
                } catch (e2: Exception) {
                    Toast.makeText(this, "Unable to open payment link.", Toast.LENGTH_LONG).show()
                    val resultIntent = Intent().apply { putExtra("error", e2.message) }
                    setResult(RESULT_CANCELED, resultIntent)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasOpenedUrl) {
            // When user returns from the browser or Custom Tab, we consider the payment flow finished on the client.
            // We return RESULT_OK, which prompts the screen to verify with the backend.
            Log.i(TAG, "RETURNED_FROM_PAYMENT")
            setResult(RESULT_OK)
            finish()
        }
    }
}
