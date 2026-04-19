import re

with open('./app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt', 'r') as f:
    content = f.read()

# Fix the syntax error caused by .height(56.dp), .background()
# It should be .height(56.dp).background()
search_text = ".height(56.dp),\n                                .background"
replace_text = ".height(56.dp)\n                                .background"
content = content.replace(search_text, replace_text)

# And for the ones that end the modifier
search_text2 = ".height(56.dp),\n                                \n                            colors ="
replace_text2 = ".height(56.dp),\n                            colors ="
content = content.replace(search_text2, replace_text2)

# Also fix the trailing comma before colors for the background ones
search_text3 = "shape = RoundedCornerShape(20.dp)\n                                )\n                                \n                            colors ="
replace_text3 = "shape = RoundedCornerShape(20.dp)\n                                ),\n                            colors ="
content = content.replace(search_text3, replace_text3)


with open('./app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt', 'w') as f:
    f.write(content)
