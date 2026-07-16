import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

target = """    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        android.util.Log.d("GoogleSignIn", "Activity result received: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
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
                val msg = "Sign in failed: code ${e.statusCode}, message: ${e.message}"
                android.util.Log.e("GoogleSignIn", msg, e)
                signInError = msg
            } catch (e: Exception) {
                val msg = "Sign in unexpected error: ${e.message}"
                android.util.Log.e("GoogleSignIn", msg, e)
                signInError = msg
            }
        } else {
            val msg = "Activity result NOT OK: ${result.resultCode}"
            android.util.Log.d("GoogleSignIn", msg)
            signInError = msg
        }
    }"""

replacement = """    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

if target in content:
    content = content.replace(target, replacement)
else:
    print("TARGET NOT FOUND!")

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
