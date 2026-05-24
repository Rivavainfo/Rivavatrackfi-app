import re
with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/PhoneInputScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'val errorMessage by viewModel\.errorMessage\.collectAsState\(\)',
                 r'val errorMessage by viewModel.errorMessage.collectAsState()\n    var showConfirmDialog by remember { mutableStateOf(false) }',
                 content)

content = content.replace('''            onClick = {
                val formattedNumber = phoneNumber.trim()
                if (formattedNumber.length == 10) {
                    viewModel.savePhoneNumber(formattedNumber) {
                        onNavigateNext()
                    }
                }
            },''', '''            onClick = {
                val formattedNumber = phoneNumber.trim()
                if (formattedNumber.length == 10) {
                    showConfirmDialog = true
                }
            },''')

dialog_code = '''

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(text = "Confirm Number", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Is this your correct phone number?", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "+91 $phoneNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimarySky)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.startPhoneVerification(phoneNumber)
                    onNavigateNext()
                }) {
                    Text("Yes, Continue", fontWeight = FontWeight.Bold, color = PrimarySky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Edit Number", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = AmoledBlack,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

'''
content = content.replace('    RivavaLoadingOverlay(isLoading = isLoading)', dialog_code + '    RivavaLoadingOverlay(isLoading = isLoading)')

with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/PhoneInputScreen.kt", "w") as f:
    f.write(content)
