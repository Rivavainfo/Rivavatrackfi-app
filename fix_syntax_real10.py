with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    lines = f.readlines()

# The error is at line 245-248!
# 245:                         items.forEach { item ->
# 246:                                             )
# 247:                                         )
# 248:                                     }
# 249:
# 250:                                 currency + String.format(Locale.getDefault(), "%.2f", it.c)

# Oh my god. I replaced the wrong code earlier when trying to fix lines 191-193.
# Let's fix lines 246-249.

lines[246] = "                            val state = stockStates[item.ticker]\n"
lines[247] = "                            val currency = if (item.exchange == \"NSE\") \"₹\" else \"$\"\n"
lines[248] = "                            val displayPrice = state?.data?.let {\n"
lines[249] = ""


with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.write(''.join(lines))
