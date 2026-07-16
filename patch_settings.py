import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add Suppress DEPRECATION
target_func = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(viewModel: DisciplineViewModel) {"""
replacement_func = """@Suppress("DEPRECATION")
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(viewModel: DisciplineViewModel) {"""
content = content.replace(target_func, replacement_func)

# Fix Toast messages
target_catch = """                    } else {
                        Toast.makeText(context, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show()
            }"""
replacement_catch = """                    } else {
                        Toast.makeText(context, "Firebase auth failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }"""
content = content.replace(target_catch, replacement_catch)

# Also suppress deprecation in AccountSection and SyncStatusSection
target_account = """fun AccountSection(account: GoogleSignInAccount?, onSignIn: () -> Unit, onSignOut: () -> Unit) {"""
replacement_account = """@Suppress("DEPRECATION")
@Composable
fun AccountSection(account: GoogleSignInAccount?, onSignIn: () -> Unit, onSignOut: () -> Unit) {"""
content = content.replace(target_account, replacement_account)

target_sync = """fun SyncStatusSection(account: GoogleSignInAccount?, lastSyncTime: String?, onSync: () -> Unit) {"""
replacement_sync = """@Suppress("DEPRECATION")
@Composable
fun SyncStatusSection(account: GoogleSignInAccount?, lastSyncTime: String?, onSync: () -> Unit) {"""
content = content.replace(target_sync, replacement_sync)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
