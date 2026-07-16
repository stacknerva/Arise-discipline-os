import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

target = """    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
    }"""

replacement = """    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("583623407618-h14m9dv8p4sr20dctugmjn2mijfor37i.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
    }"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("TARGET NOT FOUND!")

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
