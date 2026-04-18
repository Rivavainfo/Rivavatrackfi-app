package com.rivavafi.universal.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf(Message("Hello! How can I help you today?", false)) }
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }

            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMsg = inputText
                            messages.add(Message(userMsg, true))
                            inputText = ""
                            coroutineScope.launch {
                                delay(500)
                                messages.add(Message(getBotResponse(userMsg), false))
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, shape)
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor)
        }
    }
}

fun getBotResponse(query: String): String {
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
        else -> "I'm still learning and might not have the exact answer to that. Please check our official website or ask about transactions, portfolios, premium access, or account settings!"
    }
}
