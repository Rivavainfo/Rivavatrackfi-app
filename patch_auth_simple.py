import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

# Instead of complex regex, let's just find exactly what we want to replace and use string replacements.
content = content.replace('var authMethod by remember { mutableStateOf("INITIAL") }', '')
content = content.replace('var phoneNumber by remember { mutableStateOf("") }', '')
content = content.replace('var showPhoneConfirmDialog by remember { mutableStateOf(false) }', '')
content = content.replace('var tempName by remember { mutableStateOf("") }', '')
content = content.replace('var tempEmail by remember { mutableStateOf("") }', '')
content = content.replace('var tempPhotoUrl by remember { mutableStateOf("") }', '')

# Remove LaunchedEffect for phoneAuthState
effect1 = '''    LaunchedEffect(phoneAuthState) {
        if (phoneAuthState == PhoneAuthState.CODE_SENT) {
        }
    }'''
content = content.replace(effect1, '')

# Remove LaunchedEffect for requiresProfileCompletion
effect2 = '''    LaunchedEffect(requiresProfileCompletion) {
        if (requiresProfileCompletion) {
            authMethod = "PROFILE_COMPLETION"
        }
    }'''
content = content.replace(effect2, '')

# Replace the giant if/else block
# The start is: "if (showPhoneConfirmDialog) {"
start_idx = content.find("if (showPhoneConfirmDialog) {")
end_idx = content.find("RivavaLoadingOverlay(isLoading = authState == AuthState.LOADING)")

new_block = '''                        // Google Login Button
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(elevation = 12.dp, spotColor = Color.White, shape = RoundedCornerShape(20.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = authState != AuthState.LOADING
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp),
                                tint = AmoledBlack
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Google", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                }
            }
        }

        '''

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_block + content[end_idx:]

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
