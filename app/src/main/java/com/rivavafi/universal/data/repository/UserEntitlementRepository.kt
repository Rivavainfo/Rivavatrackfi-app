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
import kotlinx.coroutines.delay

enum class EntitlementStatus { LOADING, UNLOCKED, LOCKED, ERROR }

data class PremiumState(
    val status: EntitlementStatus = EntitlementStatus.LOADING,
    val isPremium: Boolean = false,
    val source: String? = null
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

    suspend fun grantPremium(source: String, txnId: String? = null): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return try {
            val data = hashMapOf(
                "source" to source,
                "txnId" to txnId
            )

            val result = functions
                .getHttpsCallable("verifyPayment")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
            if (resultData?.get("success") == true) {
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, source)
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
            Log.e("UserEntitlement", "Failed to grant premium via Cloud Function", e)
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
