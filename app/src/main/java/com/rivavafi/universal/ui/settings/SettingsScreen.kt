package com.rivavafi.universal.ui.settings

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
import com.rivavafi.universal.domain.usecase.ScanSmsUseCase
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import com.rivavafi.universal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onRestartApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isSmsTrackingEnabled by viewModel.isSmsTrackingEnabled.collectAsState()
    val smsTrackingMode by viewModel.smsTrackingMode.collectAsState()
    val layoutPreset by viewModel.homeLayoutPreset.collectAsState()
    val banksDetected by viewModel.banksDetected.collectAsState()
    val showSmsDetails by viewModel.showSmsDetails.collectAsState()
    val terminologyMode by viewModel.terminologyMode.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSmsRationaleDialog by remember { mutableStateOf(false) }
    var showSmsSettingsDialog by remember { mutableStateOf(false) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCsv(context, uri) { result ->
                result.onSuccess { path ->
                    Toast.makeText(context, "Exported successfully", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
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
        containerColor = Color(0xFF131313),
        modifier = Modifier.systemBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rivava_logo),
                    contentDescription = "Rivava Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Rivava+",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF98CBFF)
                    )
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(-1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage your financial data and privacy preferences.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFBEC7D4)
                )
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .glassMorphism(cornerRadius = 24f, alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
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
                            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                            csvExportLauncher.launch("Rivava_Backup_$timestamp.csv")
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Import from CSV") },
                        supportingContent = { Text("Restore transactions from a backup file") },
                        leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                        modifier = Modifier.clickable {
                            csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "application/vnd.ms-excel", "*/*"))
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00A3FF).copy(alpha = 0.8f),
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
                                color = Color.White
                            )
                            Text(
                                text = "Display merchant, category, and date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBEC7D4)
                            )
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
                        text = "App Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00A3FF).copy(alpha = 0.8f),
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
                                color = Color.White
                            )
                            Text(
                                text = if (terminologyMode == "CREDIT_DEBIT") "Detect credit and debits from bank messages" else "Detect income and expenses from bank messages",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBEC7D4)
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
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00A3FF)
                            )
                        )
                    }

                    if (isSmsTrackingEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tracking Mode",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = smsTrackingMode == com.rivavafi.universal.domain.preferences.SmsTrackingMode.BOTH.name,
                                    onClick = { viewModel.setSmsTrackingMode(com.rivavafi.universal.domain.preferences.SmsTrackingMode.BOTH.name) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF), unselectedColor = Color(0xFFBEC7D4))
                                )
                                Text(if (terminologyMode == "CREDIT_DEBIT") "Credit and debits" else "Credit and debits", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = smsTrackingMode == com.rivavafi.universal.domain.preferences.SmsTrackingMode.CREDIT_ONLY.name,
                                    onClick = { viewModel.setSmsTrackingMode(com.rivavafi.universal.domain.preferences.SmsTrackingMode.CREDIT_ONLY.name) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF), unselectedColor = Color(0xFFBEC7D4))
                                )
                                Text(if (terminologyMode == "CREDIT_DEBIT") "Credit only" else "Income only", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = smsTrackingMode == com.rivavafi.universal.domain.preferences.SmsTrackingMode.DEBIT_ONLY.name,
                                    onClick = { viewModel.setSmsTrackingMode(com.rivavafi.universal.domain.preferences.SmsTrackingMode.DEBIT_ONLY.name) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF), unselectedColor = Color(0xFFBEC7D4))
                                )
                                Text(if (terminologyMode == "CREDIT_DEBIT") "Debits only" else "Expenses only", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
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
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTerminology() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Use Credit/Debit terminology", color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = terminologyMode == "CREDIT_DEBIT",
                            onCheckedChange = { viewModel.toggleTerminology() }
                        )
                    }
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
