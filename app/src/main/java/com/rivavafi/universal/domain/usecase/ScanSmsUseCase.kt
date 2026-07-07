package com.rivavafi.universal.domain.usecase

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.rivavafi.universal.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.domain.preferences.SmsTrackingMode

data class ScanProgress(
    val inProgress: Boolean = false,
    val message: String = "",
    val percent: Float = 0f,
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val banksDetected: Set<String> = emptySet(),
    val totalTransactionsFound: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null
)

class ScanSmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parseAndSaveSmsUseCase: ParseAndSaveSmsUseCase,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<ScanProgress> = flow {
        // SMS Scanning is disabled due to Play Store Policies regarding READ_SMS
        emit(ScanProgress(inProgress = true, message = "SMS tracking is disabled..."))
        delay(1000)

        emit(
            ScanProgress(
                inProgress = false,
                message = "Scan skipped per policy.",
                percent = 1f,
                processedCount = 0,
                totalCount = 0,
                banksDetected = emptySet(),
                totalTransactionsFound = 0,
                isComplete = true
            )
        )
    }.flowOn(Dispatchers.IO)
}
