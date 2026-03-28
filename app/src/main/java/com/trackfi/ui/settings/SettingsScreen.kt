package com.trackfi.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Psychology

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackfi.domain.usecase.ScanSmsUseCase
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.Color
import com.trackfi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onRestartApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isSmsTrackingEnabled by viewModel.isSmsTrackingEnabled.collectAsState()
    val layoutPreset by viewModel.homeLayoutPreset.collectAsState()
    val banksDetected by viewModel.banksDetected.collectAsState()
    val showSmsDetails by viewModel.showSmsDetails.collectAsState()
    val smartCategorizationEnabled by viewModel.smartCategorizationEnabled.collectAsState()
    val biometricLockEnabled by viewModel.biometricLockEnabled.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSmsRationaleDialog by remember { mutableStateOf(false) }
    var showSmsSettingsDialog by remember { mutableStateOf(false) }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importCsv(context, uri) { result ->
                result.onSuccess { count ->
                    Toast.makeText(context, "Successfully imported $count transactions", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.READ_SMS] == true &&
                         permissions[Manifest.permission.RECEIVE_SMS] == true

        if (smsGranted) {
            viewModel.setSmsTrackingEnabled(true)
            Toast.makeText(context, "SMS Tracking Enabled", Toast.LENGTH_SHORT).show()
        } else {
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS)
            } ?: false

            if (!shouldShowRationale) {
                showSmsSettingsDialog = true
            } else {
                viewModel.setSmsTrackingEnabled(false)
                Toast.makeText(context, "SMS tracking remains disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF131313)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(-0.5f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Text(
                    text = "Manage your financial data and privacy preferences.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFBEC7D4)
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI & LEARNING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF98CBFF).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF98CBFF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .background(Color(0xFF98CBFF).copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BETA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF98CBFF),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF98CBFF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF00A3FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Predictive Analysis",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Allow AI to study spending habits for future budget forecasting.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBEC7D4),
                                lineHeight = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Switch(
                                checked = isSmsTrackingEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        showSmsRationaleDialog = true
                                    } else {
                                        viewModel.setSmsTrackingEnabled(false)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00A3FF)
                                )
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFAEDB).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFAEDB),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Smart Categorization",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Automatically tag and group similar transactions across accounts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBEC7D4),
                                lineHeight = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Switch(
                                checked = smartCategorizationEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.setSmartCategorizationEnabled(isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF38C5)
                                )
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "PRIVACY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF98CBFF).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF98CBFF),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Show Transaction Details",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Reveal merchant names in main dashboard",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFBEC7D4)
                                    )
                                }
                            }
                            Switch(
                                checked = showSmsDetails,
                                onCheckedChange = { isChecked ->
                                    viewModel.setShowSmsDetails(isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00A3FF)
                                )
                            )
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 24.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF98CBFF),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Biometric Lock",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Require FaceID/Fingerprint for transfers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFBEC7D4)
                                    )
                                }
                            }
                            Switch(
                                checked = biometricLockEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.setBiometricLockEnabled(isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00A3FF)
                                )
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "DATA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF98CBFF).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.exportCsv(context) { result ->
                                    result.onSuccess { path ->
                                        Toast.makeText(context, "Exported to $path", Toast.LENGTH_LONG).show()
                                    }.onFailure { e ->
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFF00A3FF),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = "Export CSV Statement",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFBEC7D4)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                csvImportLauncher.launch("text/comma-separated-values")
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = Color(0xFF00A3FF),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = "Import Transaction History",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFBEC7D4)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.clearAiLearning()
                                Toast.makeText(context, "Learning data reset.", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF93000A).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB4AB),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = "Reset AI Learning",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFB4AB)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFB4AB).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "DESTRUCTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFFB4AB).copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
                                    )
                                )
                            }
                        }
                    }
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00A3FF).copy(alpha = 0.8f),
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Advanced Options",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF98CBFF).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All data is securely stored locally on your device. We do not use any tracking analytics or cloud services.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFBEC7D4)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Logout",
                                color = Color(0xFF98CBFF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showClearDataDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB4AB).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Clear History",
                                color = Color(0xFFFFB4AB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RIVAVA+ V2.4.0",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = Color(0xFFBEC7D4)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By modifying these settings, you agree to our updated Data Processing Agreement and AI Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = Color(0xFFBEC7D4).copy(alpha = 0.4f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }

    if (showSmsRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showSmsRationaleDialog = false },
            title = { Text("Enable Automatic Tracking?") },
            text = { Text("This feature can read transaction SMS to help track financial activity. This is optional.") },
            confirmButton = {
                TextButton(onClick = {
                    showSmsRationaleDialog = false
                    val perms = mutableListOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                }) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSmsRationaleDialog = false
                    viewModel.setSmsTrackingEnabled(false)
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSmsSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSmsSettingsDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("You have permanently denied SMS permissions. Please enable them manually in the app settings if you wish to use automatic tracking.") },
            confirmButton = {
                TextButton(onClick = {
                    showSmsSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSmsSettingsDialog = false
                    viewModel.setSmsTrackingEnabled(false)
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Transaction History?") },
            text = { Text("This will permanently delete all your logged transactions. Your profile and portfolio access will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTransactionHistory()
                        showClearDataDialog = false
                        Toast.makeText(context, "Transaction history cleared.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
