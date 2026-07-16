echo "=== 1 & 2. Signing Certificate and Fingerprints ==="
gradle app:signingReport | grep -A 5 "Variant: debug$"
echo "=== 3 & 8. Firebase google-services.json ==="
cat app/google-services.json | grep package_name
cat app/google-services.json | grep certificate_hash
echo "=== 4. APK Package Name ==="
grep "applicationId" app/build.gradle.kts
echo "=== 5 & 6 & 7. Web Client IDs ==="
cat app/google-services.json | grep -A 2 "client_type\": 3"
cat app/build/generated/res/processDebugGoogleServices/values/values.xml | grep default_web_client_id
