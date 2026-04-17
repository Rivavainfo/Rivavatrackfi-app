import re

with open("app/src/main/java/com/trackfi/ui/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

search = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                            text = "Automatic SMS Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = if (isSmsTrackingEnabled) "Active" else "Disabled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSmsTrackingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        }
                        Switch(
                            checked = isSmsTrackingEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    showSmsRationaleDialog = true
                                } else {
                                    viewModel.setSmsTrackingEnabled(false)
                                }
                            }
                        )
                    }
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val templates = listOf("Minimal", "Analytics", "Daily Tracker", "Subscription View")

                    templates.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setHomeLayoutPreset(template) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (template == layoutPreset),
                                onClick = { viewModel.setHomeLayoutPreset(template) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = template,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (banksDetected.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Analytics Options",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        banksDetected.forEach { bank ->
                            Text(
                                text = bank,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Advanced",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All data is securely stored locally on your device. We do not use any tracking analytics or cloud services.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.logout()
                                onRestartApp()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Logout",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = { showClearDataDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Clear History",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }"""

replace = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                    Text("Manage your financial data and privacy preferences.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("AI & LEARNING", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary.copy(alpha=0.8f))
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f), RoundedCornerShape(16.dp)).padding(horizontal=12.dp, vertical=4.dp)) {
                            Text("BETA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).height(180.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                        Icon(androidx.compose.material.icons.Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Predictive Analysis", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text("Study spending habits.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f).height(180.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha=0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                        Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Smart Categorization", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text("Auto tag tracking via SMS.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                    Switch(
                                        checked = isSmsTrackingEnabled,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                showSmsRationaleDialog = true
                                            } else {
                                                viewModel.setSmsTrackingEnabled(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("PRIVACY", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary.copy(alpha=0.8f))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Show Transaction Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("Reveal merchant names in main dashboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(checked = false, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha=0.1f))
                            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Biometric Lock", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("Require FaceID/Fingerprint", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("DATA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary.copy(alpha=0.8f))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable { viewModel.logout(); onRestartApp() }
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(androidx.compose.material.icons.Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Logout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            }
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha=0.1f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha=0.1f), RoundedCornerShape(20.dp))
                                .clickable { showClearDataDialog = true }
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(androidx.compose.material.icons.Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Clear Data & Reset AI Learning", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.error)
                            }
                            Text("DESTRUCTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.error.copy(alpha=0.6f), modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=8.dp, vertical=4.dp))
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WealthCurator v1.0.2", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("By modifying these settings, you agree to our updated Data Processing Agreement and AI Privacy Policy.", style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.width(280.dp))
                }
            }
        }"""

if search in content:
    content = content.replace(search, replace)
    with open("app/src/main/java/com/trackfi/ui/settings/SettingsScreen.kt", "w") as f:
        f.write(content)
        print("Replaced successfully")
else:
    print("Search string not found")
