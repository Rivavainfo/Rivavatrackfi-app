import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

# Update AuthActivity to catch the unverified exception and show the UI
old_catch = """                                if (!isRegister) {
                                    viewModel.onEmailLogin(email, password)
                                } else {"""

new_catch = """                                if (!isRegister) {
                                    viewModel.onEmailLogin(email, password)
                                    // In a real scenario we'd observe a state for unverified, but we can also check the message
                                    if (errorMessage?.contains("verify your email") == true) {
                                        showEmailVerificationUI = true
                                    }
                                } else {"""

content = content.replace(old_catch, new_catch)

# Let's fix the AuthActivity LaunchedEffect for errorMessage to also show the UI if needed
old_effect = """    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }"""

new_effect = """    LaunchedEffect(errorMessage) {
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

content = content.replace(old_effect, new_effect)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
