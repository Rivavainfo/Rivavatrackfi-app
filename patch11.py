import re
with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteDashboardActivity.kt", "r") as f:
    content = f.read()

imports = '''
import com.rivavafi.universal.ui.portfolio.PremiumUnlockDialog
import com.rivavafi.universal.utils.SecretConfig
import com.rivavafi.universal.utils.WhatsAppUtils
import com.google.firebase.auth.FirebaseAuth
'''

content = content.replace('import dagger.hilt.android.AndroidEntryPoint', imports + '\nimport dagger.hilt.android.AndroidEntryPoint')

new_content = '''
        setContent {
            val auth = FirebaseAuth.getInstance()
            var showSecretDialog by remember { mutableStateOf(false) }
            val prefs = getSharedPreferences("RivavaElitePrefs", MODE_PRIVATE)
            var isEliteUnlocked by remember { mutableStateOf(prefs.getBoolean("elite_unlocked", false)) }
            var showWhatsAppDialog by remember { mutableStateOf(false) }

            if (!isEliteUnlocked) {
                showSecretDialog = true
                if (showSecretDialog) {
                    PremiumUnlockDialog(
                        onDismiss = { finish() },
                        onUnlockSuccess = {
                            prefs.edit().putBoolean("elite_unlocked", true).apply()
                            isEliteUnlocked = true
                            showSecretDialog = false
                        },
                        onPayClick = {
                            WhatsAppUtils.openWhatsAppForAdvisor(
                                context = this@EliteDashboardActivity,
                                username = auth.currentUser?.displayName ?: "User",
                                email = auth.currentUser?.email ?: "",
                                phoneNumber = auth.currentUser?.phoneNumber ?: "",
                                preference = "Rivava Elite",
                                premiumStatus = false
                            )
                            showWhatsAppDialog = true
                        },
                        secretKeyToMatch = SecretConfig.ELITE_KEY
                    )
                }

                if (showWhatsAppDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showWhatsAppDialog = false },
                        title = { androidx.compose.material3.Text("Contact Advisor", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        text = { androidx.compose.material3.Text("Did you connect with the advisor successfully?") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                showWhatsAppDialog = false
                                WhatsAppUtils.openWhatsAppForAdvisor(
                                    context = this@EliteDashboardActivity,
                                    username = auth.currentUser?.displayName ?: "User",
                                    email = auth.currentUser?.email ?: "",
                                    phoneNumber = auth.currentUser?.phoneNumber ?: "",
                                    preference = "Rivava Elite",
                                    premiumStatus = false
                                )
                            }) {
                                androidx.compose.material3.Text("Contact Again", color = Color(0xFFD4AF37))
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showWhatsAppDialog = false }) {
                                androidx.compose.material3.Text("Close", color = Color.Gray)
                            }
                        },
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White,
                        textContentColor = Color.White
                    )
                }
            } else {
                EliteDashboardScreen(
                    onBack = { finish() }
                )
            }
        }
'''

content = re.sub(r'\s*private var isProcessingPayment by mutableStateOf\(false\)[\s\S]*?private var currentOrderId: String\? = null', '', content)
content = re.sub(r'\s*val startPayment = intent\.getBooleanExtra\("start_payment", false\)\s*if \(startPayment\) \{\s*startPaymentFlow\(\)\s*\}', '', content)
content = re.sub(r'\s*private fun startPaymentFlow\(\) \{[\s\S]*?\}\n\s*\}', '', content)

content = re.sub(r'\s*setContent \{[\s\S]*?\}\n\s*\}', new_content + '\n    }', content)

with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteDashboardActivity.kt", "w") as f:
    f.write(content)
