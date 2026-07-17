import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# We want to replace this exact block:
original = """            } catch (e: GetCredentialException) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Credential Exception: ${e.type} - ${e.errorMessage}", e)
                signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Exception: ${e.message}", e)
                signInError = "Exception: ${e.message}"
            }
                signInError = "Exception: ${e.message}"
            }
        }
    }"""

fixed = """            } catch (e: GetCredentialException) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Credential Exception: ${e.type} - ${e.errorMessage}", e)
                signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Exception: ${e.message}", e)
                signInError = "Exception: ${e.message}"
            }
        }
    }"""

content = content.replace(original, fixed)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)

print("Done")
