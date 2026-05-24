import re
with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "r") as f:
    content = f.read()

content = content.replace('var authMethod by remember { mutableStateOf("INITIAL") }', '')
content = content.replace('var phoneNumber by remember { mutableStateOf("") }', '')
content = content.replace('var showPhoneConfirmDialog by remember { mutableStateOf(false) }', '')
content = content.replace('var tempName by remember { mutableStateOf("") }', '')
content = content.replace('var tempEmail by remember { mutableStateOf("") }', '')
content = content.replace('var tempPhotoUrl by remember { mutableStateOf("") }', '')

content = re.sub(r'tempName\s*=\s*account\.displayName.*?\n', '\n', content)
content = re.sub(r'tempEmail\s*=\s*account\.email.*?\n', '\n', content)
content = re.sub(r'tempPhotoUrl\s*=\s*account\.photoUrl\?\.toString\(\).*?\n', '\n', content)

content = re.sub(r'\s*LaunchedEffect\(phoneAuthState\)\s*\{\s*if\s*\(phoneAuthState == PhoneAuthState\.CODE_SENT\)\s*\{\s*\}\s*\}', '', content)
content = re.sub(r'\s*LaunchedEffect\(requiresProfileCompletion\)\s*\{\s*if\s*\(requiresProfileCompletion\)\s*\{\s*authMethod = "PROFILE_COMPLETION"\s*\}\s*\}', '', content)
content = re.sub(r'\s*if\s*\(showPhoneConfirmDialog\)\s*\{\s*AlertDialog\([\s\S]*?\}\s*\)', '', content)

start_marker = 'if (authMethod == "PROFILE_COMPLETION") {'
end_marker = '} else if (authMethod == "INITIAL") {'
if start_marker in content and end_marker in content:
    start_idx = content.find(start_marker)
    end_idx = content.find(end_marker) + len(end_marker)
    content = content[:start_idx] + content[end_idx:]

content = content.replace('                        }\n\n\n\n                    }\n\n                }', '                        }\n\n                }')

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthActivity.kt", "w") as f:
    f.write(content)
