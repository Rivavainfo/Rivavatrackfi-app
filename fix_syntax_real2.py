import re

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    content = f.read()

# Let's just fix it manually like the user explicitly instructed without regex
lines = content.split('\n')
for i, line in enumerate(lines):
    if "colors = listOf(" in line:
        # line i is `colors = listOf(`
        # line i+1 is `Color.Transparent,`
        # line i+2 is `MaterialTheme.colorScheme.background.copy(alpha = 0.4f),`
        # line i+3 is `MaterialTheme.colorScheme.background`
        # line i+4 is `)`
        # line i+5 is `)`
        # line i+6 is `)`
        # user wants:
        # line i+4 is `)`
        # line i+5 is `}`
        # line i+6 is ` `
        lines[i+4] = "                                            )"
        lines[i+5] = "                                        )"
        lines[i+6] = "                                    }"
        lines[i+7] = ""

# To ensure exactly one closing curly brace at the end of the `RivavaPortfolioScreen` composable:
# The `RivavaPortfolioScreen` ends where `@Composable` for `CryptoCard` begins.
# Let's clean up the braces right before `@Composable \n fun CryptoCard`

search = """            }
        }
    }

@Composable
fun CryptoCard"""

replace = """            }
        }
    }
}

@Composable
fun CryptoCard"""

content = '\n'.join(lines)
content = content.replace(search, replace)

# Clean trailing braces
content = re.sub(r'\}\s*\}\s*\}\s*\}\s*$', '}\n', content)

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(content)
