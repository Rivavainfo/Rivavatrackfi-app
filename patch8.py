import re
with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

content = content.replace('    val premiumState by premiumViewModel.premiumState.collectAsState()',
'''    val premiumState by premiumViewModel.premiumState.collectAsState()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    var showWhatsAppDialog by remember { mutableStateOf(false) }''')

content = re.sub(r'\s*val paymentState by premiumViewModel\.paymentState\.collectAsState\(\)', '', content)
content = re.sub(r'\s*val paymentLauncher = rememberLauncherForActivityResult[\s\S]*?\}\n\s*\}', '', content)
content = re.sub(r'\s*LaunchedEffect\(paymentState\.uiState\) \{[\s\S]*?\}\n\s*\}', '', content)

start_idx = content.find("val isProcessing = paymentState.uiState == PaymentUiState.CREATING_ORDER")
end_idx = content.find("val stockStates by viewModel.stockStates.collectAsState()")
end_block_idx = content.rfind("return", start_idx, end_idx)

new_buttons = '''
                    Button(
                        onClick = {
                            com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                                context = context,
                                username = auth.currentUser?.displayName ?: "User",
                                email = auth.currentUser?.email ?: "",
                                phoneNumber = auth.currentUser?.phoneNumber ?: "",
                                preference = "Not Selected",
                                premiumStatus = false
                            )
                            showWhatsAppDialog = true
                        },
                        modifier = Modifier
                            .widthIn(min = 220.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                    ) {
                        Text(
                            "Call With Advisor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    var showSecretDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showSecretDialog = true },
                        modifier = Modifier.widthIn(min = 220.dp).height(50.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha=0.5f))
                    ) {
                        Text("Enter Secret Key", color = Color.White)
                    }

                    if (showSecretDialog) {
                        PremiumUnlockDialog(
                            onDismiss = { showSecretDialog = false },
                            onUnlockSuccess = {
                                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("portfolio_unlocked", true).apply()
                                showSecretDialog = false
                            },
                            onPayClick = {
                                com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                                    context = context,
                                    username = auth.currentUser?.displayName ?: "User",
                                    email = auth.currentUser?.email ?: "",
                                    phoneNumber = auth.currentUser?.phoneNumber ?: "",
                                    preference = "Not Selected",
                                    premiumStatus = false
                                )
                                showWhatsAppDialog = true
                            }
                        )
                    }

                    if (showWhatsAppDialog) {
                        AlertDialog(
                            onDismissRequest = { showWhatsAppDialog = false },
                            title = { Text("Contact Advisor", fontWeight = FontWeight.Bold) },
                            text = { Text("Did you connect with the advisor successfully?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showWhatsAppDialog = false
                                    com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                                        context = context,
                                        username = auth.currentUser?.displayName ?: "User",
                                        email = auth.currentUser?.email ?: "",
                                        phoneNumber = auth.currentUser?.phoneNumber ?: "",
                                        preference = "Not Selected",
                                        premiumStatus = false
                                    )
                                }) {
                                    Text("Contact Again", color = Color(0xFFD4AF37))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showWhatsAppDialog = false }) {
                                    Text("Close", color = Color.Gray)
                                }
                            },
                            containerColor = Color(0xFF1E1E1E),
                            titleContentColor = Color.White,
                            textContentColor = Color.White
                        )
                    }
                }
            }

        }
        '''

if start_idx != -1 and end_block_idx != -1:
    content = content[:start_idx] + new_buttons + content[end_block_idx:]

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(content)
