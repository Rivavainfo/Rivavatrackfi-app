import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

old_verify = """                onClick = {
                    viewModel.checkEmailVerified(
                        onVerified = { onLoginSuccess(isNewUser ?: false) },
                        onNotVerified = { }
                    )
                },"""

new_verify = """                onClick = {
                    viewModel.checkEmailVerified(
                        onVerified = { onLoginSuccess(isNewUser == true) },
                        onNotVerified = { }
                    )
                },"""

content = content.replace(old_verify, new_verify)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
