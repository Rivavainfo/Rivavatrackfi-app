#!/bin/bash
set -e

# 1. AuthActivity.kt
cat << 'PY' > patch1.py
import re
with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

content = content.replace('var authMethod by remember { mutableStateOf("INITIAL") }', '')
content = content.replace('var phoneNumber by remember { mutableStateOf("") }', '')
content = content.replace('var showPhoneConfirmDialog by remember { mutableStateOf(false) }', '')
content = content.replace('var tempName by remember { mutableStateOf("") }', '')
content = content.replace('var tempEmail by remember { mutableStateOf("") }', '')
content = content.replace('var tempPhotoUrl by remember { mutableStateOf("") }', '')

content = re.sub(r'tempName\s*=\s*account\.displayName.*?\n', '\n', content)
content = re.sub(r'tempEmail\s*=\s*account\.email.*?\n', '\n', content)
content = re.sub(r'tempPhotoUrl\s*=\s*account\.photoUrl\?\.toString\(\).*?\n', '\n', content)

content = re.sub(r'\s*LaunchedEffect\(phoneAuthState\)\s*\{\s*if\s*\(phoneAuthState == PhoneAuthState\.CODE_SENT\)\s*\{\s*\}\s*\}', '', content)
content = re.sub(r'\s*LaunchedEffect\(requiresProfileCompletion\)\s*\{\s*if\s*\(requiresProfileCompletion\)\s*\{\s*authMethod = "PROFILE_COMPLETION"\s*\}\s*\}', '', content)
content = re.sub(r'\s*if\s*\(showPhoneConfirmDialog\)\s*\{\s*AlertDialog\([\s\S]*?\}\s*\)', '', content)

start_marker = 'if (authMethod == "PROFILE_COMPLETION") {'
end_marker = '} else if (authMethod == "INITIAL") {'
if start_marker in content and end_marker in content:
    start_idx = content.find(start_marker)
    end_idx = content.find(end_marker) + len(end_marker)
    content = content[:start_idx] + content[end_idx:]

content = content.replace('                        }\n\n\n\n                    }\n\n                }', '                        }\n\n                }')

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
PY
python3 patch1.py

# 2. AuthViewModel.kt
cat << 'PY' > patch2.py
import re
with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(r'private val _requiresProfileCompletion = MutableStateFlow\(false\)\s*val requiresProfileCompletion: StateFlow<Boolean> = _requiresProfileCompletion.asStateFlow\(\)', '', content)

content = content.replace('''                    // New user, return to IDLE so UI can show PROFILE_COMPLETION
                    _requiresProfileCompletion.value = true
                    _authState.value = AuthState.IDLE''', '''                    // New user, navigate to Welcome
                    _isNewUser.value = true
                    _authState.value = AuthState.SUCCESS''')

content = re.sub(r'fun saveUserProfileCompletion[\s\S]*?fun onEmailLogin', 'fun onEmailLogin', content)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
PY
python3 patch2.py

# 3. PhoneInputScreen.kt
cat << 'PY' > patch3.py
import re
with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/PhoneInputScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'val errorMessage by viewModel\.errorMessage\.collectAsState\(\)',
                 r'val errorMessage by viewModel.errorMessage.collectAsState()\n    var showConfirmDialog by remember { mutableStateOf(false) }',
                 content)

content = content.replace('''            onClick = {
                val formattedNumber = phoneNumber.trim()
                if (formattedNumber.length == 10) {
                    viewModel.savePhoneNumber(formattedNumber) {
                        onNavigateNext()
                    }
                }
            },''', '''            onClick = {
                val formattedNumber = phoneNumber.trim()
                if (formattedNumber.length == 10) {
                    showConfirmDialog = true
                }
            },''')

dialog_code = '''

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(text = "Confirm Number", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Is this your correct phone number?", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "+91 $phoneNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimarySky)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.startPhoneVerification(phoneNumber)
                    onNavigateNext()
                }) {
                    Text("Yes, Continue", fontWeight = FontWeight.Bold, color = PrimarySky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Edit Number", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = AmoledBlack,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

'''
content = content.replace('    RivavaLoadingOverlay(isLoading = isLoading)', dialog_code + '    RivavaLoadingOverlay(isLoading = isLoading)')

with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/PhoneInputScreen.kt", "w") as f:
    f.write(content)
PY
python3 patch3.py

