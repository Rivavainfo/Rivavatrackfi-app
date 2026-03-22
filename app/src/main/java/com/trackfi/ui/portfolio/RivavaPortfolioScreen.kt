@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(onNavigateToDetail: (String) -> Unit) {

    Scaffold(
        containerColor = Color(0xFF131313)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------- TOP BAR ----------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF353535)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Rivava",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF98CBFF)
                            )
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF98CBFF)
                        )
                    }
                }
            }

            // ---------------- HERO ----------------
            item {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {

                    Text(
                        "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFBEC7D4)
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$142,850",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFFE2E2E2)
                        )
                        Text(
                            ".42",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF98CBFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .background(
                                Color(0xFF00E471).copy(alpha = 0.1f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            null,
                            tint = Color(0xFF00E471),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+12.4%", color = Color(0xFF00E471))
                    }
                }
            }

            // ---------------- HEADER ----------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Market Overview",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "See all",
                            color = Color(0xFF98CBFF)
                        )
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            null,
                            tint = Color(0xFF98CBFF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // ---------------- NSE ----------------
            item {
                Column {
                    Text(
                        "NSE MARKET",
                        color = Color(0xFF98CBFF),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val nseItems = portfolioItems.filter { it.exchange == "NSE" }

                    nseItems.forEach { item ->
                        PortfolioStockCard(
                            exchange = item.exchange,
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = item.marketPrice,
                            isPremium = false,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .clickable {
                                    onNavigateToDetail(item.ticker)
                                }
                        )
                    }
                }
            }

            // ---------------- NYSE ----------------
            item {
                Column {
                    Text(
                        "NYSE MARKET",
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val nyseItems = portfolioItems.filter { it.exchange != "NSE" }

                    nyseItems.forEach { item ->
                        NysePortfolioStockCard(
                            exchange = "NYSE",
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = item.marketPrice,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .clickable {
                                    onNavigateToDetail(item.ticker)
                                }
                        )
                    }
                }
            }
        }
    }
}