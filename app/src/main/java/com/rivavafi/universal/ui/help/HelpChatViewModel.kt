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
        return "Hi, I'm Rivava Assistant. How can I help you?"
    }
}