# 4. OtpVerificationScreen.kt (Create)
cat << 'EOF' > app/src/main/java/com/rivavafi/universal/ui/onboarding/OtpVerificationScreen.kt
package com.rivavafi.universal.ui.onboarding

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.ui.components.RivavaBrandDisplay
import com.rivavafi.universal.ui.components.RivavaLoadingOverlay
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onNavigateNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(30) }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RivavaBrandDisplay(showQuote = true)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We have sent a verification code to your number.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = {
                val digitsOnly = it.filter { char -> char.isDigit() }
                if (digitsOnly.length <= 6) {
                    otpCode = digitsOnly
                }
            },
            label = { Text("6-digit Code", color = Color.White.copy(0.7f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00A3FF),
                unfocusedBorderColor = Color.White.copy(0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (otpCode.length == 6) {
                    viewModel.verifyOtp(otpCode) {
                        onNavigateNext()
                    }
                } else {
                    Toast.makeText(context, "Please enter 6 digit code", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00A3FF),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF005D8A),
                disabledContentColor = Color.White.copy(alpha = 0.75f)
            ),
            enabled = otpCode.length == 6 && !isLoading,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                "Verify",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                if (timeLeft == 0) {
                    viewModel.resendOtp()
                    timeLeft = 30
                }
            },
            enabled = timeLeft == 0 && !isLoading
        ) {
            Text(
                text = if (timeLeft > 0) "Resend code in ${timeLeft}s" else "Resend Code",
                color = if (timeLeft > 0) Color.Gray else Color(0xFF00A3FF),
                fontWeight = FontWeight.Medium
            )
        }
    }

    RivavaLoadingOverlay(isLoading = isLoading)
}
EOF

# 5. OnboardingViewModel.kt
cat << 'PY' > patch5.py
import re
with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/OnboardingViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(r'val errorMessage: StateFlow<String\?> = _errorMessage.asStateFlow\(\)',
                 r'''val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastPhoneNumber: String? = null
''', content)

phone_logic = '''
    fun startPhoneVerification(phoneNumber: String) {
        lastPhoneNumber = phoneNumber
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.sendOtp(phoneNumber)
                if (result.isSuccess) {
                    _isLoading.value = false
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: unable to send OTP."
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit) {
        val phone = lastPhoneNumber ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.verifyOtpAndSignIn(phone, code)
                if (result.isSuccess) {
                    saveUserToFirestoreAndComplete(onSuccess)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Invalid OTP"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: unable to verify OTP."
                _isLoading.value = false
            }
        }
    }

    fun resendOtp() {
        lastPhoneNumber?.let { startPhoneVerification(it) }
    }

    private fun saveUserToFirestoreAndComplete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    _errorMessage.value = "User not found"
                    _isLoading.value = false
                    return@launch
                }
                val uid = user.uid
                val email = user.email ?: ""
                val photoUrl = user.photoUrl?.toString() ?: ""
                val name = preferencesRepository.getUserName() ?: ""
                val phone = lastPhoneNumber ?: ""

                val userData = mapOf(
                    "uid" to uid,
                    "username" to name,
                    "email" to email,
                    "phoneNumber" to phone,
                    "preference" to "Not Selected",
                    "premiumStatus" to false,
                    "profileImage" to photoUrl,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "lastLoginAt" to com.google.firebase.Timestamp.now(),
                    "authProvider" to "google"
                )

                firestore.collection("therivavadata").document(uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                preferencesRepository.setOnboardingCompleted(true)
                                _isLoading.value = false
                                onSuccess()
                            }
                        } else {
                            _errorMessage.value = task.exception?.message ?: "Failed to save data"
                            _isLoading.value = false
                        }
                    }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to complete onboarding"
                _isLoading.value = false
            }
        }
    }
'''
content = content.replace('    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {', phone_logic + '\n    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {')

with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/OnboardingViewModel.kt", "w") as f:
    f.write(content)
PY
python3 patch5.py

# 6. HomeActivity.kt
cat << 'PY' > patch6.py
with open("app/src/main/java/com/rivavafi/universal/HomeActivity.kt", "r") as f:
    content = f.read()

content = content.replace('    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)',
                          '    object OtpVerification : Screen("otp_verification", "OTP Verification", Icons.Outlined.Home)\n    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)')

old_phone_input = '''            composable(Screen.PhoneInput.route) {
                PhoneInputScreen(onNavigateNext = {
                    navController.navigate(Screen.SmsOptIn.route) {
                        popUpTo(Screen.PhoneInput.route) { inclusive = true }
                    }
                })
            }'''

new_phone_input_and_otp = '''            composable(Screen.PhoneInput.route) {
                PhoneInputScreen(onNavigateNext = {
                    navController.navigate(Screen.OtpVerification.route) {
                        popUpTo(Screen.PhoneInput.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.OtpVerification.route) {
                com.rivavafi.universal.ui.onboarding.OtpVerificationScreen(onNavigateNext = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                })
            }'''

content = content.replace(old_phone_input, new_phone_input_and_otp)

with open("app/src/main/java/com/rivavafi/universal/HomeActivity.kt", "w") as f:
    f.write(content)
PY
python3 patch6.py

