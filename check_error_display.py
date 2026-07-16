with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()
if "signInError" in content:
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if "signInError" in line:
            print(f"{i}: {line}")
