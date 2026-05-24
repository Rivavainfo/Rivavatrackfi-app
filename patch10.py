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
