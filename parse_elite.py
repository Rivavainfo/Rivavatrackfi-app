with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteDashboardActivity.kt", "r") as f:
    lines = f.readlines()

print("90-115:")
for i in range(90, min(115, len(lines))):
    print(f"{i+1}: {lines[i].rstrip()}")
