import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("// implementation(libs.androidx.credentials)", "implementation(libs.androidx.credentials)")
content = content.replace("// implementation(libs.androidx.credentials.play.services)", "implementation(libs.androidx.credentials.play.services)")

if "googleid" not in content:
    content = content.replace("implementation(libs.androidx.credentials.play.services)", 'implementation(libs.androidx.credentials.play.services)\n  implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
