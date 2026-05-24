with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    lines = f.readlines()

print("190-210:")
for i in range(190, min(210, len(lines))):
    print(f"{i+1}: {lines[i].rstrip()}")
