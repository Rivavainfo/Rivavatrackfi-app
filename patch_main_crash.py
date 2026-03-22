import re

with open('app/src/main/java/com/trackfi/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix runBlocking in onCreate and replace with Splash/Loading state or handle via compose
on_create_old = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val hasCompletedOnboarding = runBlocking {
            preferencesRepository.hasCompletedOnboardingFlow.first()
        }

        setContent {
            val isPremiumUser = preferencesRepository.isPremiumUserFlow.collectAsState(initial = false).value
            TrackFiTheme(isPremium = isPremiumUser) {
                TrackFiAppContent(hasCompletedOnboarding, preferencesRepository)
            }
        }
    }"""

on_create_new = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val hasCompletedOnboardingState = preferencesRepository.hasCompletedOnboardingFlow.collectAsState(initial = null)
            val isPremiumUser = preferencesRepository.isPremiumUserFlow.collectAsState(initial = false).value

            val hasCompletedOnboarding = hasCompletedOnboardingState.value

            if (hasCompletedOnboarding != null) {
                TrackFiTheme(isPremium = isPremiumUser) {
                    TrackFiAppContent(hasCompletedOnboarding, preferencesRepository)
                }
            } else {
                // Loading screen while datastore is resolving
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }
        }
    }"""

content = content.replace(on_create_old, on_create_new)

# Fix runBlocking in SmsOptIn route
sms_opt_old = """            composable(Screen.SmsOptIn.route) {
                SmsOptInScreen(onNavigateNext = { optedIn ->
                    val alreadyScanned = runBlocking { preferencesRepository?.smsScanCompletedFlow?.first() ?: false }
                    if (optedIn && !alreadyScanned) {
                        navController.navigate(Screen.Scanning.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    }
                })
            }"""

sms_opt_new = """            composable(Screen.SmsOptIn.route) {
                val alreadyScannedState = preferencesRepository?.smsScanCompletedFlow?.collectAsState(initial = false)
                val alreadyScanned = alreadyScannedState?.value ?: false

                SmsOptInScreen(onNavigateNext = { optedIn ->
                    if (optedIn && !alreadyScanned) {
                        navController.navigate(Screen.Scanning.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    }
                })
            }"""

content = content.replace(sms_opt_old, sms_opt_new)

with open('app/src/main/java/com/trackfi/MainActivity.kt', 'w') as f:
    f.write(content)
