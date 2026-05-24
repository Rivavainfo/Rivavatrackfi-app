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
