import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

# Fix the resend button in the verification UI
old_ui = """    if (showEmailVerificationUI) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Check your email and click verify",
                style = MaterialTheme.typography.titleLarge,
                color = PrimarySky,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.checkEmailVerified(
                        onVerified = { onLoginSuccess(isNewUser == true) },
                        onNotVerified = { }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack)
            ) {
                Text("I've Verified", fontWeight = FontWeight.Bold)
            }
        }
        return // Return early to not show the rest of the auth screen
    }"""

new_ui = """    if (showEmailVerificationUI) {
        var resendCooldown by remember { mutableStateOf(0) }

        LaunchedEffect(resendCooldown) {
            if (resendCooldown > 0) {
                kotlinx.coroutines.delay(1000)
                resendCooldown--
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PrimarySky
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Check your inbox",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "We've sent a verification link to your email. Please click it to continue.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.checkEmailVerified(
                        onVerified = { onLoginSuccess(isNewUser == true) },
                        onNotVerified = { }
                    )
                },
                enabled = authState != AuthState.LOADING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack)
            ) {
                if (authState == AuthState.LOADING) {
                    CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
                } else {
                    Text("I've Verified", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.resendVerificationEmail()
                    resendCooldown = 60
                },
                enabled = resendCooldown == 0 && authState != AuthState.LOADING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimarySky.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimarySky)
            ) {
                Text(if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend Email")
            }
        }
        return // Return early to not show the rest of the auth screen
    }"""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
