import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

# Update onEmailLogin to redirect to verification UI
old_login = """                } else {
                    repository.auth.signOut()
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                }"""

new_login = """                } else {
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                    // Trigger a state change that the UI can observe to show the verification screen
                    throw com.google.firebase.auth.FirebaseAuthInvalidCredentialsException("unverified", "Email not verified")
                }"""

content = content.replace(old_login, new_login)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
