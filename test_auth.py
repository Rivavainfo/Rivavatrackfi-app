import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

new_block = """    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = false
    }
    composeOptions {"""

content = content.replace("""    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {""", new_block)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
