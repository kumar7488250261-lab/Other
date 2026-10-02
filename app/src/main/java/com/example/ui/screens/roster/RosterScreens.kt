package com.example.ui.screens.roster

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffRepository
import com.example.data.equipment.InChargeAuthManager
import com.example.data.equipment.RosterTlcRecord
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
fun RosterTlcSubMenuScreen(
    inChargeAuthManager: InChargeAuthManager,
    onNavigateToShiftRoster: () -> Unit,
    onNavigateToAdminPortal: () -> Unit,
    onBack: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shift Roster & TLC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("SECR Bilaspur Division • Shift Operations", style = MaterialTheme.typography.labelSmall, color = RailwayGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_roster_sub_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        containerColor = DarkCanvasBg
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp)
                    .border(1.2.dp, Color(0xFFA78BFA).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF581C87), Color(0xFF7C3AED))))
                    .clickable { onNavigateToShiftRoster() }
                    .testTag("menu_item_view_roster")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Column {
                        Text("Shift Roster", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                        Text("06-14, 14-22, 22-06", fontWeight = FontWeight.SemiBold, color = RailwayGold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text("Active duty shifts", color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp)
                    .border(1.2.dp, Color(0xFFF87171).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF881337), Color(0xFFE11D48))))
                    .clickable {
                        if (inChargeAuthManager.isSessionValid) {
                            onNavigateToAdminPortal()
                        } else {
                            showPinDialog = true
                        }
                    }
                    .testTag("menu_item_roster_admin")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Column {
                        Text("Roster Admin", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                        Text("TLC & Duty Assignment", fontWeight = FontWeight.SemiBold, color = RailwayGold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text("Supervisor portal", color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp)
                    }
                }
            }
        }

    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Supervisor PIN", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter Supervisor PIN to access Roster Admin (Default: 1234):", color = Color(0xFFA0B4D0))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it; pinError = false },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_roster_pin")
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
                            showPinDialog = false
                            enteredPin = ""
                            onNavigateToAdminPortal()
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
                OutlinedButton(onClick = { showPinDialog = false }) { Text("Cancel", color = Color.White) }
            },
            containerColor = DarkSurfaceNavy
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftWiseRosterScreen(
    onBack: () -> Unit
) {
    val shifts = listOf("ALL", "06:00 - 14:00", "14:00 - 22:00", "22:00 - 06:00")
    var selectedShift by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val records = listOf(
        RosterTlcRecord(1, "06:00 - 14:00", "Today", "Lobby Supervisor", "EMP-201", "S. K. Sharma", "CLI", "9752876001", "Bilaspur Goods TLC", "9752876100"),
        RosterTlcRecord(2, "06:00 - 14:00", "Today", "CC (Crew Controller)", "EMP-304", "V. K. Singh", "CC/KHS", "9752876015", "Bilaspur Goods TLC", "9752876100"),
        RosterTlcRecord(3, "14:00 - 22:00", "Today", "Lobby Supervisor", "EMP-205", "R. P. Mishra", "CLI", "9752876002", "Bilaspur Passenger TLC", "9752876101"),
        RosterTlcRecord(4, "22:00 - 06:00", "Today", "Lobby Supervisor", "EMP-208", "M. K. Verma", "CLI", "9752876003", "Bilaspur Night TLC", "9752876102")
    )

    val filteredRecords = records.filter {
        (selectedShift == "ALL" || it.shift == selectedShift) &&
                (it.staffName.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) || it.tlcName.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shift-Wise Duty Roster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Kharsia Lobby 3-Shift Roster & TLC In-Charge", style = MaterialTheme.typography.labelSmall, color = RailwayGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_roster_view_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        containerColor = DarkCanvasBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Staff, Role, or TLC...", color = Color(0xFF7E8EA6)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RailwayGold) },
                modifier = Modifier.fillMaxWidth().testTag("input_search_roster"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shifts) { shift ->
                    val isSelected = selectedShift == shift
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                            .border(1.dp, if (isSelected) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                            .clickable { selectedShift = shift }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = shift,
                            color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredRecords) { rec ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(rec.role, fontWeight = FontWeight.Bold, color = Color.White)
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(RailwayNavy).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(rec.shift, color = RailwayGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${rec.staffName} (${rec.designation}) • Ph: ${rec.mobile}", color = Color(0xFFA0B4D0), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("TLC In-Charge: ${rec.tlcName} • ${rec.tlcMobile}", color = RailwayAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterAdminPortalScreen(
    staffRepository: StaffRepository,
    onBack: () -> Unit
) {
    var shift by remember { mutableStateOf("06:00 - 14:00") }
    var role by remember { mutableStateOf("Lobby Supervisor") }
    var staffName by remember { mutableStateOf("") }
    var staffMobile by remember { mutableStateOf("") }
    var tlcName by remember { mutableStateOf("Bilaspur Goods TLC") }
    var tlcMobile by remember { mutableStateOf("9752876100") }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Roster Admin Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Assign Shift In-Charges & Bilaspur TLC", style = MaterialTheme.typography.labelSmall, color = RailwayGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_roster_admin_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        containerColor = DarkCanvasBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Roster Duty Assignment", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = shift,
                            onValueChange = { shift = it },
                            label = { Text("Shift Timing (e.g. 06:00 - 14:00)", color = Color(0xFFA0B4D0)) },
                            modifier = Modifier.fillMaxWidth().testTag("input_roster_shift"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = role,
                            onValueChange = { role = it },
                            label = { Text("Designated Role", color = Color(0xFFA0B4D0)) },
                            modifier = Modifier.fillMaxWidth().testTag("input_roster_role"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = staffName,
                                onValueChange = { input ->
                                    staffName = input
                                    val member = staffRepository.findCrew(input)
                                    if (member != null) {
                                        staffName = member.name
                                        if (member.mobile.isNotBlank()) staffMobile = member.mobile
                                        if (member.designation.isNotBlank()) role = member.designation
                                    }
                                },
                                label = { Text("Staff Name / Crew ID", color = Color(0xFFA0B4D0)) },
                                placeholder = { Text("e.g. KHS1001 or Name", color = Color(0xFF6B7280)) },
                                modifier = Modifier.weight(1f).testTag("input_roster_staff"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = staffMobile,
                                onValueChange = { staffMobile = it },
                                label = { Text("Mobile / CUG", color = Color(0xFFA0B4D0)) },
                                modifier = Modifier.weight(1f).testTag("input_roster_mobile"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                )
                            )
                        }

                        if (staffName.isNotBlank() && staffMobile.isNotBlank()) {
                            Text(
                                text = "✓ Staff: $staffName • CUG: $staffMobile",
                                color = RailwayGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))


                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = tlcName,
                                onValueChange = { tlcName = it },
                                label = { Text("TLC Desk Name", color = Color(0xFFA0B4D0)) },
                                modifier = Modifier.weight(1f).testTag("input_roster_tlc_name"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = tlcMobile,
                                onValueChange = { tlcMobile = it },
                                label = { Text("TLC CUG / Phone", color = Color(0xFFA0B4D0)) },
                                modifier = Modifier.weight(1f).testTag("input_roster_tlc_mobile"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                savedMsg = "Roster updated for shift $shift ($staffName)"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_save_roster"),
                            colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Duty Assignment (सुरक्षित करें)", fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36))
                        }

                        if (savedMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(savedMsg ?: "", color = RailwayGreen, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
