import re

# `ActivityNotFoundException` inside `PremiumUnlockDialog` on click might crash? No, it's inside a try catch.
# `startActivityForResult` with implicit activity intent might crash if `PaymentActivity` is not exported properly? It's exported=false but in the same package.
# Or maybe `context.getSharedPreferences` is throwing something if `context` is not valid? It is.
# Look at `PaymentActivity.kt`, wait, the issue is that it crashes "when i click on rivava portfolio section", not after clicking a button.
# "when i click on rivava portfolio section it keeps crasding" -> This means navigating to `RivavaPortfolioScreen` crashes.

with open("app/src/main/java/com/rivavafi/universal/HomeActivity.kt", "r") as f:
    content = f.read()
    if "RivavaPortfolioScreen" in content:
        print("HomeActivity has RivavaPortfolioScreen")
