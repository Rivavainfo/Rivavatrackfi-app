package com.rivavafi.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.rivavafi.data.preferences.UserPreferencesRepository
import com.rivavafi.domain.usecase.ParseAndSaveSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var parseAndSaveSmsUseCase: ParseAndSaveSmsUseCase

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val isTrackingEnabled = preferencesRepository.isSmsTrackingEnabledFlow.first()
                    if (!isTrackingEnabled) return@launch

                    for (sms in messages) {
                        val body = sms.displayMessageBody
                        val sender = sms.displayOriginatingAddress

                        if (body != null && sender != null) {
                            Log.d("TRACKFI_SMS", "New SMS detected from sender: $sender")
                            parseAndSaveSmsUseCase(sender, body, sms.timestampMillis)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
