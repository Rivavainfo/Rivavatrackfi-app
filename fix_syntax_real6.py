import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

search = """                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                        )
                                    }

                                            uriHandler.openUri(newsItem.url)
                                        } catch (e: Exception) {}
                                    }
                                    .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {"""

replace = """                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        try {
                                            uriHandler.openUri(newsItem.url)
                                        } catch (e: Exception) {}
                                    }
                                    .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {"""

content = content.replace(search, replace)
with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(content)
