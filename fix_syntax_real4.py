with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    lines = f.readlines()

# The error says "195:51 Expecting ')'"
# "247:45 Expecting an element"
# "340:1 Expecting a top level declaration"

# Ok! Let's actually look at the unmodified file at line 191:
print("Original file lines 185-195")
for i in range(185, 195):
    print(f"{i+1}: {lines[i]}", end='')
