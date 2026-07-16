import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Fix conflicting coroutineScope
content = re.sub(r'val coroutineScope = rememberCoroutineScope\(\)\s*val credentialManager = remember \{ CredentialManager.create\(context\) \}', r'val credentialManager = remember { CredentialManager.create(context) }', content)

content = re.sub(r'val coroutineScope = rememberCoroutineScope\(\)\n\s*val coroutineScope = rememberCoroutineScope\(\)', r'val coroutineScope = rememberCoroutineScope()', content)

# Fix unresolved references
content = content.replace("com.google.android.libraries.identity.googleid.GetGoogleIdOption", "com.google.android.libraries.identity.googleid.GetGoogleIdOption")
content = content.replace("com.google.android.libraries.identity.googleid.GoogleIdTokenCredential", "androidx.credentials.CustomCredential")

target = """                if (credential is androidx.credentials.CustomCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)"""

replacement = """                if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
