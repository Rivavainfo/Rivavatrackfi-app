import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

# Let's fix the portfolio crash again properly from the beginning state.
# We know the issue was an early `return` before `collectAsState`.
# We need to wrap the Scaffold in `if (!isUnlocked) { ... } else { ... }` carefully.

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

    val iredaPrice = portfolioViewModel.iredaPrice.collectAsState(initial = 0.0).value
    val iredaPreviousClose = portfolioViewModel.iredaPreviousClose.collectAsState(initial = 0.0).value
    val isLoading = portfolioViewModel.isLoading.collectAsState(initial = true).value
    val isError = portfolioViewModel.isError.collectAsState(initial = false).value

    val stockStates by viewModel.stockStates.collectAsState()

    val marketNews by viewModel.marketNews.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(emptyList()) // maintain for other dependencies like news
        cryptoViewModel.startPolling(cryptoIds)
    }"""

replace = """    val iredaPrice = portfolioViewModel.iredaPrice.collectAsState(initial = 0.0).value
    val iredaPreviousClose = portfolioViewModel.iredaPreviousClose.collectAsState(initial = 0.0).value
    val isLoading = portfolioViewModel.isLoading.collectAsState(initial = true).value
    val isError = portfolioViewModel.isError.collectAsState(initial = false).value

    val stockStates by viewModel.stockStates.collectAsState()

    val marketNews by viewModel.marketNews.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

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
