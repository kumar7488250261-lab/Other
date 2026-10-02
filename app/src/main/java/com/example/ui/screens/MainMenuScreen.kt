package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.firebase.SyncState
import com.example.data.update.AppUpdateChecker
import com.example.data.update.AppUpdateInfo
import com.example.ui.theme.RailwayAmber
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KharsiaLobbyEmblem
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayRed

private const val COMMON_LOCKED_PASSWORD = "kharsia@cc"
private const val JEEP_LOCKED_PASSWORD = "kharsia@jeep"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    currentUserId: String,
    onNavigateToStaffDirectory: () -> Unit,
    onNavigateToEquipmentRegister: () -> Unit,
    onNavigateToPeriodicalRest: () -> Unit,
    onNavigateToLongHour: () -> Unit,
    onNavigateToJeepSubMenu: () -> Unit,
    onNavigateToRosterTlc: () -> Unit,
    onNavigateToLrDeclaration: () -> Unit,
    onNavigateToRunningRoomBoy: () -> Unit,
    onNavigateToBoxBoy: () -> Unit,
    onNavigateToSanderBoy: () -> Unit,
    onNavigateToCliPosition: () -> Unit,
    onNavigateToJeepDriverAttendance: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val syncManager = remember { FirebaseSyncManager.getInstance(context) }
    val syncState by syncManager.syncState.collectAsState()

    var updateInfo by remember { mutableStateOf(AppUpdateInfo()) }
    LaunchedEffect(Unit) {
        updateInfo = AppUpdateChecker.checkForUpdate()
    }

    // Password Prompt State
    var showPasswordDialog by remember { mutableStateOf(false) }
    var targetTitle by remember { mutableStateOf("") }
    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun handleMenuClick(item: MenuItemData, destination: () -> Unit) {
        if (item.isLocked) {
            targetTitle = item.title
            pendingNavigation = destination
            passwordInput = ""
            passwordError = null
            passwordVisible = false
            showPasswordDialog = true
        } else {
            destination()
        }
    }

    val menuItems = listOf(
        // OPEN MENUS
        MenuItemData(
            title = "Staff Call Book",
            hindiTitle = "स्टाफ डायरेक्टरी",
            subtitle = "141 Stations & Lobbies",
            icon = Icons.Default.ContactPhone,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_staff_directory",
            gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)),
            borderColor = Color(0xFF60A5FA).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Periodical Rest",
            hindiTitle = "पी.आर रिमार्क",
            subtitle = "Sign-Off & Rest",
            icon = Icons.Default.Schedule,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_periodical_rest",
            gradientColors = listOf(Color(0xFF065F46), Color(0xFF059669)),
            borderColor = Color(0xFF34D399).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Store Register",
            hindiTitle = "उपकरण रजिस्टर",
            subtitle = "Fast Issue & Return",
            icon = Icons.Default.Inventory,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_equipment_register",
            gradientColors = listOf(Color(0xFF92400E), Color(0xFFD97706)),
            borderColor = Color(0xFFFBBF24).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Long Hour Duty",
            hindiTitle = "लॉन्ग आवर अपडेट",
            subtitle = "9+ Hours Relief Monitor",
            icon = Icons.Default.Timer,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_long_hour",
            gradientColors = listOf(Color(0xFF991B1B), Color(0xFFDC2626)),
            borderColor = Color(0xFFF87171).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "JEEP DI",
            hindiTitle = "जीप डी.आई",
            subtitle = "Shift DI & Fleet Movement",
            icon = Icons.Default.DirectionsCar,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_jeep_movement",
            gradientColors = listOf(Color(0xFF155E75), Color(0xFF0891B2)),
            borderColor = Color(0xFF22D3EE).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Shift Roster",
            hindiTitle = "रोस्टर व TLC",
            subtitle = "06-14, 14-22, 22-06",
            icon = Icons.AutoMirrored.Filled.Assignment,
            isLocked = false,
            statusBadge = "OPEN",
            testTag = "menu_item_roster_tlc",
            gradientColors = listOf(Color(0xFF581C87), Color(0xFF7C3AED)),
            borderColor = Color(0xFFA78BFA).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "LR Declaration",
            hindiTitle = "एल.आर डिक्लेरेशन",
            subtitle = "Monthly Section-Wise LR",
            icon = Icons.AutoMirrored.Filled.FactCheck,
            isLocked = false,
            statusBadge = "MONTHLY LR",
            testTag = "menu_item_lr_declaration",
            gradientColors = listOf(Color(0xFF0F766E), Color(0xFF0D9488)),
            borderColor = Color(0xFF2DD4BF).copy(alpha = 0.5f)
        ),
        // 3 NEW ATTENDANCE MENUS (ALL UNLOCKED)
        MenuItemData(
            title = "Running Room Boy",
            hindiTitle = "रनिंग रूम बॉय उपस्थिति",
            subtitle = "Shift-Wise Attendance",
            icon = Icons.Default.Bed,
            isLocked = false,
            statusBadge = "SHIFT WISE",
            testTag = "menu_item_running_room_boy",
            gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF6D28D9)),
            borderColor = Color(0xFFA78BFA).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Box Boy Attendance",
            hindiTitle = "बॉक्स बॉय उपस्थिति",
            subtitle = "Shift & Date Wise Register",
            icon = Icons.Default.Work,
            isLocked = false,
            statusBadge = "SHIFT & DATE",
            testTag = "menu_item_box_boy",
            gradientColors = listOf(Color(0xFF042F2E), Color(0xFF0D9488)),
            borderColor = Color(0xFF2DD4BF).copy(alpha = 0.5f)
        ),
        MenuItemData(
            title = "Sander Filling Boy",
            hindiTitle = "सैंडर फिलिंग बॉय",
            subtitle = "Loco Sand Shift Register",
            icon = Icons.Default.Engineering,
            isLocked = false,
            statusBadge = "LOCO SAND",
            testTag = "menu_item_sander_boy",
            gradientColors = listOf(Color(0xFF78350F), Color(0xFFD97706)),
            borderColor = Color(0xFFFBBF24).copy(alpha = 0.5f)
        ),
        // 11. CLI POSITION
        MenuItemData(
            title = "CLI Position",
            hindiTitle = "सी.एल.आई स्थिति",
            subtitle = "Duty, Footplate, Rest & Leave",
            icon = Icons.Default.Badge,
            isLocked = false,
            statusBadge = "CLI CADRE",
            testTag = "menu_item_cli_position",
            gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
            borderColor = Color(0xFF60A5FA).copy(alpha = 0.5f)
        ),
        // 12. JEEP DRIVER ATTENDANCE
        MenuItemData(
            title = "Jeep Driver",
            hindiTitle = "जीप चालक उपस्थिति",
            subtitle = "Shift, Jeep No & BA Register",
            icon = Icons.Default.DirectionsCar,
            isLocked = false,
            statusBadge = "DRIVERS",
            testTag = "menu_item_jeep_driver_attendance",
            gradientColors = listOf(Color(0xFF164E63), Color(0xFF0891B2)),
            borderColor = Color(0xFF22D3EE).copy(alpha = 0.5f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KharsiaLobbyEmblem(size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KHARSIA LOBBY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "SECR Bilaspur Division • User: $currentUserId",
                                style = MaterialTheme.typography.labelSmall,
                                color = RailwayGold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackgroundNavy
                )
            )
        },
        containerColor = DarkCanvasBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (updateInfo.isUpdateAvailable) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, RailwayGold, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚀 New Version Available: v${updateInfo.latestVersion}",
                                    fontWeight = FontWeight.Bold,
                                    color = RailwayGold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Installed: v${updateInfo.currentVersion}",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "An official update is published on GitHub Releases. Update now to receive the latest features and synchronization enhancements.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    AppUpdateChecker.openUpdatePage(context, updateInfo.releaseUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("UPDATE NOW (अपडेट करें)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }


            // 2-Column Compact Colorful Box Grid
            val chunkedItems = menuItems.chunked(2)
            chunkedItems.forEach { pair ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val item1 = pair[0]
                        ColorfulMenuBox(
                            item = item1,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (item1.testTag) {
                                    "menu_item_staff_directory" -> handleMenuClick(item1, onNavigateToStaffDirectory)
                                    "menu_item_periodical_rest" -> handleMenuClick(item1, onNavigateToPeriodicalRest)
                                    "menu_item_equipment_register" -> handleMenuClick(item1, onNavigateToEquipmentRegister)
                                    "menu_item_long_hour" -> handleMenuClick(item1, onNavigateToLongHour)
                                    "menu_item_jeep_movement" -> handleMenuClick(item1, onNavigateToJeepSubMenu)
                                    "menu_item_roster_tlc" -> handleMenuClick(item1, onNavigateToRosterTlc)
                                    "menu_item_lr_declaration" -> handleMenuClick(item1, onNavigateToLrDeclaration)
                                    "menu_item_running_room_boy" -> handleMenuClick(item1, onNavigateToRunningRoomBoy)
                                    "menu_item_box_boy" -> handleMenuClick(item1, onNavigateToBoxBoy)
                                    "menu_item_sander_boy" -> handleMenuClick(item1, onNavigateToSanderBoy)
                                    "menu_item_cli_position" -> handleMenuClick(item1, onNavigateToCliPosition)
                                    "menu_item_jeep_driver_attendance" -> handleMenuClick(item1, onNavigateToJeepDriverAttendance)
                                }
                            }
                        )

                        if (pair.size > 1) {
                            val item2 = pair[1]
                            ColorfulMenuBox(
                                item = item2,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    when (item2.testTag) {
                                        "menu_item_staff_directory" -> handleMenuClick(item2, onNavigateToStaffDirectory)
                                        "menu_item_periodical_rest" -> handleMenuClick(item2, onNavigateToPeriodicalRest)
                                        "menu_item_equipment_register" -> handleMenuClick(item2, onNavigateToEquipmentRegister)
                                        "menu_item_long_hour" -> handleMenuClick(item2, onNavigateToLongHour)
                                        "menu_item_jeep_movement" -> handleMenuClick(item2, onNavigateToJeepSubMenu)
                                        "menu_item_roster_tlc" -> handleMenuClick(item2, onNavigateToRosterTlc)
                                        "menu_item_lr_declaration" -> handleMenuClick(item2, onNavigateToLrDeclaration)
                                        "menu_item_running_room_boy" -> handleMenuClick(item2, onNavigateToRunningRoomBoy)
                                        "menu_item_box_boy" -> handleMenuClick(item2, onNavigateToBoxBoy)
                                        "menu_item_sander_boy" -> handleMenuClick(item2, onNavigateToSanderBoy)
                                        "menu_item_cli_position" -> handleMenuClick(item2, onNavigateToCliPosition)
                                        "menu_item_jeep_driver_attendance" -> handleMenuClick(item2, onNavigateToJeepDriverAttendance)
                                    }
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // PASSWORD PROTECTION DIALOG (PASSWORD: kharsia@cc)
        // =========================================================================
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                containerColor = DarkSurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RailwayGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RailwayGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Password Protected (सुरक्षित मेनू)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Access to '$targetTitle' requires In-Charge password authorization.",
                            fontSize = 12.5.sp,
                            color = Color(0xFFA0B4D0),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = null
                            },
                            label = { Text("Enter Password (पासवर्ड डालें)", fontSize = 12.sp) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val expectedPassword = if (targetTitle == "JEEP DI") JEEP_LOCKED_PASSWORD else COMMON_LOCKED_PASSWORD
                                    if (passwordInput.trim() == expectedPassword) {
                                        showPasswordDialog = false
                                        pendingNavigation?.invoke()
                                    } else {
                                        passwordError = "Incorrect password! Please enter valid password."
                                    }
                                }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold,
                                unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_lock_password")
                        )

                        if (passwordError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = passwordError ?: "",
                                color = RailwayRed,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val expectedPassword = if (targetTitle == "JEEP DI") JEEP_LOCKED_PASSWORD else COMMON_LOCKED_PASSWORD
                            if (passwordInput.trim() == expectedPassword) {
                                showPasswordDialog = false
                                pendingNavigation?.invoke()
                            } else {
                                passwordError = "Incorrect password! Please enter valid password."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_unlock_menu")
                    ) {
                        Text(
                            text = "Unlock & Open",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F1E36)
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showPasswordDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun ColorfulMenuBox(
    item: MenuItemData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .border(1.2.dp, item.borderColor, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(item.gradientColors))
            .clickable { onClick() }
            .testTag(item.testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isLocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = RailwayGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "LOCKED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailwayGold
                                )
                            }
                        }
                    } else if (item.statusBadge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.statusBadge,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.statusBadge == "OPEN") RailwayGreen else RailwayGold
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    maxLines = 1
                )
                if (item.hindiTitle.isNotBlank()) {
                    Text(
                        text = item.hindiTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = RailwayGold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 9.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}
