import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

replacement = """            } catch (e: GetCredentialException) {
                if (e.type == androidx.credentials.exceptions.GetCredentialException.TYPE_NO_CREDENTIAL) {
                    signInError = "No Google account found on this device. Please add a Google account in the device Settings and try again."
                } else {
                    signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
                }
            } catch (e: Exception) {"""

content = content.replace("""            } catch (e: GetCredentialException) {
                signInError = "GetCredentialException: ${e.type} - ${e.errorMessage}"
            } catch (e: Exception) {""", replacement)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
