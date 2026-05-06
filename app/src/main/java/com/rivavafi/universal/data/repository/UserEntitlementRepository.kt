package com.rivavafi.universal.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class EntitlementStatus { LOADING, UNLOCKED, LOCKED, ERROR }

data class PremiumState(
    val status: EntitlementStatus = EntitlementStatus.LOADING,
    val isPremium: Boolean = false,
    val source: String? = null
)

data class RazorpayOrderResult(
    val success: Boolean,
    val orderId: String? = null,
    val amount: Int? = null,
    val currency: String? = null,
    val keyId: String? = null,
    val error: String? = null
)

@Singleton
class UserEntitlementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _premiumState = MutableStateFlow(PremiumState())
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    suspend fun syncEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        val localPremium = prefs.getBoolean("isPremium", false)

        if (localPremium) {
            _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "cache")
        } else {
            _premiumState.value = PremiumState(EntitlementStatus.LOADING, false, null)
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
            prefs.edit().putBoolean("isPremium", false).putBoolean("portfolio_unlocked", false).apply()
            userPreferencesRepository.setPremiumUserForCurrent(false)
            return
        }

        try {
            val docSnap = firestore.collection("users").document(uid).get().await()
            val isPremium = docSnap.getBoolean("is_premium") ?: false
            val status = docSnap.getString("premium_status") ?: if (isPremium) "active" else "locked"

            val validPremium = isPremium && status == "active"

            _premiumState.value = if (validPremium) {
                PremiumState(EntitlementStatus.UNLOCKED, true, docSnap.getString("premium_source"))
            } else {
                PremiumState(EntitlementStatus.LOCKED, false, null)
            }

            prefs.edit()
                .putBoolean("isPremium", validPremium)
                .putBoolean("portfolio_unlocked", validPremium)
                .apply()
            userPreferencesRepository.setPremiumUserForCurrent(validPremium)

        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error syncing entitlement", e)
            _premiumState.value = PremiumState(EntitlementStatus.ERROR, localPremium, "cache")
        }
    }

    suspend fun createRazorpayOrder(amountPaise: Int): RazorpayOrderResult {
        if (auth.currentUser?.uid == null) return RazorpayOrderResult(false, error = "User not authenticated")

        return try {
            val data = hashMapOf("amount" to amountPaise)
            val result = functions
                .getHttpsCallable("createRazorpayOrder")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
            val orderId = resultData?.get("order_id") as? String
            val amount = (resultData?.get("amount") as? Number)?.toInt()
            val currency = resultData?.get("currency") as? String
            val keyId = resultData?.get("key_id") as? String

            if (orderId != null && amount != null) {
                RazorpayOrderResult(true, orderId, amount, currency, keyId)
            } else {
                RazorpayOrderResult(false, error = "Invalid response from server")
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error creating Razorpay order", e)
            RazorpayOrderResult(false, error = e.message ?: "Unknown error")
        }
    }

    suspend fun verifyRazorpayPayment(orderId: String, paymentId: String, signature: String): Boolean {
        if (auth.currentUser?.uid == null) return false

        return try {
            val data = hashMapOf(
                "razorpay_order_id" to orderId,
                "razorpay_payment_id" to paymentId,
                "razorpay_signature" to signature
            )

            val result = functions
                .getHttpsCallable("verifyRazorpayPayment")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
            if (resultData?.get("success") == true) {
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "razorpay")
                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isPremium", true)
                    .putBoolean("portfolio_unlocked", true)
                    .apply()
                userPreferencesRepository.setPremiumUserForCurrent(true)
                true
            } else {
                 _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
                 false
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Failed to verify payment via Cloud Function", e)
            _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
            false
        }
    }

    suspend fun clearEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isPremium", false)
            .putBoolean("portfolio_unlocked", false)
            .apply()
        userPreferencesRepository.setPremiumUserForCurrent(false)
        _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
    }
}
