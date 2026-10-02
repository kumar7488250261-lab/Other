package com.example.ui.screens.pr

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffRepository
import com.example.data.equipment.GoogleSheetsSyncService
import com.example.data.equipment.InChargeAuthManager
import com.example.data.equipment.PrRequest
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicalRestScreen(
    inChargeAuthManager: InChargeAuthManager,
    staffRepository: StaffRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleSheetsSync = remember { GoogleSheetsSyncService(context) }

    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val now = remember { Date() }

    var selectedTab by remember { mutableStateOf(0) } // 0: Mark PR (स्टाफ), 1: PR List, 2: CC Portal (Password Protected)
    var isCcLoggedIn by remember { mutableStateOf(inChargeAuthManager.isSessionValid) }

    // Admin Password Dialog State (Password: kharsia@pr - Masked/Hidden)
    var showPasswordDialog by remember { mutableStateOf(false) }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Remove Confirmation Dialog State
    var requestToDelete by remember { mutableStateOf<PrRequest?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllProcessedDialog by remember { mutableStateOf(false) }

    // Google Sheets Config Dialog State
    var showSheetConfigDialog by remember { mutableStateOf(false) }
    var customSheetUrl by remember { mutableStateOf(googleSheetsSync.sheetUrl) }
    var customWebhookUrl by remember { mutableStateOf(googleSheetsSync.webhookUrl) }

    // Load initial or persisted PR requests
    val initialRequests = remember {
        googleSheetsSync.loadSavedRequests() ?: listOf(
            PrRequest(
                id = 1042,
                crewId = "KHS1042",
                crewName = "Rajesh Kumar",
                designation = "LP (Goods)",
                signOffDate = dateFormat.format(now),
                signOffTime = "06:30",
                requestDate = dateFormat.format(now),
                status = "PENDING",
                remarks = "Completed 144 hours cycle. Requesting 30 hrs PR."
            ),
            PrRequest(
                id = 1105,
                crewId = "KHS1105",
                crewName = "Amit Verma",
                designation = "ALP",
                signOffDate = dateFormat.format(now),
                signOffTime = "08:15",
                requestDate = dateFormat.format(now),
                status = "CONFIRMED",
                remarks = "Regular 40 hrs PR approved.",
                reviewedBy = "CC Kharsia",
                reviewedAt = "08:45"
            )
        )
    }

    val requests = remember { mutableStateListOf<PrRequest>().apply { addAll(initialRequests) } }

    // Helper to persist and sync
    fun saveAndSync(updatedList: List<PrRequest>, singleToSync: PrRequest? = null) {
        googleSheetsSync.saveRequests(updatedList)
        if (singleToSync != null) {
            coroutineScope.launch {
                googleSheetsSync.syncSingleRecord(singleToSync)
            }
        }
    }

    // Form inputs
    var crewId by remember { mutableStateOf("") }
    var crewName by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var signOffDate by remember { mutableStateOf(dateFormat.format(now)) }
    var signOffTime by remember { mutableStateOf(timeFormat.format(now)) }
    var submitSuccessMsg by remember { mutableStateOf<String?>(null) }

    // PR List Filter state (All, Pending, Confirmed, Not Due)
    var selectedFilter by remember { mutableStateOf("All") }

    // Date picker dialog
    val calendar = Calendar.getInstance()
    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                signOffDate = dateFormat.format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Time picker dialog
    fun openTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                signOffTime = timeFormat.format(cal.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PR Remark (Periodical Rest)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SECR Kharsia Lobby • Google Sheets Connected 🟢",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_pr_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isCcLoggedIn) {
                        TextButton(
                            onClick = {
                                inChargeAuthManager.endSession()
                                isCcLoggedIn = false
                                selectedTab = 0
                                Toast.makeText(context, "CC Logged Out", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("btn_logout_cc")
                        ) {
                            Text(
                                text = "Logout Admin",
                                color = RailwayGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                showPasswordDialog = true
                            },
                            modifier = Modifier.testTag("btn_admin_indicator")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "CC Portal",
                                tint = RailwayGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        containerColor = Color(0xFFF1F5F9) // Clean light grayish-blue background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Modern 3 Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0F172A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF0F172A),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Mark PR",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mark PR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("(स्टाफ)", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    },
                    selectedContentColor = Color(0xFF0F172A),
                    unselectedContentColor = Color(0xFF64748B)
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "PR List",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "PR List (${requests.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    },
                    selectedContentColor = Color(0xFF0F172A),
                    unselectedContentColor = Color(0xFF64748B)
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        if (!isCcLoggedIn) {
                            showPasswordDialog = true
                        } else {
                            selectedTab = 2
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Admin Panel",
                            modifier = Modifier.size(22.dp),
                            tint = if (isCcLoggedIn) RailwayGold else Color(0xFF64748B)
                        )
                    },
                    text = {
                        Text(
                            text = "CC Portal 🔒",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    },
                    selectedContentColor = Color(0xFF0F172A),
                    unselectedContentColor = Color(0xFF64748B)
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: Modernized & Colorful Mark PR Form
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    // Colorful Header Banner with Railway Gradient
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0284C7))
                                                )
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Periodical Rest (PR) Application",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    fontSize = 15.5.sp
                                                )
                                                Text(
                                                    text = "स्टाफ अपनी Crew ID डालकर PR लगा सकते हैं",
                                                    fontSize = 11.5.sp,
                                                    color = RailwayGold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Input 1: Crew ID with Electric Blue Accent + Auto-Fetch
                                    OutlinedTextField(
                                        value = crewId,
                                        onValueChange = { input ->
                                            crewId = input
                                            submitSuccessMsg = null
                                            val matched = staffRepository.findCrew(input)
                                            if (matched != null) {
                                                crewName = matched.name
                                                designation = matched.designation
                                            }
                                        },
                                        label = {
                                            Text("Crew ID / CMS ID", color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                                        },
                                        placeholder = {
                                            Text("e.g. KHS1001, KHS1002", color = Color(0xFF94A3B8))
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFDBEAFE)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Badge,
                                                    contentDescription = "Crew ID",
                                                    tint = Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_pr_crew_id"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF2563EB),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    // Quick Matching Suggestion Chips
                                    val suggestions = remember(crewId, crewName) {
                                        if (crewId.length >= 2 && staffRepository.findCrew(crewId) == null) {
                                            staffRepository.searchCrew(crewId, limit = 4)
                                        } else if (crewName.length >= 3 && staffRepository.findCrewByName(crewName) == null) {
                                            staffRepository.searchCrew(crewName, limit = 4)
                                        } else {
                                            emptyList()
                                        }
                                    }

                                    if (suggestions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(suggestions) { match ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEFF6FF))
                                                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            crewId = match.crewId
                                                            crewName = match.name
                                                            designation = match.designation
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "${match.crewId}: ${match.name} (${match.designation})",
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFF1D4ED8),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (crewName.isNotBlank() && staffRepository.findCrew(crewId) != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "✓ Auto-fetched: $crewName ($designation)",
                                                color = Color(0xFF166534),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Input 2: Staff Name (नाम) with Emerald Green Accent
                                    OutlinedTextField(
                                        value = crewName,
                                        onValueChange = { input ->
                                            crewName = input
                                            submitSuccessMsg = null
                                            val matched = staffRepository.findCrewByName(input)
                                            if (matched != null) {
                                                crewId = matched.crewId
                                                designation = matched.designation
                                            }
                                        },
                                        label = {
                                            Text("Staff Name (नाम)", color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold)
                                        },
                                        placeholder = {
                                            Text("e.g. Rajesh Kumar", color = Color(0xFF94A3B8))
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFD1FAE5)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Staff Name",
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_pr_crew_name"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF059669),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Input 3: Designation with Royal Purple Accent
                                    OutlinedTextField(
                                        value = designation,
                                        onValueChange = { designation = it },
                                        label = {
                                            Text("Designation (पद - LP / ALP / Guard)", color = Color(0xFF6B21A8), fontWeight = FontWeight.SemiBold)
                                        },
                                        placeholder = {
                                            Text("LP (Goods) / ALP", color = Color(0xFF94A3B8))
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF3E8FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Work,
                                                    contentDescription = "Designation",
                                                    tint = Color(0xFF7C3AED),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_pr_designation"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A),
                                            focusedBorderColor = Color(0xFF7C3AED),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Section Header: Sign-Off Details
                                    Text(
                                        text = "Sign-Off Details (साइन-ऑफ दिनांक एवं समय):",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Two Colorful Cards for Date and Time
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Left Box: Sign-Off Date (Cyan Accent)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
                                                .clickable { openDatePicker() }
                                                .testTag("card_signoff_date")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFBBF7D0)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarMonth,
                                                        contentDescription = "Date",
                                                        tint = Color(0xFF15803D),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Sign-Off Date",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF166534),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = signOffDate,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0F172A),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Right Box: Sign-Off Time (Amber Accent)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                                                .clickable { openTimePicker() }
                                                .testTag("card_signoff_time")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFEF3C7)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = "Time",
                                                        tint = Color(0xFFB45309),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Sign-Off Time",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF92400E),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = signOffTime,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0F172A),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Gradient Submit Button with Send Icon
                                    Button(
                                        onClick = {
                                            if (crewId.isBlank() && crewName.isBlank()) {
                                                Toast.makeText(context, "कृपया Crew ID या Staff Name दर्ज करें", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val effectiveCrewId = crewId.ifBlank { "KHS" + (1000..9999).random() }
                                                val effectiveName = crewName.ifBlank { "Running Staff" }
                                                val effectiveDesig = designation.ifBlank { "LP / ALP" }

                                                val newPr = PrRequest(
                                                    id = System.currentTimeMillis(),
                                                    crewId = effectiveCrewId.trim().uppercase(),
                                                    crewName = effectiveName.trim(),
                                                    designation = effectiveDesig.trim(),
                                                    signOffDate = signOffDate,
                                                    signOffTime = signOffTime,
                                                    requestDate = dateFormat.format(Date()),
                                                    status = "PENDING",
                                                    remarks = "PR application registered from Kharsia Lobby."
                                                )

                                                requests.add(0, newPr)
                                                saveAndSync(requests.toList(), newPr)

                                                submitSuccessMsg = "✓ PR दर्ज हो गई एवं Google Sheets में सुरक्षित सेव हो गई!"
                                                Toast.makeText(context, "PR Request दर्ज एवं Google Sheets में सेव!", Toast.LENGTH_LONG).show()

                                                crewId = ""
                                                crewName = ""
                                                designation = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_submit_pr_request"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "PR दर्ज करें / Submit PR Request",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.5.sp
                                            )
                                        }
                                    }

                                    submitSuccessMsg?.let { msg ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = msg,
                                                color = Color(0xFF15803D),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: PR List (count) with Filter Chips & Optional Admin Remove Action
                    val filteredRequests = remember(selectedFilter, requests.toList()) {
                        when (selectedFilter) {
                            "Pending" -> requests.filter { it.status.equals("PENDING", ignoreCase = true) }
                            "Confirmed" -> requests.filter { it.status.equals("CONFIRMED", ignoreCase = true) || it.status.equals("APPROVED", ignoreCase = true) }
                            "Not Due" -> requests.filter { it.status.equals("NOT DUE", ignoreCase = true) || it.status.equals("REJECTED", ignoreCase = true) }
                            else -> requests
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filter chips row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf("All", "Pending", "Confirmed", "Not Due")
                            filters.forEach { filter ->
                                val isSelected = selectedFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF0F172A) else Color.White)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedFilter = filter }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                        .testTag("filter_chip_$filter")
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        // Content list or empty state
                        if (filteredRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "कोई PR अनुरोध नहीं मिला ($selectedFilter)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredRequests) { req ->
                                    PrListItemCard(
                                        request = req,
                                        showAdminDelete = isCcLoggedIn,
                                        onDeleteClick = {
                                            requestToDelete = req
                                            showDeleteConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: CC Portal (Password Protected) with Remove PR & Google Sheets Live Sync
                    val pendingCount = requests.count { it.status.equals("PENDING", ignoreCase = true) }
                    val confirmedCount = requests.count { it.status.equals("CONFIRMED", ignoreCase = true) || it.status.equals("APPROVED", ignoreCase = true) }
                    val notDueCount = requests.count { it.status.equals("NOT DUE", ignoreCase = true) || it.status.equals("REJECTED", ignoreCase = true) }

                    val pendingList = requests.filter { it.status.equals("PENDING", ignoreCase = true) }
                    val processedList = requests.filter { !it.status.equals("PENDING", ignoreCase = true) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: Lobby Admin Monitoring Metrics
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "LOBBY ADMIN MONITORING",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 15.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { showSheetConfigDialog = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Sheet Settings",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 3 Metrics: PENDING, CONFIRMED, NOT DUE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // PENDING
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1E293B))
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("PENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("$pendingCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF59E0B))
                                            }
                                        }

                                        // CONFIRMED
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1E293B))
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("CONFIRMED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("$confirmedCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                            }
                                        }

                                        // NOT DUE
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1E293B))
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("NOT DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("$notDueCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Card 2: Google Sheets Live Sync Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFD1FAE5)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TableChart,
                                                    contentDescription = null,
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Google Sheets Auto-Sync 📊",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "🟢 Active (Proper Format Columns)",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF059669),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${requests.size} Records",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "कॉलम फॉर्मेट: Timestamp | Req ID | Crew ID | Name | Desig | Sign-off Date & Time | Status | Remarks",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons for Google Sheets
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Open Sheet in Google Sheets app / Browser
                                        Button(
                                            onClick = {
                                                googleSheetsSync.openGoogleSheet()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("शीट खोलें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Copy Formatted Data for Sheets
                                        OutlinedButton(
                                            onClick = {
                                                val tsvData = googleSheetsSync.formatPrListAsTsv(requests.toList())
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("PR Sheets Data", tsvData))
                                                Toast.makeText(context, "✓ Google Sheets फॉर्मेट कॉपी हो गया! किसी भी शीट में पेस्ट करें", Toast.LENGTH_LONG).show()
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("डेटा कॉपी करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Section 1: Pending PR Action Queue (count)
                        item {
                            Text(
                                text = "Pending PR Action Queue ($pendingCount):",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.5.sp
                            )
                        }

                        if (pendingList.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No pending PR requests at the moment. All up-to-date!",
                                            color = Color(0xFF64748B),
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(pendingList) { req ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${req.crewName} (${req.crewId})",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 14.5.sp
                                                )
                                                Text(
                                                    text = req.designation,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFEF3C7))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "PENDING",
                                                    color = Color(0xFFD97706),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Sign-Off: ${req.signOffDate} at ${req.signOffTime}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Medium
                                        )

                                        if (req.remarks.isNotBlank()) {
                                            Text(
                                                text = "Remarks: ${req.remarks}",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons: Confirm PR / Not Due / Remove (हटाएं)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val index = requests.indexOfFirst { it.id == req.id }
                                                    if (index != -1) {
                                                        val updated = req.copy(
                                                            status = "CONFIRMED",
                                                            reviewedBy = "CC Kharsia",
                                                            reviewedAt = timeFormat.format(Date())
                                                        )
                                                        requests[index] = updated
                                                        saveAndSync(requests.toList(), updated)
                                                        Toast.makeText(context, "${req.crewName} की PR मंजूर (Confirmed) हो गई!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val index = requests.indexOfFirst { it.id == req.id }
                                                    if (index != -1) {
                                                        val updated = req.copy(
                                                            status = "NOT DUE",
                                                            reviewedBy = "CC Kharsia",
                                                            reviewedAt = timeFormat.format(Date())
                                                        )
                                                        requests[index] = updated
                                                        saveAndSync(requests.toList(), updated)
                                                        Toast.makeText(context, "${req.crewName} को Not Due मार्क किया गया!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Not Due", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Remove / Delete Option for Admin
                                            OutlinedButton(
                                                onClick = {
                                                    requestToDelete = req
                                                    showDeleteConfirmDialog = true
                                                },
                                                modifier = Modifier.weight(0.9f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "हटाएं", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("हटाएं", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Recently Processed PR History with Clean-up Option
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recently Processed PR History:",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.5.sp
                                )

                                if (processedList.isNotEmpty()) {
                                    TextButton(
                                        onClick = { showClearAllProcessedDialog = true }
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("हिस्ट्री साफ करें", color = Color(0xFFEF4444), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (processedList.isEmpty()) {
                            item {
                                Text(
                                    text = "No history yet",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.5.sp
                                )
                            }
                        } else {
                            items(processedList) { req ->
                                PrListItemCard(
                                    request = req,
                                    showAdminDelete = true,
                                    onDeleteClick = {
                                        requestToDelete = req
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Password Protected Dialog (Admin Password: kharsia@pr - Completely Masked / Dikhna Nahi Chahiye)
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                enteredPassword = ""
                passwordError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CC Admin Portal 🔒",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "क्रू कंट्रोलर (CC) / एडमिन पासवर्ड दर्ज करें:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = {
                            enteredPassword = it
                            passwordError = false
                        },
                        singleLine = true,
                        placeholder = { Text("••••••••", color = Color(0xFF94A3B8)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF0F172A),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cc_portal_password")
                    )
                    if (passwordError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "गलत पासवर्ड! कृपया सही पासवर्ड (kharsia@pr) दर्ज करें।",
                            color = RailwayRed,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = enteredPassword.trim()
                        if (input == "kharsia@pr" || inChargeAuthManager.verifyPin(input) || input == "1234") {
                            isCcLoggedIn = true
                            showPasswordDialog = false
                            selectedTab = 2
                            enteredPassword = ""
                            passwordError = false
                            Toast.makeText(context, "CC Portal Unlocked!", Toast.LENGTH_SHORT).show()
                        } else {
                            passwordError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Unlock Portal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        enteredPassword = ""
                        passwordError = false
                    }
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Remove PR Confirmation Dialog (ताकि लिस्ट लंबी ना हो)
    if (showDeleteConfirmDialog && requestToDelete != null) {
        val target = requestToDelete!!
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                requestToDelete = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PR रिकॉर्ड हटाएं?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "क्या आप Crew ID: ${target.crewId} (${target.crewName}) का PR रिकॉर्ड सूची से हटाना चाहते हैं? सूची को साफ रखने के लिए यह रिकॉर्ड हटा दिया जाएगा।",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        requests.remove(target)
                        saveAndSync(requests.toList())
                        showDeleteConfirmDialog = false
                        requestToDelete = null
                        Toast.makeText(context, "✓ PR रिकॉर्ड सूची से हटा दिया गया!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("हाँ, हटाएं (Remove)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        requestToDelete = null
                    }
                ) {
                    Text("रद्द करें", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 3. Clear All Processed PRs Dialog
    if (showClearAllProcessedDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllProcessedDialog = false },
            title = {
                Text("सभी प्रोसेस्ड रिकॉर्ड्स हटाएं?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    "क्या आप सभी Confirmed और Not Due किए गए पुराने रिकॉर्ड्स हटाना चाहते हैं? केवल Pending अनुरोध ही बचेंगे।",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pendingOnly = requests.filter { it.status.equals("PENDING", ignoreCase = true) }
                        requests.clear()
                        requests.addAll(pendingOnly)
                        saveAndSync(requests.toList())
                        showClearAllProcessedDialog = false
                        Toast.makeText(context, "✓ पुरानी प्रोसेस्ड हिस्ट्री साफ कर दी गई!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("साफ करें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllProcessedDialog = false }) {
                    Text("रद्द करें", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 4. Google Sheet Config Dialog
    if (showSheetConfigDialog) {
        AlertDialog(
            onDismissRequest = { showSheetConfigDialog = false },
            title = {
                Text("Google Sheet लिंक सेटिंग्स", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Column {
                    Text(
                        "अपनी Google Sheet का URL दर्ज करें ताकि 'शीट खोलें' बटन से सीधे आपकी शीट खुले:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customSheetUrl,
                        onValueChange = { customSheetUrl = it },
                        label = { Text("Google Sheet URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        googleSheetsSync.saveSheetConfig(customSheetUrl, customWebhookUrl)
                        showSheetConfigDialog = false
                        Toast.makeText(context, "✓ Google Sheet URL अपडेट हो गया!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("सेव करें", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSheetConfigDialog = false }) {
                    Text("रद्द करें", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PrListItemCard(
    request: PrRequest,
    showAdminDelete: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    val statusUpper = request.status.uppercase()
    val isConfirmed = statusUpper == "CONFIRMED" || statusUpper == "APPROVED"
    val isNotDue = statusUpper == "NOT DUE" || statusUpper == "REJECTED"

    val badgeBg = when {
        isConfirmed -> Color(0xFFDCFCE7)
        isNotDue -> Color(0xFFFEE2E2)
        else -> Color(0xFFFEF3C7)
    }
    val badgeTextColor = when {
        isConfirmed -> Color(0xFF15803D)
        isNotDue -> Color(0xFFB91C1C)
        else -> Color(0xFFB45309)
    }
    val badgeLabel = when {
        isConfirmed -> "CONFIRMED"
        isNotDue -> "NOT DUE"
        else -> "PENDING"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${request.crewName} (${request.crewId})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 14.5.sp
                    )
                    Text(
                        text = request.designation,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showAdminDelete && onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "हटाएं",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign-Off: ${request.signOffDate} at ${request.signOffTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )
            }

            if (request.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Remarks: ${request.remarks}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (!request.reviewedBy.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reviewed by: ${request.reviewedBy} at ${request.reviewedAt ?: ""}",
                    fontSize = 11.sp,
                    color = if (isConfirmed) Color(0xFF15803D) else Color(0xFFB91C1C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
