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
