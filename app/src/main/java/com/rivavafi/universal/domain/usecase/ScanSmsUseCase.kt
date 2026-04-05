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
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<ScanProgress> = flow {
        emit(ScanProgress(inProgress = true, message = "Scanning SMS..."))
        delay(1000)

        val uri = Uri.parse("content://sms/inbox")

        val banks = mutableSetOf<String>()
        var transactionsFound = 0

        try {
            // Read all old messages, no date limit
            val cursor = context.contentResolver.query(
                uri,
                null,
                null,
                null,
                "date ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val totalMessages = cursor.count // No limit, scan entire inbox historically as requested
                var processed = 0

                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

                emit(ScanProgress(inProgress = true, message = "Building your transaction history..."))

                do {
                    val smsId = cursor.getString(idIndex)
                    val sender = cursor.getString(addressIndex)
                    val body = cursor.getString(bodyIndex)
                    val date = cursor.getLong(dateIndex)

                    if (sender != null && body != null) {
                        val smsHashId = "sms_$smsId"

                        // Check if already processed via smsId
                        if (!transactionRepository.isSmsIdProcessed(smsHashId)) {
                            Log.d("TRACKFI_SMS", "Scanning historical SMS from: $sender")
                            val result = parseAndSaveSmsUseCase.parseAndReturn(sender, body, date, smsHashId)
                            if (result != null) {
                                // Check explicit deduplication parameters before insert
                                if (!transactionRepository.doesTransactionExist(result.date, result.amount, result.merchantName)) {
                                    transactionRepository.addTransaction(result)
                                    transactionsFound++
                                    Log.d("TRACKFI_DATABASE", "Saved historical transaction: $result")
                                    if (result.bankName != null) {
                                        banks.add(result.bankName)
                                    }
                                } else {
                                    Log.d("TRACKFI_DATABASE", "Skipped duplicate historical transaction: $result")
                                }
                            }
                        }
                    }

                    processed++
                    if (processed % 20 == 0) {
                        emit(
                            ScanProgress(
                                inProgress = true,
                                message = if (banks.isNotEmpty()) "Detecting bank transactions..." else "Scanning SMS...",
                                percent = processed.toFloat() / totalMessages.toFloat(),
                                processedCount = processed,
                                totalCount = totalMessages,
                                banksDetected = banks,
                                totalTransactionsFound = transactionsFound
                            )
                        )
                    }

                } while (cursor.moveToNext() && processed < totalMessages)

                cursor.close()
            }

            emit(
                ScanProgress(
                    inProgress = false,
                    message = "Scan Complete!",
                    percent = 1f,
                    processedCount = 0, // Not needed upon completion
                    totalCount = 0,
                    banksDetected = banks,
                    totalTransactionsFound = transactionsFound,
                    isComplete = true
                )
            )
        } catch (e: Exception) {
            Log.e("TRACKFI_SMS", "Error scanning SMS: ${e.message}", e)
            emit(
                ScanProgress(
                    inProgress = false,
                    message = "Error scanning messages. Please try again.",
                    isComplete = true,
                    error = e.message
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}
