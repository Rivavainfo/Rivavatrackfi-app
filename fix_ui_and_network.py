import re

# Fix AuthActivity.kt to handle "verify your email" check flexibly
with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

# Make the check case-insensitive and handle both "verify your email" and "Email not verified"
old_effect = """    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                if (it.contains("verify your email")) {
                    showEmailVerificationUI = true
                }
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }"""

new_effect = """    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                if (it.contains("verify your email", ignoreCase = true) || it.contains("email not verified", ignoreCase = true)) {
                    showEmailVerificationUI = true
                }
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }"""
content = content.replace(old_effect, new_effect)

old_login_btn = """                                    // In a real scenario we'd observe a state for unverified, but we can also check the message
                                    if (errorMessage?.contains("verify your email") == true) {
                                        showEmailVerificationUI = true
                                    }"""

new_login_btn = """                                    // In a real scenario we'd observe a state for unverified, but we can also check the message
                                    if (errorMessage?.contains("verify your email", ignoreCase = true) == true || errorMessage?.contains("email not verified", ignoreCase = true) == true) {
                                        showEmailVerificationUI = true
                                    }"""
content = content.replace(old_login_btn, new_login_btn)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)

# Fix missing resend button.
# Oh, we DID add it in our previous fix_auth_ui.py but we reverted everything by mistake or the review was evaluating an older state.
# Let's verify if the resend button is actually missing.
