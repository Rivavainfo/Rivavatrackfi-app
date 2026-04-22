with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    lines = f.readlines()

# The issue is that resendVerificationEmail is placed outside the AuthViewModel class.
# We will find the closing brace of the class, insert the method before it, and remove the extra brace.

for i in range(len(lines)-1, -1, -1):
    if lines[i].strip() == "}":
        class_end = i
        break

# The current structure has `}` at line 414, which closes `AuthViewModel`, and then `resendVerificationEmail` is from 416-435.
content = "".join(lines)
old_tail = """            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP verification failed", e)
                _errorMessage.value = "OTP failed. Try again or use test number"
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
                onError()
            }
        }
    }
}

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val user = repository.auth.currentUser
                if (user != null && user.email != null) {
                    repository.sendVerificationEmail(user.email!!, user.uid)
                    _errorMessage.value = "Verification email resent successfully."
                } else {
                    _errorMessage.value = "User not found. Please log in again."
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to resend verification email", e)
                _errorMessage.value = "Failed to resend verification email."
            } finally {
                _authState.value = AuthState.IDLE
            }
        }
    }
"""

new_tail = """            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP verification failed", e)
                _errorMessage.value = "OTP failed. Try again or use test number"
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
                onError()
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val user = repository.auth.currentUser
                if (user != null && user.email != null) {
                    repository.sendVerificationEmail(user.email!!, user.uid)
                    _errorMessage.value = "Verification email resent successfully."
                } else {
                    _errorMessage.value = "User not found. Please log in again."
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to resend verification email", e)
                _errorMessage.value = "Failed to resend verification email."
            } finally {
                _authState.value = AuthState.IDLE
            }
        }
    }
}
"""

content = content.replace(old_tail, new_tail)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
