with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    lines = f.readlines()

# The original trace:
# 191|                                         )
# 192|                                     )
# 193|                             )
# The user wants:
# 191|                                         )
# 192|                                     }
# 193: removed

for i, line in enumerate(lines):
    if "androidx.compose.ui.graphics.Brush.verticalGradient" in line:
        # found it!
        lines[i+5] = "                                            )\n"
        lines[i+6] = "                                        )\n"
        lines[i+7] = "                                    }\n"
        lines[i+8] = "                            \n"

# And exactly one brace at the end of the composable!
# Let's count them
count = 0
for i in range(len(lines)):
    if "fun RivavaPortfolioScreen" in lines[i]:
        count += 1
