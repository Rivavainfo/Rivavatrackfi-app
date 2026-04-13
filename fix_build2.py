import re

# Since `BuildConfig.NEWS_API_KEY` isn't compiling because build features `buildConfig = true` might not be working or the project needs to be clean, let's just make sure it's declared cleanly in `build.gradle.kts`.

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Let's see what is actually in the defaultConfig
# print(content)
