import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

replacement = """            } catch (e: GetCredentialException) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Credential Exception: ${e.type} - ${e.errorMessage}", e)
                signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("SettingsScreen", "Exception: ${e.message}", e)
                signInError = "Exception: ${e.message}"
            }"""

content = content.replace("""            } catch (e: GetCredentialException) {
                if (e.type.contains("TYPE_NO_CREDENTIAL", ignoreCase = true) || e.errorMessage?.toString()?.contains("no credentials", ignoreCase = true) == true) {
                    signInError = "No Google account found on this device. Please add a Google account in the device Settings and try again."
                } else {
                    signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
                }
            } catch (e: Exception) {""", replacement)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
