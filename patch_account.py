import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace googleAccount variable definition
content = content.replace("var googleAccount by remember { mutableStateOf(GoogleSignIn.getLastSignedInAccount(context)) }", 
                          "var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }")

# Replace googleAccount usages
content = content.replace("googleAccount = GoogleSignIn.getLastSignedInAccount(context)", "currentUser = FirebaseAuth.getInstance().currentUser")
content = content.replace("googleAccount = null", "currentUser = null")
content = content.replace("account = googleAccount", "account = currentUser")

# Update AccountSection parameter
content = content.replace("fun AccountSection(account: GoogleSignInAccount?", "fun AccountSection(account: com.google.firebase.auth.FirebaseUser?")

# Update AccountSection property access
content = content.replace("account.displayName", "account.displayName") # Same
content = content.replace("account.email", "account.email") # Same
content = content.replace("account.photoUrl", "account.photoUrl") # Same

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
