package com.rivavafi.universal.sms

import android.content.Context
import android.provider.Telephony
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsInboxScanner @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend fun scanInbox(context: Context, mode: SmsTrackingMode, userId: String) {
        if (mode == SmsTrackingMode.OFF) return

        withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 500" // Limit to avoid long scans
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val smsId = it.getString(idIndex)
                    val sender = it.getString(addressIndex) ?: ""
                    val body = it.getString(bodyIndex) ?: ""
                    val timestamp = it.getLong(dateIndex)

                    val parsed = SmsTransactionParser.parseMessage(sender, body, timestamp, smsId, mode)
                    if (parsed != null) {
                        // Check duplicates
                        val isProcessed = transactionRepository.isSmsIdProcessed(smsId, userId)
                        if (!isProcessed) {
                            val exists = transactionRepository.doesTransactionExist(
                                date = parsed.date,
                                amount = parsed.amount,
                                merchantName = parsed.merchantName,
                                userId = userId
                            )
                            if (!exists) {
                                val entity = TransactionEntity(
                                    userId = userId,
                                    bankName = parsed.bankName,
                                    merchantName = parsed.merchantName,
                                    amount = parsed.amount,
                                    type = parsed.type,
                                    category = parsed.category,
                                    date = parsed.date,
                                    smsId = parsed.smsId,
                                    rawMessage = parsed.rawMessage,
                                    availableBalance = parsed.availableBalance,
                                    referenceId = parsed.referenceId,
                                    subcategory = parsed.subcategory
                                )
                                transactionRepository.addTransaction(entity)
                            }
                        }
                    }
                }
            }
        }
    }
}
