import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add signInError state
target_state = """    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current"""
replacement_state = """    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var signInError by remember { mutableStateOf<String?>(null) }"""
content = content.replace(target_state, replacement_state)

# Replace launcher
target_launcher = """    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        googleAccount = account
                        viewModel.handleSignIn(context)
                    } else {
                        Toast.makeText(context, "Firebase auth failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }"""
replacement_launcher = """    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
content = content.replace(target_launcher, replacement_launcher)

# Add AlertDialog at the end of the composable
target_end = """    if (isAddingRoutine) {
        AddRoutineDialog("""
replacement_end = """    if (signInError != null) {
        AlertDialog(
            onDismissRequest = { signInError = null },
            title = { Text("Sign In Error") },
            text = { Text(signInError ?: "") },
            confirmButton = {
                TextButton(onClick = { signInError = null }) { Text("OK") }
            }
        )
    }

    if (isAddingRoutine) {
        AddRoutineDialog("""
content = content.replace(target_end, replacement_end)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
