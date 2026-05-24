with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i in [195, 196, 197]:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.writelines(new_lines)
