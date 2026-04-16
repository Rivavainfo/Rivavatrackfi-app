import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

# Let's fix the specific lines 191-193 exactly as the user said.
# The user's trace states:
# 191|                                         )
# 192|                                     }
# remove these lines

lines = content.split('\n')
lines[190] = "                                        )"
lines[191] = "                                    }"
lines[192] = ""

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write('\n'.join(lines))
