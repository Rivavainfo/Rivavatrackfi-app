package com.rivavafi.universal.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivavafi.universal.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(listOf(
        Message("Hello! How can I help you today?", false)
    ))
    val messages: StateFlow<List<Message>> = _messages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun sendMessage(userMsg: String) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(Message(userMsg, true))
        _messages.value = currentMessages

        _isTyping.value = true

        viewModelScope.launch {
            repository.sendMessage(userMsg).collect { result ->
                _isTyping.value = false
                val botReply = result.getOrElse {
                    getFallbackResponse(userMsg)
                }
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(Message(botReply, false))
                _messages.value = updatedMessages
            }
        }
    }

    private fun getFallbackResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("hello") || q.contains("hi") -> "Hello there! I am your AI Assistant. How can I help you navigate Rivava/TrackFi today?"
            q.contains("app") -> "Rivava Universal is your all-in-one platform for managing personal finances, tracking real-time stock/crypto portfolios, and reviewing deep analytics."
            q.contains("help") -> "I am your built-in AI assistant! Ask me about adding transactions, tracking your portfolio, upgrading to premium, or managing your profile settings."
            q.contains("stock") || q.contains("portfolio") || q.contains("market") -> "You can access live stock and crypto data in the 'Rivava Portfolio' tab. Just tap the chart icon on the bottom navigation bar!"
            q.contains("security") || q.contains("privacy") -> "Your security is paramount to us. All sensitive actions are authenticated via Firebase, and screen capture is strictly disabled across the app for your safety."
            q.contains("transaction") || q.contains("expense") || q.contains("income") -> "You can manage and view all your transactions in the 'History' screen. Tap the '+' button to add new income or expenses!"
            q.contains("crypto") || q.contains("bitcoin") -> "Yes, we support live crypto tracking! Head over to the Portfolio tab and scroll down to the 'Crypto Assets' section to view live prices."
            q.contains("premium") || q.contains("unlock") || q.contains("pay") -> "The Rivava Portfolio screen is a premium feature. You can easily unlock it using Razorpay checkout directly in the app, or by entering a valid promo key."
            q.contains("password") || q.contains("login") || q.contains("auth") -> "If you are having trouble logging in, use the 'Forgot Password' option on the login screen to receive a secure reset link via email."
            q.contains("contact") || q.contains("support") || q.contains("human") -> "For complex issues, please email our support team at support@rivava.in or visit our website directly from the settings menu."
            q.contains("export") || q.contains("pdf") || q.contains("report") -> "You can view detailed investment thesis reports natively in the app! Simply click 'Read Detailed PDF' on any supported stock card."
            q.contains("news") || q.contains("article") -> "Stay updated with global financial news in the Portfolio tab! We aggregate top articles from major financial outlets like Bloomberg and WSJ."
            else -> "I'm experiencing an API error right now, but I can still tell you about transactions, portfolios, premium access, or account settings!"
        }
    }
}
