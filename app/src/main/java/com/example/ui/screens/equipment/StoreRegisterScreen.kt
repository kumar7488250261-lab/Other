package com.example.ui.screens.equipment

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.equipment.StoreIssueRecord
import com.example.ui.components.CrewAutoFetchPicker
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayAmber
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreRegisterScreen(
    inChargeAuthManager: InChargeAuthManager,
    staffRepository: StaffRepository,
    onBack: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var selectedTab by remember { mutableStateOf(0) } // 0: Issue, 1: Return, 2: Records
    var isAdminLoggedIn by remember { mutableStateOf(inChargeAuthManager.isSessionValid) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val records = remember {
        mutableStateListOf(
            StoreIssueRecord(
                id = 1,
                equipmentName = "VHF Walkie Talkie 5W",
                serialNumber = "WT-SECR-4091",
                crewId = "KHS1042",
                crewName = "Rajesh Kumar",
                designation = "LP (Goods)",
                trainNo = "BOXN/KHS",
                issueTime = "06:15",
                status = "ISSUED"
            ),
            StoreIssueRecord(
                id = 2,
                equipmentName = "Tricolor LED HS Lamp",
                serialNumber = "TL-8842",
                crewId = "KHS1105",
                crewName = "Amit Verma",
                designation = "ALP",
                trainNo = "BCN/RIG",
                issueTime = "07:30",
                returnTime = "15:45",
                status = "RETURNED"
            )
        )
    }

    // Issue form
    var eqName by remember { mutableStateOf("VHF Walkie Talkie 5W") }
    var serialNo by remember { mutableStateOf("") }
    var crewId by remember { mutableStateOf("") }
    var crewName by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("LP (Goods)") }
    var trainNo by remember { mutableStateOf("") }
    var issueMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Store & Equipment Register",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Kharsia Lobby • CHO Equipment Fast Issue & Return",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_store_back")
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
                    text = { Text("Fast Issue", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 0) RailwayGold else Color.White) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fast Return", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 1) RailwayGold else Color.White) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        if (!isAdminLoggedIn) {
                            showPinDialog = true
                        } else {
                            selectedTab = 2
                        }
                    },
                    text = { Text("Store Records", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 2) RailwayGold else Color.White) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Fast Issue Screen
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
                                        text = "Equipment Fast Issue Entry",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = eqName,
                                        onValueChange = { eqName = it },
                                        label = { Text("Equipment Name (e.g. Walkie Talkie / Torch)", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.fillMaxWidth().testTag("input_eq_name"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RailwayGold,
                                            unfocusedBorderColor = DarkBorderBlue
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = serialNo,
                                        onValueChange = { serialNo = it },
                                        label = { Text("Equipment Serial / Badge No.", color = Color(0xFFA0B4D0)) },
                                        modifier = Modifier.fillMaxWidth().testTag("input_eq_serial"),
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
                                            modifier = Modifier.weight(1f).testTag("input_eq_crew_id"),
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
                                            modifier = Modifier.weight(1f).testTag("input_eq_crew_name"),
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
                                    val eqCrewSuggestions = remember(crewId, crewName) {
                                        if (crewId.length >= 2 && staffRepository.findCrew(crewId) == null) {
                                            staffRepository.searchCrew(crewId, limit = 4)
                                        } else if (crewName.length >= 3 && staffRepository.findCrewByName(crewName) == null) {
                                            staffRepository.searchCrew(crewName, limit = 4)
                                        } else {
                                            emptyList()
                                        }
                                    }

                                    if (eqCrewSuggestions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(eqCrewSuggestions) { match ->
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
                                            modifier = Modifier.weight(1f).testTag("input_eq_designation"),
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
                                            label = { Text("Train / Loco No.", color = Color(0xFFA0B4D0)) },
                                            modifier = Modifier.weight(1f).testTag("input_eq_train"),
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
                                            if (serialNo.isNotBlank() && crewName.isNotBlank()) {
                                                records.add(
                                                    0,
                                                    StoreIssueRecord(
                                                        id = System.currentTimeMillis(),
                                                        equipmentName = eqName.trim(),
                                                        serialNumber = serialNo.trim(),
                                                        crewId = crewId.trim(),
                                                        crewName = crewName.trim(),
                                                        designation = designation.trim(),
                                                        trainNo = trainNo.trim(),
                                                        issueTime = timeFormat.format(Date()),
                                                        status = "ISSUED"
                                                    )
                                                )
                                                issueMessage = "Equipment issued to $crewName ($trainNo)"
                                                serialNo = ""
                                                crewId = ""
                                                crewName = ""
                                                trainNo = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_issue_equipment"),
                                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Confirm Fast Issue (जारी करें)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F1E36)
                                        )
                                    }

                                    if (issueMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = issueMessage ?: "",
                                            color = RailwayGreen,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Fast Return Screen
                    var returnSearchQuery by remember { mutableStateOf("") }
                    val activeIssues = records.filter {
                        it.status == "ISSUED" && (
                            returnSearchQuery.isBlank() ||
                            it.crewName.contains(returnSearchQuery, ignoreCase = true) ||
                            it.crewId.contains(returnSearchQuery, ignoreCase = true) ||
                            it.equipmentName.contains(returnSearchQuery, ignoreCase = true) ||
                            it.serialNumber.contains(returnSearchQuery, ignoreCase = true)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Active Issued Items (${activeIssues.size} Pending Return)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = returnSearchQuery,
                                onValueChange = { returnSearchQuery = it },
                                placeholder = { Text("Search by Crew ID (e.g. KHS1042), Name, or Serial No...", color = Color(0xFFA0B4D0)) },
                                modifier = Modifier.fillMaxWidth().testTag("input_return_search"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold,
                                    unfocusedBorderColor = DarkBorderBlue
                                )
                            )
                        }


                        if (activeIssues.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No equipment currently issued. All items returned to store.",
                                        color = Color(0xFFA0B4D0),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        items(activeIssues) { record ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.equipmentName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "S/N: ${record.serialNumber} • Train: ${record.trainNo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RailwayGold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "With: ${record.crewName} (${record.designation}) at ${record.issueTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA0B4D0)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val index = records.indexOfFirst { it.id == record.id }
                                            if (index != -1) {
                                                records[index] = record.copy(
                                                    status = "RETURNED",
                                                    returnTime = timeFormat.format(Date())
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_return_${record.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Return")
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Store Records / Admin View
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
                                    text = "All Equipment Transactions",
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
                            val isReturned = record.status == "RETURNED"
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = record.equipmentName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isReturned) RailwayGreen.copy(alpha = 0.2f) else RailwayAmber.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = record.status,
                                                color = if (isReturned) RailwayGreen else RailwayAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "S/N: ${record.serialNumber} • Crew: ${record.crewName} (${record.crewId})",
                                        color = Color(0xFFA0B4D0),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Issued: ${record.issueTime}${if (record.returnTime != null) " • Returned: ${record.returnTime}" else ""}",
                                        color = RailwayGold,
                                        fontSize = 12.sp
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
            title = { Text("Store In-Charge PIN", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter Store In-Charge PIN (Default: 1234):", color = Color(0xFFA0B4D0))
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
                        modifier = Modifier.fillMaxWidth().testTag("input_store_pin")
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
                            selectedTab = 2
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
