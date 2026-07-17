import os

workflow_path = ".github/workflows/android.yml"
with open(workflow_path, "r") as f:
    content = f.read()

new_steps = """    - name: Make Gradle wrapper executable
      run: chmod +x gradlew

    - name: Decode Keystore
      env:
        KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        echo $KEYSTORE_BASE64 | base64 --decode > app/release.keystore

    - name: Build Signed Release APK
      env:
        KEYSTORE_PATH: release.keystore
        STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew assembleRelease

    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: ARISE-APK
        path: app/build/outputs/apk/release/app-release.apk"""

content = content.split("    - name: Make Gradle wrapper executable")[0] + new_steps

with open(workflow_path, "w") as f:
    f.write(content)
