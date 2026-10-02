package com.example.ui.screens.longhour

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffRepository
import com.example.data.equipment.InChargeAuthManager
import com.example.data.equipment.LongHourDutyRecord
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.firebase.SyncState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.CrewAutoFetchPicker
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayAmber
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayNavy
import com.example.ui.theme.RailwayRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongHourUpdateScreen(
    inChargeAuthManager: InChargeAuthManager,
    staffRepository: StaffRepository,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val context = LocalContext.current
    val syncManager = remember { FirebaseSyncManager.getInstance(context) }
    val syncState by syncManager.syncState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Duty Entry, 1: Monitor / Relieve
    var isAdminLoggedIn by remember { mutableStateOf(inChargeAuthManager.isSessionValid) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val records = remember {
        mutableStateListOf(
            LongHourDutyRecord(
                id = 1,
                crewId = "KHS1042",
                crewName = "Rajesh Kumar",
                designation = "LP (Goods)",
                trainNo = "BOXN/KHS-RIG",
                section = "KHS - RIG",
                signOnDate = dateFormat.format(Date()),
                signOnTime = "04:30",
                dutyHours = 9.5,
                status = "LONG_HOUR"
            ),
            LongHourDutyRecord(
                id = 2,
                crewId = "KHS1105",
                crewName = "Amit Verma",
                designation = "ALP",
                trainNo = "BCN/RIG-BSP",
                section = "RIG - BSP",
                signOnDate = dateFormat.format(Date()),
                signOnTime = "06:00",
                dutyHours = 7.0,
                status = "ON_DUTY"
            )
        )
    }

    DisposableEffect(Unit) {
        val listener = syncManager.listenDuties { liveList ->
            if (liveList.isNotEmpty()) {
                records.clear()
                records.addAll(liveList)
            }
        }
        onDispose {
            listener?.remove()
        }
    }

    var crewId by remember { mutableStateOf("") }
    var crewName by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("LP (Goods)") }
    var trainNo by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("KHS - RIG") }
    var signOnDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var signOnTime by remember { mutableStateOf(timeFormat.format(Date())) }
    var submitMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Long Hour Duty Update",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Kharsia Lobby • Crew 9+ Hours Monitoring & Relief",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_long_hour_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceNavy,
                contentColor = RailwayGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RailwayGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sign On Entry", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 0) RailwayGold else Color.White) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (!isAdminLoggedIn) {
                            showPinDialog = true
                        } else {
                            selectedTab = 1
                        }
                    },
                    text = { Text("Live Duty Monitoring", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 1) RailwayGold else Color.White) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorderBlue, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Crew Sign-On & Section Duty Entry",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = crewId,
                                        onValueChange = { input ->
                                            crewId = input
                                            val member = staffRepository.findCrew(input)
                                            if (member != null) {
                                                crewName = member.name
                                                designation = member.designation
                                            }
                                        },
                                        label = { Text("Crew ID", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f).testTag("input_lh_crew_id"),
                                        singleLine = true,
                                        placeholder = { Text("e.g. KHS1001", color = Color(0xFF6B7280)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = crewName,
                                        onValueChange = { input ->
                                            crewName = input
                                            val member = staffRepository.findCrewByName(input)
                                            if (member != null) {
                                                crewId = member.crewId
                                                designation = member.designation
                                            }
                                        },
                                        label = { Text("Crew Name", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f).testTag("input_lh_crew_name"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                }

                                // Quick Matching Suggestion Chips
                                val lhCrewSuggestions = remember(crewId, crewName) {
                                    if (crewId.length >= 2 && staffRepository.findCrew(crewId) == null) {
                                        staffRepository.searchCrew(crewId, limit = 4)
                                    } else if (crewName.length >= 3 && staffRepository.findCrewByName(crewName) == null) {
                                        staffRepository.searchCrew(crewName, limit = 4)
                                    } else {
                                        emptyList()
                                    }
                                }

                                if (lhCrewSuggestions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(lhCrewSuggestions) { match ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(RailwayGold.copy(alpha = 0.18f))
                                                    .border(1.dp, RailwayGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        crewId = match.crewId
                                                        crewName = match.name
                                                        designation = match.designation
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${match.crewId}: ${match.name} (${match.designation})",
                                                    fontSize = 11.sp,
                                                    color = RailwayGold,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                if (crewName.isNotBlank() && staffRepository.findCrew(crewId) != null) {
                                    Text(
                                        text = "✓ Auto-fetched: $crewName ($designation)",
                                        color = RailwayGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = designation,
                                        onValueChange = { designation = it },
                                        label = { Text("Designation", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f).testTag("input_lh_designation"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = trainNo,
                                        onValueChange = { trainNo = it },
                                        label = { Text("Train / Load No.", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f).testTag("input_lh_train"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                }



                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = section,
                                    onValueChange = { section = it },
                                    label = { Text("Section (e.g. KHS - RIG, KHS - CPH)", color = Color(0xFFA0B4D0)) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_lh_section"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = RailwayGold,
                                        unfocusedBorderColor = DarkBorderBlue
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = signOnDate,
                                        onValueChange = { signOnDate = it },
                                        label = { Text("Sign On Date", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = signOnTime,
                                        onValueChange = { signOnTime = it },
                                        label = { Text("Sign On Time", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                 Button(
                                    onClick = {
                                        if (crewId.isNotBlank() && crewName.isNotBlank()) {
                                            val newDuty = LongHourDutyRecord(
                                                id = System.currentTimeMillis(),
                                                crewId = crewId.trim(),
                                                crewName = crewName.trim(),
                                                designation = designation.trim(),
                                                trainNo = trainNo.trim(),
                                                section = section.trim(),
                                                signOnDate = signOnDate.trim(),
                                                signOnTime = signOnTime.trim(),
                                                dutyHours = 1.0,
                                                status = "ON_DUTY"
                                            )
                                            records.add(0, newDuty)
                                            syncManager.syncDuty(newDuty)
                                            submitMsg = "Sign-on duty recorded for $crewName ($trainNo) • Synced to Cloud"
                                            crewId = ""
                                            crewName = ""
                                            trainNo = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_submit_long_hour"),
                                    colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Record Duty Sign-On (साइन-ऑन दर्ज करें)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F1E36)
                                    )
                                }

                                if (submitMsg != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = submitMsg ?: "",
                                        color = RailwayGreen,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Monitor / Relieve
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Crew Working on Section",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            OutlinedButton(
                                onClick = {
                                    inChargeAuthManager.endSession()
                                    isAdminLoggedIn = false
                                    selectedTab = 0
                                }
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RailwayGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock Admin", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    items(records) { record ->
                        val isLongHour = record.status == "LONG_HOUR"
                        val isRelieved = record.status == "RELIEVED"
                        val badgeColor = when {
                            isRelieved -> RailwayGreen
                            isLongHour -> RailwayRed
                            else -> RailwayAmber
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${record.crewName} (${record.crewId})",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${record.designation} • Train: ${record.trainNo}",
                                            color = RailwayGold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isLongHour) "LONG HOUR (>9h)" else record.status,
                                            color = badgeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Section: ${record.section} • Sign On: ${record.signOnTime} (${record.signOnDate})",
                                    color = Color(0xFFA0B4D0),
                                    fontSize = 12.sp
                                )

                                if (!isRelieved) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val idx = records.indexOfFirst { it.id == record.id }
                                            if (idx != -1) {
                                                val relieved = record.copy(
                                                    status = "RELIEVED",
                                                    reliefStation = "KHS / Relieved by TLC"
                                                )
                                                records[idx] = relieved
                                                syncManager.syncDuty(relieved)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_relieve_${record.id}")
                                    ) {
                                        Text("Relieve Duty (कार्यमुक्त करें)", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Relieved at ${record.reliefStation}",
                                        color = RailwayGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("TLC / Supervisor PIN", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter In-Charge PIN (Default: 1234):", color = Color(0xFFA0B4D0))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold,
                            unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_long_hour_pin")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Invalid PIN. Use 1234", color = RailwayRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inChargeAuthManager.verifyPin(enteredPin)) {
                            isAdminLoggedIn = true
                            showPinDialog = false
                            selectedTab = 1
                            enteredPin = ""
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailwayGold)
                ) {
                    Text("Unlock", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = DarkSurfaceNavy
        )
    }
}
