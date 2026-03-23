package com.trackfi.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackfi.ui.theme.glassMorphism

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
                    Toast.makeText(context, "Imported $count transactions", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.READ_SMS] == true
        if (smsGranted) {
            viewModel.setSmsTrackingEnabled(true)
        } else {
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS)
            } ?: false
            if (!shouldShowRationale) showSmsSettingsDialog = true
            viewModel.setSmsTrackingEnabled(false)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            Column {
                Text("WealthCurator Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Manage your financial data and privacy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // AI & SMS Section
            SettingsSection(title = "AI & AUTOMATION") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        SettingsToggleItem(
                            title = "Automatic SMS Tracking",
                            subtitle = "Detect transactions from bank messages",
                            icon = Icons.Default.Psychology,
                            checked = isSmsTrackingEnabled,
                            onCheckedChange = { if (it) showSmsRationaleDialog = true else viewModel.setSmsTrackingEnabled(false) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color.White.copy(alpha = 0.05f))
                        SettingsToggleItem(
                            title = "Show Merchant Details",
                            subtitle = "Reveal names in dashboard",
                            icon = Icons.Default.Visibility,
                            checked = showSmsDetails,
                            onCheckedChange = { viewModel.setShowSmsDetails(it) }
                        )
                    }
                }
            }

            // Appearance Section
            SettingsSection(title = "APPEARANCE") {
                Card(modifier = Modifier.glassMorphism(cornerRadius = 24f, alpha = 0.1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        listOf("Minimal", "Analytics", "Daily Tracker").forEach { template ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.setHomeLayoutPreset(template) }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (template == layoutPreset), onClick = { viewModel.setHomeLayoutPreset(template) })
                                Text(template, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }

            // Data Section
            SettingsSection(title = "DATA MANAGEMENT") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DataItem(Icons.Default.Download, "Export CSV Statement") {
                        viewModel.exportCsv(context) { it.onSuccess { path -> Toast.makeText(context, "Saved to $path", Toast.LENGTH_LONG).show() } }
                    }
                    DataItem(Icons.Default.Upload, "Import History") {
                        csvImportLauncher.launch("text/comma-separated-values")
                    }
                    DataItem(Icons.Default.Delete, "Clear All Data", isDestructive = true) {
                        showClearDataDialog = true
                    }
                }
            }

            // Footer
            Text(
                "Version 2.4.0\nLocally encrypted and stored.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    // Dialogs
    if (showSmsRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showSmsRationaleDialog = false },
            title = { Text("Enable Auto-Tracking?") },
            text = { Text("We need permission to read transaction SMS. Data never leaves your device.") },
            confirmButton = {
                TextButton(onClick = {
                    showSmsRationaleDialog = false
                    val perms = mutableListOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    permissionLauncher.launch(perms.toTypedArray())
                }) { Text("Enable") }
            },
            dismissButton = { TextButton(onClick = { showSmsRationaleDialog = false }) { Text("Cancel") } }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Delete Everything?") },
            text = { Text("This action cannot be undone. All transactions will be wiped.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData(); onRestartApp() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DataItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = if (isDestructive) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) else null
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(label, fontWeight = FontWeight.Bold, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}