package com.rivavafi.universal.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class EntitlementStatus { LOADING, UNLOCKED, LOCKED, ERROR }

data class PremiumState(
    val status: EntitlementStatus = EntitlementStatus.LOADING,
    val isPremium: Boolean = false,
    val source: String? = null
)

data class OrderResult(
    val success: Boolean,
    val orderId: String? = null,
    val paymentUrl: String? = null,
    val error: String? = null
)

@Singleton
class UserEntitlementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance("asia-south1")
    private val auth = FirebaseAuth.getInstance()

    private val _premiumState = MutableStateFlow(PremiumState())
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    suspend fun syncEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        val localPremium = prefs.getBoolean("isPremium", false)
        val localPremiumSource = prefs.getString("premium_source", null)
        val hasLocalKeyUnlock = localPremium && localPremiumSource == "access_key"

        if (localPremium) {
            _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "cache")
        } else {
            _premiumState.value = PremiumState(EntitlementStatus.LOADING, false, null)
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            if (hasLocalKeyUnlock) {
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "access_key")
                userPreferencesRepository.setPremiumUserForCurrent(true)
            } else {
                _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
                prefs.edit().putBoolean("isPremium", false).putBoolean("portfolio_unlocked", false).remove("premium_source").apply()
                userPreferencesRepository.setPremiumUserForCurrent(false)
            }
            snapshotListener?.remove()
            return
        }

        try {
            snapshotListener?.remove()
            snapshotListener = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("UserEntitlement", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val isPremium = snapshot.getBoolean("is_premium") ?: false
                        val status = snapshot.getString("premium_status") ?: if (isPremium) "active" else "locked"

                        val validPremium = isPremium && status == "active"
                        val effectivePremium = validPremium || hasLocalKeyUnlock
                        val effectiveSource = if (validPremium) snapshot.getString("premium_source") else "access_key"

                        _premiumState.value = if (effectivePremium) {
                            PremiumState(EntitlementStatus.UNLOCKED, true, effectiveSource)
                        } else {
                            PremiumState(EntitlementStatus.LOCKED, false, null)
                        }

                        val edit = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE).edit()
                        edit.putBoolean("isPremium", effectivePremium)
                            .putBoolean("portfolio_unlocked", effectivePremium)
                        if (effectivePremium) {
                            edit.putString("premium_source", effectiveSource)
                        } else {
                            edit.remove("premium_source")
                        }
                        edit.apply()

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            userPreferencesRepository.setPremiumUserForCurrent(effectivePremium)
                        }
                    }
                }
            val docSnap = firestore.collection("users").document(uid).get().await()
            val isPremium = docSnap.getBoolean("is_premium") ?: false
            val status = docSnap.getString("premium_status") ?: if (isPremium) "active" else "locked"

            val validPremium = isPremium && status == "active"
            val effectivePremium = validPremium || hasLocalKeyUnlock
            val effectiveSource = if (validPremium) docSnap.getString("premium_source") else "access_key"

            _premiumState.value = if (effectivePremium) {
                PremiumState(EntitlementStatus.UNLOCKED, true, effectiveSource)
            } else {
                PremiumState(EntitlementStatus.LOCKED, false, null)
            }

            val edit = prefs.edit()
                .putBoolean("isPremium", effectivePremium)
                .putBoolean("portfolio_unlocked", effectivePremium)
            if (effectivePremium) {
                edit.putString("premium_source", effectiveSource)
            } else {
                edit.remove("premium_source")
            }
            edit.apply()
            userPreferencesRepository.setPremiumUserForCurrent(effectivePremium)

        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error syncing entitlement", e)
            _premiumState.value = PremiumState(EntitlementStatus.ERROR, localPremium, "cache")
        }
    }

        suspend fun createUroPayOrder(amountPaise: Int): OrderResult {
        if (auth.currentUser?.uid == null) return OrderResult(false, error = "User not authenticated")

        return try {
            Log.i("UserEntitlement", "PAYMENT_CREATE_STARTED")
            val data = hashMapOf("amount" to amountPaise)
            val result = functions
                .getHttpsCallable("createUroPayOrder")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
            val orderId = resultData?.get("order_id") as? String
            val paymentUrl = resultData?.get("payment_url") as? String

            if (orderId != null && paymentUrl != null) {
                Log.i("UserEntitlement", "PAYMENT_CREATE_SUCCESS")
                OrderResult(true, orderId, paymentUrl = paymentUrl)
            } else {
                OrderResult(false, error = "Invalid response from server")
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error creating UroPay order", e)
            OrderResult(false, error = e.message ?: "Unknown error")
        }
    }

        suspend fun verifyUroPayPayment(orderId: String): Boolean {
        if (auth.currentUser?.uid == null) return false

        return try {
            Log.i("UserEntitlement", "PAYMENT_VERIFY_STARTED")
            val data = hashMapOf(
                "order_id" to orderId
            )

            val result = functions
                .getHttpsCallable("verifyUroPayPayment")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
            if (resultData?.get("success") == true) {
                Log.i("UserEntitlement", "PAYMENT_VERIFY_SUCCESS")
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "uropay")
                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isPremium", true)
                    .putBoolean("portfolio_unlocked", true)
                    .putString("premium_source", "uropay")
                    .apply()
                userPreferencesRepository.setPremiumUserForCurrent(true)
                true
            } else {
                 Log.w("UserEntitlement", "PAYMENT_VERIFY_FAILED: Server returned success=false")
                 _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
                 false
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Failed to verify payment via Cloud Function", e)
            Log.w("UserEntitlement", "PAYMENT_VERIFY_FAILED: Exception")
            _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
            false
        }
    }



    suspend fun clearEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isPremium", false)
            .putBoolean("portfolio_unlocked", false)
            .remove("premium_source")
            .apply()
        userPreferencesRepository.setPremiumUserForCurrent(false)
        _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
    }
}
