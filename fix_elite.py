with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteDashboardActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = lines[:104] + ['    }\n', '}\n']

with open("app/src/main/java/com/rivavafi/universal/ui/elite/EliteDashboardActivity.kt", "w") as f:
    f.writelines(new_lines)
