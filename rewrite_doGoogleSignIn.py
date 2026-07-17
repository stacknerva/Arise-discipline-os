import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Let's find the start of `val doGoogleSignIn = {` and the end of the block.
# We know it's inside `fun SettingsScreen(...)`.
# Instead of regex, let's just find the index of `val doGoogleSignIn = {`
# and find the matching closing brace.

start_idx = content.find("val doGoogleSignIn = {")
if start_idx == -1:
    print("Could not find doGoogleSignIn")
    exit(1)

brace_count = 0
end_idx = -1
in_block = False

for i in range(start_idx, len(content)):
    if content[i] == '{':
        brace_count += 1
        in_block = True
    elif content[i] == '}':
        brace_count -= 1
        
    if in_block and brace_count == 0:
        end_idx = i
        break

if end_idx == -1:
    print("Could not find end of doGoogleSignIn")
    exit(1)

original = content[start_idx:end_idx+1]

fixed = """val doGoogleSignIn = {
        coroutineScope.launch {
            try {
                val webClientId = context.getString(R.string.default_web_client_id)
                android.util.Log.d("SettingsScreen", "Starting Google Sign-In with Web Client ID: $webClientId")
                
                val hashedNonce = UUID.randomUUID().toString()
                android.util.Log.d("SettingsScreen", "Building GetGoogleIdOption...")
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(false)
                    .build()
                    
                android.util.Log.d("SettingsScreen", "Building GetCredentialRequest...")
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                    
                android.util.Log.d("SettingsScreen", "Calling credentialManager.getCredential()...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                
                android.util.Log.d("SettingsScreen", "Received result, parsing credential...")
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    android.util.Log.d("SettingsScreen", "Creating GoogleIdTokenCredential from data...")
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    
                    android.util.Log.d("SettingsScreen", "Authenticating with Firebase...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            android.util.Log.d("SettingsScreen", "Firebase Auth successful.")
                            currentUser = FirebaseAuth.getInstance().currentUser
                            viewModel.handleSignIn(context)
                            signInError = null
                        } else {
                            val msg = "Firebase auth failed: ${authTask.exception?.message}"
                            android.util.Log.e("SettingsScreen", msg, authTask.exception)
                            signInError = msg
                        }
                    }
                } else {
                    android.util.Log.e("SettingsScreen", "Unexpected credential type: ${credential.type}")
                    signInError = "Unexpected credential type"
                }
            } catch (e: GetCredentialException) {
                val msg = "GetCredentialException: type=${e.type}, msg=${e.errorMessage}\\n${e.message}"
                android.util.Log.e("SettingsScreen", msg, e)
                signInError = msg
            } catch (e: Exception) {
                val msg = "Exception during sign in: ${e.message}"
                android.util.Log.e("SettingsScreen", msg, e)
                signInError = msg
            }
        }
    }"""

content = content[:start_idx] + fixed + content[end_idx+1:]

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)

print("Replaced successfully")

