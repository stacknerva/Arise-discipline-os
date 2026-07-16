import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun SyncStatusSection(account: GoogleSignInAccount?", "fun SyncStatusSection(account: com.google.firebase.auth.FirebaseUser?")

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
