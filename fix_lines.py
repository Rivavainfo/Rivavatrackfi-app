with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i == 94:
        new_lines.append('    var premiumKeyInput by remember { mutableStateOf("") }\n')
        continue
    if i in [95, 96, 97]:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/rivavafi/universal/ui/portfolio/RivavaPortfolioScreen.kt", "w") as f:
    f.writelines(new_lines)
