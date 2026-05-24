with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    lines = f.readlines()

print("390-420:")
for i in range(390, min(420, len(lines))):
    print(f"{i+1}: {lines[i].rstrip()}")
