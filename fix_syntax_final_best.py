import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

# Let's just fix the whole `HorizontalPager` block by replacing it with a clean, perfectly bracketed version of itself.

search = """                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp,
                        beyondBoundsPageCount = 1
                    ) { page ->
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))) {
                        val newsItem = displayNews[page]
                        val newsUrl = newsItem.url.ifBlank { "https://www.google.com/search?q=${newsItem.headline}" }
                        val newsImage = if (newsItem.image.isNotBlank()) newsItem.image else "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=60"
                        val newsHeadline = newsItem.headline

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    try {
                                        uriHandler.openUri(newsUrl)
                                    } catch (e: Exception) {}
                                }
                        ) {
                            coil.compose.AsyncImage(
                                model = newsImage,
                                contentDescription = "Abstract digital visualization",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.4f
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                    )
                            )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "MARKET NEWS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = SecondaryPink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = newsHeadline,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                lineHeight = 32.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "READ ANALYSIS",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimarySky
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = PrimarySky,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Pager Indicators
                                if (displayNews.size > 1) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(displayNews.size) { index ->
                                            val isSelected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .height(6.dp)
                                                    .width(if (isSelected) 16.dp else 6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(if (isSelected) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
                }
            }"""

replace = """                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp,
                        beyondBoundsPageCount = 1
                    ) { page ->
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))) {
                            val newsItem = displayNews[page]
                            val newsUrl = newsItem.url.ifBlank { "https://www.google.com/search?q=${newsItem.headline}" }
                            val newsImage = if (newsItem.image.isNotBlank()) newsItem.image else "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=60"
                            val newsHeadline = newsItem.headline

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        try {
                                            uriHandler.openUri(newsUrl)
                                        } catch (e: Exception) {}
                                    }
                            ) {
                                coil.compose.AsyncImage(
                                    model = newsImage,
                                    contentDescription = "Abstract digital visualization",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.4f
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                                    MaterialTheme.colorScheme.background
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "MARKET NEWS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        ),
                                        color = SecondaryPink
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = newsHeadline,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White,
                                        lineHeight = 32.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "READ ANALYSIS",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = PrimarySky
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = PrimarySky,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Pager Indicators
                                        if (displayNews.size > 1) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                repeat(displayNews.size) { index ->
                                                    val isSelected = pagerState.currentPage == index
                                                    Box(
                                                        modifier = Modifier
                                                            .height(6.dp)
                                                            .width(if (isSelected) 16.dp else 6.dp)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(if (isSelected) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

content = content.replace(search, replace)
# Also fix trailing braces to exactly match the 4 outer closures (LazyColumn, Scaffold, and function)
content = re.sub(r'\}\s*\}\s*\}\s*\}\s*$', '}\n}\n', content)

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(content)
