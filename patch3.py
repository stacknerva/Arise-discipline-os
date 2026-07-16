import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """    override fun onCreate(savedInstanceState: Bundle?) {"""
replacement = """    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.syncTimeNow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
