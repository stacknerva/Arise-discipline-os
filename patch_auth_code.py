import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import androidx.compose.runtime.rememberCoroutineScope
"""
if "CredentialManager" not in content:
    content = content.replace("import com.google.firebase.auth.FirebaseAuth", imports + "import com.google.firebase.auth.FirebaseAuth")

# Replace launcher
target_launcher = """    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        android.util.Log.d("GoogleSignIn", "Activity result received: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            android.util.Log.d("GoogleSignIn", "Google account obtained: ${account.email}, ID token null? ${account.idToken == null}")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            android.util.Log.d("GoogleSignIn", "Firebase credential created")
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    android.util.Log.d("GoogleSignIn", "Authentication success")
                    googleAccount = account
                    viewModel.handleSignIn(context)
                } else {
                    val msg = "Firebase auth failed: ${authTask.exception?.message}"
                    android.util.Log.e("GoogleSignIn", msg, authTask.exception)
                    signInError = msg
                }
            }
        } catch (e: ApiException) {
            val msg = "Sign in failed: code ${e.statusCode}" + 
                if (e.statusCode == 10) " (DEVELOPER_ERROR: check SHA1/Web Client ID/Support Email)" 
                else if (e.statusCode == 12500) " (SIGN_IN_FAILED: check support email in Firebase console)" 
                else ", message: ${e.message}"
            android.util.Log.e("GoogleSignIn", msg, e)
            signInError = msg
        } catch (e: Exception) {
            val msg = "Sign in unexpected error: ${e.message}"
            android.util.Log.e("GoogleSignIn", msg, e)
            signInError = msg
        }
    }"""

replacement_launcher = """    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    
    val doGoogleSignIn = {
        coroutineScope.launch {
            try {
                val hashedNonce = UUID.randomUUID().toString()
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("583623407618-h14m9dv8p4sr20dctugmjn2mijfor37i.apps.googleusercontent.com")
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                
                val credential = result.credential
                if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            googleAccount = GoogleSignIn.getLastSignedInAccount(context)
                            viewModel.handleSignIn(context)
                        } else {
                            signInError = "Firebase auth failed: ${authTask.exception?.message}"
                        }
                    }
                } else {
                    signInError = "Unexpected credential type"
                }
            } catch (e: GetCredentialException) {
                signInError = "GetCredentialException: ${e.errorMessage}"
            } catch (e: Exception) {
                signInError = "Exception: ${e.message}"
            }
        }
    }"""

if target_launcher in content:
    content = content.replace(target_launcher, replacement_launcher)

target_button = """                onSignIn = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },"""
replacement_button = """                onSignIn = { doGoogleSignIn() },"""
content = content.replace(target_button, replacement_button)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
