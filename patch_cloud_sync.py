import re

with open("app/src/main/java/com/example/data/CloudSyncManager.kt", "r") as f:
    content = f.read()

target = """    suspend fun fetchFromCloud(): Boolean {"""
replacement = """    @Suppress("UNCHECKED_CAST")
    suspend fun fetchFromCloud(): Boolean {"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/data/CloudSyncManager.kt", "w") as f:
    f.write(content)
