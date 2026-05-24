with open("app/src/main/java/com/rivavafi/universal/HomeActivity.kt", "r") as f:
    content = f.read()

content = content.replace('    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)',
                          '    object OtpVerification : Screen("otp_verification", "OTP Verification", Icons.Outlined.Home)\n    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)')

old_phone_input = '''            composable(Screen.PhoneInput.route) {
                PhoneInputScreen(onNavigateNext = {
                    navController.navigate(Screen.SmsOptIn.route) {
                        popUpTo(Screen.PhoneInput.route) { inclusive = true }
                    }
                })
            }'''

new_phone_input_and_otp = '''            composable(Screen.PhoneInput.route) {
                PhoneInputScreen(onNavigateNext = {
                    navController.navigate(Screen.OtpVerification.route) {
                        popUpTo(Screen.PhoneInput.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.OtpVerification.route) {
                com.rivavafi.universal.ui.onboarding.OtpVerificationScreen(onNavigateNext = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                })
            }'''

content = content.replace(old_phone_input, new_phone_input_and_otp)

with open("app/src/main/java/com/rivavafi/universal/HomeActivity.kt", "w") as f:
    f.write(content)
