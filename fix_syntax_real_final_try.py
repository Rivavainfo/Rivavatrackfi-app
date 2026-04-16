import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

# 191|                                         )
# 192|                                     }

# Wait, why not just strictly replace what the user said using regular expressions or lines array!
lines = content.split('\n')

for i in range(len(lines)):
    if "androidx.compose.ui.graphics.Brush.verticalGradient" in lines[i]:
        # This is at index 240.
        # lines[245] is:                                            )
        # lines[246] is:                                        )
        # lines[247] is:                                    )
        # We need to change lines[247] to `}` and remove lines[248].
        lines[247] = "                                    }"
        lines[248] = ""
        break

# The file ends with 4 closing braces. Let's make it 2 closing braces for RivavaPortfolioScreen.
while lines[-1].strip() == "}":
    lines.pop()
lines.append("}")
lines.append("}")

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write('\n'.join(lines))
