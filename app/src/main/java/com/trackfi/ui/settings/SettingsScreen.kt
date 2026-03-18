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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Data",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ListItem(
                        headlineContent = { Text("Export to CSV") },
                        supportingContent = { Text("Offline backup of all your transactions") },
                        leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.exportCsv(context) { result ->
                                result.onSuccess { path ->
                                    Toast.makeText(context, "Exported to $path", Toast.LENGTH_LONG).show()
                                }.onFailure { e ->
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Import from CSV") },
                        supportingContent = { Text("Restore transactions from a backup file") },
                        leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                        modifier = Modifier.clickable {
                            csvImportLauncher.launch("text/comma-separated-values")
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Reset AI Learning") },
                        supportingContent = { Text("Clear merchant category corrections") },
                        leadingContent = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.clearAiLearning()
                            Toast.makeText(context, "Learning data reset.", Toast.LENGTH_SHORT).show()
                        }
                    )
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
                        text = "Privacy",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Transaction Details",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Display merchant, category, and date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showSmsDetails,
                            onCheckedChange = { isChecked ->
                                viewModel.setShowSmsDetails(isChecked)
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
                        text = "AI & Learning",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatic SMS Tracking",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Detect income and expenses from bank messages",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                "Clear All Data",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
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
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all your transactions, custom categories, and preferences. You will need to restart the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        onRestartApp()
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
