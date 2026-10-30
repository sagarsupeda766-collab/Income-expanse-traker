package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataConfirmation by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showBackupResultDialog by remember { mutableStateOf<String?>(null) }
    var showPrivacyInfoDialog by remember { mutableStateOf(false) }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            Pair("₹", "Indian Rupee (INR)"),
            Pair("$", "US Dollar (USD)"),
            Pair("€", "Euro (EUR)"),
            Pair("£", "British Pound (GBP)"),
            Pair("AED", "UAE Dirham (AED)"),
            Pair("¥", "Japanese Yen (JPY)")
        )
        Dialog(onDismissRequest = { showCurrencyDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Choose Currency",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    currencies.forEach { (symbol, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrencySymbol(symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currencySymbol == symbol,
                                onClick = {
                                    viewModel.setCurrencySymbol(symbol)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$symbol - $name",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currencySymbol == symbol) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        val themes = listOf(
            Pair("SYSTEM", "System Default"),
            Pair("LIGHT", "Light Mode"),
            Pair("DARK", "Dark Mode")
        )
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmation = false },
            title = { Text("Clear All Transactions?") },
            text = { Text("This will permanently delete all your expense records. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData {
                            Toast.makeText(context, "All expense records cleared.", Toast.LENGTH_SHORT).show()
                        }
                        showClearDataConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Backup Result Dialog
    if (showBackupResultDialog != null) {
        val jsonBackup = showBackupResultDialog!!
        Dialog(onDismissRequest = { showBackupResultDialog = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Backup Ready",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your expense data has been packaged into a standard backup format. You can copy it to your clipboard to save in notes or cloud storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = jsonBackup,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBackupResultDialog = null }) {
                            Text("Done")
                        }
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HisabPro Backup", jsonBackup)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                showBackupResultDialog = null
                            }
                        ) {
                            Text("Copy Backup")
                        }
                    }
                }
            }
        }
    }

    // Restore Dialog
    if (showRestoreDialog) {
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Restore Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste your previously saved JSON backup content below to restore your transactions and categories:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("Paste JSON backup here...") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val success = viewModel.restoreJson(restoreJsonInput.trim())
                                    if (success) {
                                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_LONG).show()
                                        showRestoreDialog = false
                                        restoreJsonInput = ""
                                    } else {
                                        Toast.makeText(context, "Invalid backup format. Please check JSON data.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        ) {
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }

    // Privacy Dialog
    if (showPrivacyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyInfoDialog = false },
            title = { Text("Privacy & Security") },
            text = {
                Text(
                    "HisabPro is designed with privacy as the highest priority.\n\n" +
                            "• All transaction records, categories, and budgets are securely stored on your device in a local SQLite/Room database.\n" +
                            "• No personal financial data is transmitted to external servers without your explicit action.\n" +
                            "• You can backup or export your records at any time to CSV or JSON formats."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Section: Regional & Appearance
        item {
            Text(
                text = "Regional & Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.CurrencyRupee,
                        title = "Currency",
                        subtitle = "Selected: $currencySymbol (Indian Rupee default)",
                        onClick = { showCurrencyDialog = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme Mode",
                        subtitle = "Selected: $themeMode",
                        onClick = { showThemeDialog = true }
                    )
                }
            }
        }

        // Section: Data & Backup
        item {
            Text(
                text = "Data Export & Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.FileDownload,
                        title = "Export to CSV",
                        subtitle = "Copy all transaction history in CSV format for Excel/Sheets",
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportCsv()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HisabPro Expenses CSV", csv)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "CSV exported & copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Backup Records (JSON)",
                        subtitle = "Create a complete backup of all transactions and budgets",
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.exportJson()
                                showBackupResultDialog = json
                            }
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Restore,
                        title = "Restore from Backup",
                        subtitle = "Import and restore previously saved expense data",
                        onClick = { showRestoreDialog = true }
                    )
                }
            }
        }

        // Section: Testing & Tools
        item {
            Text(
                text = "Testing & Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.VolunteerActivism,
                        title = "Load Sample Transactions",
                        subtitle = "Populates realistic Indian household expenses across October 2026",
                        onClick = {
                            viewModel.seedSampleData {
                                Toast.makeText(context, "Sample transactions loaded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = "Privacy & Security Info",
                        subtitle = "Device-local data guarantees & zero cloud leaks",
                        onClick = { showPrivacyInfoDialog = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Records",
                        subtitle = "Permanently remove all expenses and start fresh",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearDataConfirmation = true }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
