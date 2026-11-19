@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.monethra.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.monethra.utils.NotificationHelper
import com.example.monethra.workers.DailyReminderWorker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import com.example.monethra.ui.components.ExportPdfRecapDialog
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.monethra.data.model.CurrencyModel
import com.example.monethra.ui.theme.BudgetPulseTheme
import com.example.monethra.data.model.WorldCurrency
import com.example.monethra.data.model.GLOBAL_CURRENCY_CATALOG
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monethra.MonethraApplication
import com.example.monethra.ui.theme.DeepIndigo
import com.example.monethra.ui.theme.PastelPeriwinkle
import com.example.monethra.viewmodel.ProfileViewModel
import com.example.monethra.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = run {
        val context = LocalContext.current
        val app = context.applicationContext as? MonethraApplication
        if (app != null) {
            viewModel(factory = ProfileViewModelFactory(app.financeRepository, app.authRepository))
        } else {
            viewModel()
        }
    },
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as? MonethraApplication
    val authRepository = app?.authRepository

    val user by viewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val userEmail = user?.email.orEmpty().ifBlank { uiState.userEmail.orEmpty() }
    val userName = user?.name.orEmpty().ifBlank { uiState.userName }.ifBlank { "User" }
    val initials = userName.firstOrNull()?.uppercase() ?: "U"
    val avatarInitial = initials.toString()
    val isDemoAccount = userEmail.isNotBlank() && userEmail.equals("monthra@andro.app", ignoreCase = true)
    val paydayStartDay = uiState.paydayStartDay.coerceIn(1, 28)
    val selectedCurrency = uiState.currencySymbol.ifBlank { "Rp" }
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationHelper = remember { NotificationHelper(context) }

    val enableNotificationsWithEffects: () -> Unit = {
        viewModel.updateNotificationSettings(true)
        DailyReminderWorker.schedule(context)
        notificationHelper.showConfirmationNotification()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableNotificationsWithEffects()
        } else {
            viewModel.updateNotificationSettings(false)
            Log.d(NotificationHelper.TAG, "POST_NOTIFICATIONS permission denied by user")
        }
    }

    var isAboutDialogOpen by remember { mutableStateOf(false) }
    var isExportPdfDialogOpen by remember { mutableStateOf(false) }
    var isLogoutDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                ) {
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF1E1B4B), // Deep indigo-charcoal background
                        contentColor = Color.White,          // Crisp white readable text
                        actionColor = Color(0xFF9FA0FF),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Screen Title
                    Text(
                        text = "Profile & Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage user details, preferences, and data",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // User Header Card
                    UserProfileCard(
                        userName = userName,
                        userEmail = if (userEmail.isNotBlank()) userEmail else "No email set",
                        avatarInitial = avatarInitial,
                        onEditClick = { viewModel.openEditProfileDialog() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferences Section
                    PreferencesSection(
                        currencySymbol = selectedCurrency,
                        notificationsEnabled = uiState.notificationsEnabled,
                        isDarkMode = uiState.isDarkMode,
                        paydayStartDay = paydayStartDay,
                        onCurrencyClick = { viewModel.openCurrencyDialog() },
                        onPaydayStartDayClick = { viewModel.openPaydayStartDayDialog() },
                        onNotificationsToggle = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        enableNotificationsWithEffects()
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    enableNotificationsWithEffects()
                                }
                            } else {
                                viewModel.updateNotificationSettings(false)
                                DailyReminderWorker.cancel(context)
                            }
                        },
                        onDarkModeToggle = { enabled -> viewModel.updateDarkMode(enabled) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Data Management Section
                    DataManagementSection(
                        isDemoAccount = isDemoAccount,
                        onExportPdfRecapClick = { isExportPdfDialogOpen = true },
                        onLoadSampleDataClick = { viewModel.openSampleDataDialog() },
                        onResetDataClick = { viewModel.openResetDataDialog() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Info Section
                    AppInfoSection(
                        onAboutClick = { isAboutDialogOpen = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Log Out Section Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLogoutDialogOpen = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Log Out",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Dialogs
            if (uiState.isEditProfileDialogOpen) {
                EditProfileDialog(
                    initialName = userName,
                    initialEmail = userEmail,
                    onDismiss = { viewModel.closeEditProfileDialog() },
                    onConfirm = { name, email ->
                        viewModel.updateProfile(name, email)
                    }
                )
            }

            if (uiState.isCurrencyDialogOpen) {
                CurrencySelectorDialog(
                    currentCurrencyCode = uiState.currencyCode,
                    availableCurrencies = uiState.availableCurrencies,
                    onDismiss = { viewModel.closeCurrencyDialog() },
                    onSelectCurrency = { currency ->
                        viewModel.selectCurrency(currency)
                    },
                    onOpenAddCustomCurrency = {
                        viewModel.openAddCustomCurrencyDialog()
                    },
                    onDeleteCustomCurrency = { code ->
                        viewModel.deleteCustomCurrency(code)
                    }
                )
            }

            if (uiState.isAddCustomCurrencyDialogOpen) {
                AddCurrencyPickerDialog(
                    existingCurrencyCodes = uiState.availableCurrencies.map { it.code },
                    cachedRates = uiState.cachedRates,
                    addingCurrencyCode = uiState.addingCurrencyCode,
                    onDismiss = { viewModel.closeAddCustomCurrencyDialog() },
                    onSelectCurrency = { worldCurrency ->
                        viewModel.addCurrencyFromWorldCatalog(worldCurrency)
                    }
                )
            }

            if (uiState.isPaydayStartDayDialogOpen) {
                PaydaySelectionSheet(
                    initialDay = paydayStartDay,
                    onDismissRequest = { viewModel.closePaydayStartDayDialog() },
                    onConfirm = { day ->
                        viewModel.updatePaydayStartDay(day)
                        viewModel.closePaydayStartDayDialog()
                    }
                )
            }

            if (uiState.isSampleDataDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeSampleDataDialog() },
                    title = { Text("Load Sample Data?", fontWeight = FontWeight.Bold) },
                    text = { Text("This will overwrite existing financial records with realistic sample transactions and budget entries.") },
                    confirmButton = {
                        Button(onClick = { viewModel.loadSampleData() }) {
                            Text("Load Sample Data")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeSampleDataDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (uiState.isResetDataDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeResetDataDialog() },
                    title = { Text("Reset All Financial Data?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                    text = { Text("Are you sure you want to permanently delete all transactions, budgets, and category allocations? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.resetFinancialData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Reset Data", color = MaterialTheme.colorScheme.onError)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeResetDataDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (isAboutDialogOpen) {
                AboutMonethraDialog(onDismiss = { isAboutDialogOpen = false })
            }

            if (isExportPdfDialogOpen) {
                ExportPdfRecapDialog(
                    currentMonth = com.example.monethra.utils.DateUtils.getCurrentMonth(),
                    currentYear = com.example.monethra.utils.DateUtils.getCurrentYear(),
                    paydayStartDay = uiState.paydayStartDay,
                    onConfirmExport = { month, year ->
                        viewModel.exportPdfRecap(context, month, year)
                    },
                    onDismiss = { isExportPdfDialogOpen = false }
                )
            }

            if (isLogoutDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isLogoutDialogOpen = false },
                    title = { Text("Log Out Confirmation", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to log out of BudgetPulse?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isLogoutDialogOpen = false
                                viewModel.logout(authRepository) {
                                    onLogout()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Log Out", color = MaterialTheme.colorScheme.onError)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isLogoutDialogOpen = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserProfileCard(
    userName: String,
    userEmail: String?,
    avatarInitial: String = userName.firstOrNull()?.uppercase()?.toString() ?: "U",
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarInitial,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = userEmail ?: "No email set",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PreferencesSection(
    currencySymbol: String,
    notificationsEnabled: Boolean,
    isDarkMode: Boolean,
    paydayStartDay: Int,
    onCurrencyClick: () -> Unit,
    onPaydayStartDayClick: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Preferences",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Currency Row
            SettingsRowItem(
                icon = Icons.Rounded.AttachMoney,
                title = "Currency",
                subtitle = "Selected symbol: $currencySymbol",
                trailing = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currencySymbol,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                onClick = onCurrencyClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Payday Start Date Row
            SettingsRowItem(
                icon = Icons.Rounded.Event,
                title = "Payday Start Date",
                subtitle = "Cycle starts on day $paydayStartDay of each month",
                trailing = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Day $paydayStartDay",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                onClick = onPaydayStartDayClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Notifications Toggle Row
            SettingsRowItem(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                subtitle = "Receive budget alerts and reminders",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Dark Mode Toggle Row
            SettingsRowItem(
                icon = Icons.Rounded.DarkMode,
                title = "Dark Mode",
                subtitle = "Invert palette to monochrome dark",
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun DataManagementSection(
    isDemoAccount: Boolean,
    onExportPdfRecapClick: () -> Unit,
    onLoadSampleDataClick: () -> Unit,
    onResetDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Data Management",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Export Monthly Recap as PDF Button
            Button(
                onClick = onExportPdfRecapClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Monthly Recap to PDF",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (isDemoAccount) {
                Spacer(modifier = Modifier.height(10.dp))

                // Load Sample Data Button
                OutlinedButton(
                    onClick = onLoadSampleDataClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Sample Data", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset Data Button
                OutlinedButton(
                    onClick = onResetDataClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Financial Data", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


@Composable
fun AppInfoSection(
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Application Info",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BudgetPulse",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Personal Finance Tracker",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v1.0.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            SettingsRowItem(
                icon = Icons.AutoMirrored.Rounded.Help,
                title = "About BudgetPulse & Privacy",
                subtitle = "App information, features & privacy details",
                onClick = onAboutClick
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.trim().ifBlank { "BudgetPulse User" }
                    val finalEmail = email.trim().ifBlank { null }
                    onConfirm(finalName, finalEmail)
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CurrencySelectorDialog(
    currentCurrencyCode: String,
    availableCurrencies: List<CurrencyModel>,
    onDismiss: () -> Unit,
    onSelectCurrency: (CurrencyModel) -> Unit,
    onOpenAddCustomCurrency: () -> Unit,
    onDeleteCustomCurrency: (String) -> Unit
) {
    var selectedCode by remember(currentCurrencyCode) { mutableStateOf(currentCurrencyCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Currency Preference",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Financial amounts will convert automatically. Base records remain in IDR.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                availableCurrencies.forEach { currency ->
                    val isSelected = currency.code.equals(selectedCode, ignoreCase = true)
                    val rateToIdr = if (currency.rateFromIdr > 0) 1.0 / currency.rateFromIdr else 1.0
                    val formattedRate = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                        .format(kotlin.math.round(rateToIdr).toLong())

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCode = currency.code },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedCode = currency.code },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currency.code} (${currency.symbol}) - ${currency.name}",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currency.code.equals("IDR", ignoreCase = true)) {
                                        "Base Currency (1 IDR = Rp 1)"
                                    } else {
                                        "1 ${currency.code} ≈ Rp $formattedRate"
                                    },
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!currency.isDefault) {
                                IconButton(
                                    onClick = { onDeleteCustomCurrency(currency.code) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete ${currency.code}",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // + Add World Currency Button
                OutlinedButton(
                    onClick = onOpenAddCustomCurrency,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add World Currency",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rates updated automatically • Real-time",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chosen = availableCurrencies.find { it.code.equals(selectedCode, ignoreCase = true) }
                        ?: availableCurrencies.firstOrNull()
                    if (chosen != null) {
                        onSelectCurrency(chosen)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun AddCurrencyPickerDialog(
    existingCurrencyCodes: List<String>,
    cachedRates: Map<String, Double>,
    addingCurrencyCode: String?,
    onDismiss: () -> Unit,
    onSelectCurrency: (WorldCurrency) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Normalize existing currency codes to uppercase for robust filtering
    val existingNormalized = remember(existingCurrencyCodes) {
        existingCurrencyCodes.map { it.uppercase().trim() }.toSet()
    }

    // Filter catalog: exclude currencies already present, and match query
    val availableCurrencies = remember(searchQuery, existingNormalized) {
        val trimmedQuery = searchQuery.trim()
        GLOBAL_CURRENCY_CATALOG.filter { currency ->
            val codeUpper = currency.code.uppercase()
            !existingNormalized.contains(codeUpper) &&
                (trimmedQuery.isEmpty() ||
                    codeUpper.contains(trimmedQuery, ignoreCase = true) ||
                    currency.name.contains(trimmedQuery, ignoreCase = true) ||
                    currency.symbol.contains(trimmedQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (addingCurrencyCode == null) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Add World Currency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select from global currencies with live rates",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Instant Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search currency (e.g. JPY, Euro)...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (availableCurrencies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No currencies found matching \"$searchQuery\"" else "All available currencies have been added",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableCurrencies, key = { it.code }) { currency ->
                            val isAddingThis = addingCurrencyCode == currency.code
                            val isAddingAny = addingCurrencyCode != null
                            val rateFromIdr = cachedRates[currency.code] ?: currency.fallbackRateFromIdr
                            val rateToIdr = if (rateFromIdr > 0.0) 1.0 / rateFromIdr else 1.0
                            val formattedRate = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                                .format(kotlin.math.round(rateToIdr).toLong())

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAddingThis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isAddingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        enabled = !isAddingAny,
                                        onClick = { onSelectCurrency(currency) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Currency Code Pill Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = currency.code,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${currency.symbol}  ${currency.name}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "1 ${currency.code} ≈ Rp $formattedRate",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isAddingThis) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = addingCurrencyCode == null,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4B5563)
                )
            ) {
                Text(
                    text = "Close",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun AboutMonethraDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PrivacyTip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("About BudgetPulse", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "BudgetPulse is a modern personal finance tracking app built using Jetpack Compose, Room Database, and Material Design 3.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Key Features:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• Income & Expense tracking with custom categories\n• Monthly budget planning with dynamic threshold alerts\n• Visual reports with Bar, Line, and Donut charts\n• Automated financial recap insights\n• 100% offline-first local data storage",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreferencesSectionPreview() {
    BudgetPulseTheme(darkTheme = false) {
        PreferencesSection(
            currencySymbol = "Rp",
            notificationsEnabled = true,
            isDarkMode = false,
            paydayStartDay = 1,
            onCurrencyClick = {},
            onPaydayStartDayClick = {},
            onNotificationsToggle = {},
            onDarkModeToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreferencesSectionDarkPreview() {
    BudgetPulseTheme(darkTheme = true) {
        PreferencesSection(
            currencySymbol = "Rp",
            notificationsEnabled = true,
            isDarkMode = true,
            paydayStartDay = 1,
            onCurrencyClick = {},
            onPaydayStartDayClick = {},
            onNotificationsToggle = {},
            onDarkModeToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileActionsDarkPreview() {
    BudgetPulseTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DataManagementSection(
                isDemoAccount = true,
                onExportPdfRecapClick = {},
                onLoadSampleDataClick = {},
                onResetDataClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = "Log Out",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
