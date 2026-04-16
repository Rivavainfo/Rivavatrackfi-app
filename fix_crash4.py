import re

# `return` in `@Composable` is technically fine in Compose if not interrupting Compose nodes unexpectedly,
# but it skips `val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()` and `LaunchedEffect`.
# Calling `collectAsState()` conditionally (or skipping it via `return` before it's called) violates Compose rules
# because hooks (remember, collectAsState, LaunchedEffect) must be called in the same order every recomposition.
# THAT IS THE CAUSE OF THE CRASH!
# "java.lang.IllegalStateException: ... remember must be called ... "

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()


search = """    if (!isUnlocked) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔒 Portfolio Locked",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showUnlockDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Unlock for ₹11 or Enter Key")
                    }
                }
            }
        }
        return
    }






    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(emptyList()) // maintain for other dependencies like news
        cryptoViewModel.startPolling(cryptoIds)
    }"""

replace = """    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(emptyList()) // maintain for other dependencies like news
        cryptoViewModel.startPolling(cryptoIds)
    }

    if (!isUnlocked) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔒 Portfolio Locked",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showUnlockDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Unlock for ₹11 or Enter Key")
                    }
                }
            }
        }
        return
    }"""

content = content.replace(search, replace)

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(content)
