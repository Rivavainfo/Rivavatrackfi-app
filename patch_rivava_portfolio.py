import re

with open('app/src/main/java/com/trackfi/ui/portfolio/RivavaPortfolioScreen.kt', 'r') as f:
    content = f.read()

# Replace padding for top header and remove net worth section / upgrade card completely
# and apply new styles for lists.

content = re.sub(r'// ---------------- HEADER ----------------.*?// ---------------- NSE ----------------', r'// ---------------- HEADER ----------------\n            item {\n                Row(\n                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    Text(\n                        "Market Overview",\n                        style = MaterialTheme.typography.headlineMedium.copy(\n                            fontWeight = FontWeight.Bold\n                        ),\n                        color = Color.White\n                    )\n\n                    Row(\n                        modifier = Modifier.clickable { },\n                        verticalAlignment = Alignment.CenterVertically\n                    ) {\n                        Text(\n                            "See all",\n                            color = Color(0xFF98CBFF)\n                        )\n                        Icon(\n                            Icons.AutoMirrored.Filled.ArrowForwardIos,\n                            null,\n                            tint = Color(0xFF98CBFF),\n                            modifier = Modifier.size(12.dp)\n                        )\n                    }\n                }\n            }\n\n            // ---------------- NSE ----------------', content, flags=re.DOTALL)

# Remove the TOTAL NET WORTH item.
content = re.sub(r'// ---------------- USER BALANCE ----------------.*?// ---------------- HEADER ----------------', '// ---------------- HEADER ----------------', content, flags=re.DOTALL)


with open('app/src/main/java/com/trackfi/ui/portfolio/RivavaPortfolioScreen.kt', 'w') as f:
    f.write(content)