# 7. WhatsAppUtils.kt
cat << 'EOF' > app/src/main/java/com/rivavafi/universal/utils/WhatsAppUtils.kt
package com.rivavafi.universal.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppUtils {

    fun openWhatsAppForAdvisor(
        context: Context,
        username: String,
        email: String,
        phoneNumber: String,
        preference: String,
        premiumStatus: Boolean
    ) {
        try {
            val message = """
                Hello Rivava Team,

                A user wants to connect with an advisor.

                Details:
                Name: $username
                Email: $email
                Phone: $phoneNumber
                Preference: $preference
                Premium Status: $premiumStatus

                Please contact me.
            """.trimIndent()

            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val targetNumber = "919044761170"

            val url = "https://api.whatsapp.com/send?phone=$targetNumber&text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on your device", Toast.LENGTH_LONG).show()
        }
    }
}
EOF

# 8. RivavaPortfolioScreen.kt
cat << 'PY' > patch8.py
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
PY
python3 patch8.py

# 9. PremiumUnlockDialog.kt
cat << 'PY' > patch9.py
with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/PremiumUnlockDialog.kt", "r") as f:
    content = f.read()

content = content.replace(
'''fun PremiumUnlockDialog(
    userName: String,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit,
    onPayClick: (() -> Unit)? = null
) {''',
'''fun PremiumUnlockDialog(
    userName: String = "User",
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit,
    onPayClick: (() -> Unit)? = null,
    secretKeyToMatch: String = com.rivavafi.universal.utils.SecretConfig.PORTFOLIO_KEY
) {''')

content = content.replace('secretKeyInput == com.rivavafi.universal.utils.SecretConfig.PORTFOLIO_KEY', 'secretKeyInput == secretKeyToMatch')

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/PremiumUnlockDialog.kt", "w") as f:
    f.write(content)
PY
python3 patch9.py

# 10. EliteLandingActivity.kt
cat << 'PY' > patch10.py
import re
with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt", "r") as f:
    content = f.read()

content = re.sub(r'context\.startActivity\(Intent\(context, com\.rivavafi\.universal\.ui\.elite\.EliteDashboardActivity::class\.java\)\.apply \{\n\s*putExtra\("start_payment", true\)\n\s*\}\)',
'''com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                            context = context,
                            username = finalUserName,
                            email = finalUserEmail,
                            phoneNumber = auth.currentUser?.phoneNumber ?: "",
                            preference = "Rivava Elite",
                            premiumStatus = false
                        )
                        isConnecting = false''', content)

content = re.sub(r'''val openWhatsApp = \{
        try \{
            logInquiry\("whatsapp", "whatsapp_opened"\)
            val intent = Intent\(Intent.ACTION_VIEW\)\.apply \{
                data = Uri.parse\("https://api\.whatsapp\.com/send\?phone=919044761170&text=\$\{Uri\.encode\(formattedMessage\)\}"\)
                setPackage\("com\.whatsapp"\)
            \}
            context\.startActivity\(intent\)
            logInquiry\("whatsapp", "inquiry_completed"\)
            showAfterDialog = true
        \} catch \(e: Exception\) \{
            Toast\.makeText\(context, "WhatsApp not installed\.", Toast\.LENGTH_SHORT\)\.show\(\)
        \}
    \}''',
'''val openWhatsApp = {
        logInquiry("whatsapp", "whatsapp_opened")
        com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
            context = context,
            username = finalUserName,
            email = finalUserEmail,
            phoneNumber = auth.currentUser?.phoneNumber ?: "",
            preference = "Rivava Elite",
            premiumStatus = false
        )
        logInquiry("whatsapp", "inquiry_completed")
        showAfterDialog = true
    }''', content)

content = re.sub(r'''LaunchedEffect\(isConnecting\) \{\n\s*if \(isConnecting\) \{\n\s*delay\(1500\)\n\s*context.startActivity\(Intent\(context, com\.rivavafi\.universal\.ui\.elite\.EliteDashboardActivity::class\.java\)\.apply \{\n\s*putExtra\("start_payment", true\)\n\s*\}\)\n\s*isConnecting = false\n\s*\}\n\s*\}''',
'''LaunchedEffect(isConnecting) {
        if (isConnecting) {
            delay(1500)
            com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                context = context,
                username = finalUserName,
                email = finalUserEmail,
                phoneNumber = auth.currentUser?.phoneNumber ?: "",
                preference = "Rivava Elite",
                premiumStatus = false
            )
            isConnecting = false
        }
    }''', content)

with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt", "w") as f:
    f.write(content)
PY
python3 patch10.py

# 11. EliteDashboardActivity.kt
cat << 'PY' > patch11.py
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
PY
python3 patch11.py

# 12. firestore.rules
cat << 'PY' > patch12.py
with open("firestore.rules", "r") as f:
    content = f.read()

content = content.replace('    match /THEDATA/{userId} {',
'''    match /therivavadata/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /THEDATA/{userId} {''')

with open("firestore.rules", "w") as f:
    f.write(content)
PY
python3 patch12.py
