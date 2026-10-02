package com.example.ui.screens.jeep

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffRepository
import com.example.data.jeep.JeepBreakdownRecord
import com.example.data.jeep.JeepDiDuty
import com.example.data.jeep.JeepDriver
import com.example.data.jeep.JeepRepository
import com.example.data.jeep.JeepTripRecord
import com.example.data.jeep.JeepVehicle
import com.example.data.jeep.StationCodeItem
import com.example.ui.components.KharsiaLobbyEmblem
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayNavy
import com.example.ui.theme.RailwayRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeepSubMenuScreen(
    jeepRepository: JeepRepository,
    staffRepository: StaffRepository,
    initialTab: Int = 0,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    // State holders
    var diDuty by remember { mutableStateOf(jeepRepository.getJeepDiDuty()) }
    val vehiclesState = remember { mutableStateListOf<JeepVehicle>() }
    val tripsState = remember { mutableStateListOf<JeepTripRecord>() }
    val breakdownsState = remember { mutableStateListOf<JeepBreakdownRecord>() }
    val driversState = remember { mutableStateListOf<JeepDriver>() }

    fun refreshAll() {
        diDuty = jeepRepository.getJeepDiDuty()
        vehiclesState.clear()
        vehiclesState.addAll(jeepRepository.getVehicles())
        tripsState.clear()
        tripsState.addAll(jeepRepository.getTrips())
        breakdownsState.clear()
        breakdownsState.addAll(jeepRepository.getBreakdowns())
        driversState.clear()
        driversState.addAll(jeepRepository.getDrivers())
    }

    remember {
        refreshAll()
        true
    }

    // Dialog state for changing Jeep DI
    var showChangeDiDialog by remember { mutableStateOf(false) }

    // Dialog state for quick Arrival recording from FIFO queue
    var showArrivalDialogForTrip by remember { mutableStateOf<JeepTripRecord?>(null) }
    var showQuickVehicleArrivalDialog by remember { mutableStateOf(false) }
    var selectedVehicleForArrival by remember { mutableStateOf("") }

    // Dialog state for reporting breakdown
    var showReportBreakdownDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Lobby FIFO Queue",
        "Quick Dispatch",
        "Return & Arrival",
        "Breakdown Tracker",
        "Driver Directory",
        "Station Codes"
    )

    val availableFifoCount = vehiclesState.count { it.status == "AVAILABLE" }
    val onTripCount = vehiclesState.count { it.status == "ON_TRIP" }
    val breakdownCount = vehiclesState.count { it.status == "BREAKDOWN" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KharsiaLobbyEmblem(size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "JEEP DI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RailwayGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "KHARSIA LOBBY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F1E36)
                                    )
                                }
                            }
                            Text(
                                text = "Shift: ${diDuty.shift} • DI: ${diDuty.diName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RailwayGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_jeep_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showChangeDiDialog = true },
                        modifier = Modifier.testTag("btn_change_jeep_di")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change DI",
                            tint = RailwayGold
                        )
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
        ) {
            // =========================================================================
            // 1. DUTY JEEP DI BANNER & LIVE METRICS
            // =========================================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlue)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(RailwayGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Duty Jeep DI: ${diDuty.diName} (${diDuty.diCrewId})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E3A8A))
                                .clickable { showChangeDiDialog = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Shift: ${diDuty.shift} ✎",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Fleet Status Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FleetStatusChip(
                            title = "In Lobby (FIFO)",
                            count = availableFifoCount,
                            color = RailwayGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        FleetStatusChip(
                            title = "On Line Trip",
                            count = onTripCount,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        FleetStatusChip(
                            title = "Breakdown",
                            count = breakdownCount,
                            color = if (breakdownCount > 0) RailwayRed else Color.Gray,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 3 }
                        )
                    }
                }
            }

            // =========================================================================
            // 2. SCROLLABLE TAB NAVIGATION
            // =========================================================================
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackgroundNavy,
                contentColor = RailwayGold,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RailwayGold,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) RailwayGold else Color(0xFFA0B4D0)
                            )
                        }
                    )
                }
            }

            // =========================================================================
            // 3. TAB CONTENT
            // =========================================================================
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> JeepFifoQueueTab(
                        vehicles = vehiclesState,
                        activeTrips = tripsState.filter { it.status == "ON_TRIP" },
                        onQuickDispatchClick = {
                            selectedTab = 1
                        },
                        onRecordArrivalClick = { trip ->
                            showArrivalDialogForTrip = trip
                        },
                        onQuickVehicleArrivalClick = {
                            showQuickVehicleArrivalDialog = true
                        }
                    )

                    1 -> JeepQuickDispatchTab(
                        jeepRepository = jeepRepository,
                        staffRepository = staffRepository,
                        currentDuty = diDuty,
                        onDispatched = {
                            refreshAll()
                            selectedTab = 0
                            Toast.makeText(context, "Jeep Dispatched Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> JeepReturnArrivalTab(
                        jeepRepository = jeepRepository,
                        staffRepository = staffRepository,
                        activeTrips = tripsState.filter { it.status == "ON_TRIP" },
                        vehicles = vehiclesState,
                        onArrivalRecorded = {
                            refreshAll()
                            selectedTab = 0
                            Toast.makeText(context, "Jeep Arrival Recorded at Kharsia Lobby!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    3 -> JeepBreakdownTab(
                        jeepRepository = jeepRepository,
                        vehicles = vehiclesState,
                        breakdowns = breakdownsState,
                        onReportBreakdownClick = { showReportBreakdownDialog = true },
                        onResolved = {
                            refreshAll()
                            Toast.makeText(context, "Jeep Repaired & Restored to Fleet!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    4 -> JeepDriverDirectoryTab(
                        drivers = driversState,
                        onCallDriver = { phone ->
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    5 -> StationCodesDirectoryTab(
                        stations = jeepRepository.getStationCodes()
                    )
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: CHANGE JEEP DI & SHIFT
    // =========================================================================
    if (showChangeDiDialog) {
        ChangeJeepDiDialog(
            currentDuty = diDuty,
            staffRepository = staffRepository,
            onDismiss = { showChangeDiDialog = false },
            onSave = { updatedDuty ->
                jeepRepository.saveJeepDiDuty(updatedDuty)
                diDuty = updatedDuty
                showChangeDiDialog = false
                Toast.makeText(context, "Duty Jeep DI Updated: ${updatedDuty.diName}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // =========================================================================
    // DIALOG: RECORD ARRIVAL FOR ACTIVE TRIP
    // =========================================================================
    showArrivalDialogForTrip?.let { trip ->
        RecordTripArrivalDialog(
            trip = trip,
            staffRepository = staffRepository,
            onDismiss = { showArrivalDialogForTrip = null },
            onConfirm = { arrivalTime, retCrewId, retCrewName, retCrewDesig, retOtherCrew, retFromCode, retDepTime, remarks ->
                jeepRepository.recordArrival(
                    tripId = trip.id,
                    arrivalTime = arrivalTime,
                    returningCrewId = retCrewId,
                    returningCrewName = retCrewName,
                    returningCrewDesig = retCrewDesig,
                    returningOtherCrew = retOtherCrew,
                    returningFromStationCode = retFromCode,
                    returningDepartureTime = retDepTime,
                    remarks = remarks
                )
                refreshAll()
                showArrivalDialogForTrip = null
                Toast.makeText(context, "Jeep ${trip.vehicleNo} returned to Kharsia Lobby at $arrivalTime!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // =========================================================================
    // DIALOG: QUICK VEHICLE ARRIVAL (WHEN ANY JEEP ARRIVES AT LOBBY)
    // =========================================================================
    if (showQuickVehicleArrivalDialog) {
        QuickVehicleArrivalDialog(
            jeepRepository = jeepRepository,
            vehicles = vehiclesState,
            onDismiss = { showQuickVehicleArrivalDialog = false },
            onConfirm = { vNo, arrTime ->
                jeepRepository.quickVehicleArrival(vNo, arrTime)
                refreshAll()
                showQuickVehicleArrivalDialog = false
                Toast.makeText(context, "Jeep $vNo marked Available at Kharsia Lobby at $arrTime!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // =========================================================================
    // DIALOG: REPORT JEEP BREAKDOWN
    // =========================================================================
    if (showReportBreakdownDialog) {
        ReportBreakdownDialog(
            vehicles = vehiclesState,
            currentDiName = diDuty.diName,
            onDismiss = { showReportBreakdownDialog = false },
            onConfirm = { vNo, dName, sTime, sDate, reason, repBy ->
                jeepRepository.reportBreakdown(vNo, dName, sTime, sDate, reason, repBy)
                refreshAll()
                showReportBreakdownDialog = false
                Toast.makeText(context, "Breakdown reported for Jeep $vNo", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// =============================================================================
// COMPONENT: FLEET STATUS CHIP
// =============================================================================
@Composable
private fun FleetStatusChip(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color)
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (color == RailwayGold) Color(0xFF0F1E36) else Color.White
                )
            }
        }
    }
}

// =============================================================================
// TAB 0: FIFO QUEUE & LOBBY STATUS
// =============================================================================
@Composable
private fun JeepFifoQueueTab(
    vehicles: List<JeepVehicle>,
    activeTrips: List<JeepTripRecord>,
    onQuickDispatchClick: () -> Unit,
    onRecordArrivalClick: (JeepTripRecord) -> Unit,
    onQuickVehicleArrivalClick: () -> Unit
) {
    val availableFifo = vehicles.filter { it.status == "AVAILABLE" }.sortedBy { it.lobbyArrivalTime }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // FIFO Rule Announcement Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, RailwayGold, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RailwayGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = RailwayGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "नियमानुसार: जो जीप पहले आई है, वही पहले जाएगी",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = RailwayGold
                        )
                        Text(
                            text = "Kharsia Lobby Jeep FIFO Queue • Turnwise Dispatch",
                            fontSize = 10.5.sp,
                            color = Color(0xFFA0B4D0)
                        )
                    }
                    Button(
                        onClick = onQuickVehicleArrivalClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_record_vehicle_arrival")
                    ) {
                        Text("लॉबी आगमन +", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Section: Available in Lobby (FIFO Queue)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "लॉबी में उपलब्ध जीप कतार (FIFO Order - ${availableFifo.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Button(
                    onClick = onQuickDispatchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_dispatch_now")
                ) {
                    Text("रवाना करें (Dispatch)", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }

        if (availableFifo.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No Jeeps currently available at Kharsia Lobby", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("All vehicles are on trip or under breakdown", color = Color(0xFFA0B4D0), fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(availableFifo) { jeep ->
                val turnIndex = availableFifo.indexOf(jeep) + 1
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (turnIndex == 1) RailwayGold else DarkBorderBlue, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Turn / Queue Number
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (turnIndex == 1) RailwayGold else Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$turnIndex",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (turnIndex == 1) Color(0xFF0F1E36) else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Jeep No: ${jeep.vehicleNo}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (turnIndex == 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RailwayGreen)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("अगला नंबर (Next)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            Text(
                                text = "Driver: ${jeep.defaultDriverName} • Ph: ${jeep.defaultDriverMobile}",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA0B4D0)
                            )
                            Text(
                                text = "Lobby Arrival Time: ${jeep.lobbyArrivalTime} hrs",
                                fontSize = 11.sp,
                                color = RailwayGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onQuickDispatchClick,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RailwayGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Dispatch", fontSize = 11.sp, color = RailwayGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Active Trips On Line
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "वर्तमान में रास्ते में जीपें (On Line Trips - ${activeTrips.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF38BDF8)
            )
        }

        if (activeTrips.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "No vehicles currently on line trip",
                        modifier = Modifier.padding(14.dp),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(activeTrips) { trip ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A45)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Jeep ${trip.vehicleNo}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0284C7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ON TRIP", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Text(
                                text = "Dep: ${trip.departureTime}",
                                fontSize = 11.5.sp,
                                color = RailwayGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Route: ${trip.fromStationCode} (${trip.fromStationName}) ➔ ${trip.toStationCode} (${trip.toStationName})",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Driver: ${trip.driverName} • ${trip.driverMobile}",
                            fontSize = 11.sp,
                            color = Color(0xFFA0B4D0)
                        )
                        if (trip.outgoingCrewName.isNotBlank()) {
                            Text(
                                text = "Crew: ${trip.outgoingCrewName} (${trip.outgoingCrewId})",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onRecordArrivalClick(trip) },
                            colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("लॉबी वापसी दर्ज करें (Record Arrival)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: QUICK DISPATCH (रवाना करें)
// =============================================================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun JeepQuickDispatchTab(
    jeepRepository: JeepRepository,
    staffRepository: StaffRepository,
    currentDuty: JeepDiDuty,
    onDispatched: () -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    var selectedJeepNo by remember { mutableStateOf("89") }
    var customJeepNo by remember { mutableStateOf("") }
    var isCustomJeep by remember { mutableStateOf(false) }

    val allDrivers = remember { jeepRepository.getDrivers() }
    var selectedDriverName by remember { mutableStateOf(allDrivers.firstOrNull()?.name ?: "Rajendra Patel") }
    var driverMobile by remember { mutableStateOf(allDrivers.firstOrNull()?.mobile ?: "6265395782") }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var isOtherDriver by remember { mutableStateOf(false) }
    var otherDriverName by remember { mutableStateOf("") }

    val allStations = remember { jeepRepository.getStationCodes() }
    var selectedToCode by remember { mutableStateOf("ROB") }
    var selectedToName by remember { mutableStateOf("Robertson") }
    var stationSearchQuery by remember { mutableStateOf("") }
    var showStationPicker by remember { mutableStateOf(false) }

    var departureTimeInput by remember { mutableStateOf(currentTime) }

    // Outgoing Crew
    var outgoingCrewId by remember { mutableStateOf("") }
    var outgoingCrewName by remember { mutableStateOf("") }
    var outgoingCrewDesig by remember { mutableStateOf("") }
    var outgoingOtherCrew by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Jeep Quick Departure (गाड़ी रवानगी)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.5.sp
                    )
                    Text(
                        text = "From: KHS (Kharsia Lobby) • Shift: ${currentDuty.shift}",
                        color = RailwayGold,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. SELECT JEEP NUMBER CHIPS
                    Text("Select Jeep Number (जीप नंबर चुनें):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        jeepRepository.standardJeepNumbers.forEach { jNo ->
                            val isSel = !isCustomJeep && selectedJeepNo == jNo
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) RailwayGold else Color(0xFF1E2D44))
                                    .border(1.dp, if (isSel) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                                    .clickable {
                                        isCustomJeep = false
                                        selectedJeepNo = jNo
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = jNo,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF0F1E36) else Color.White
                                )
                            }
                        }
                        // Custom Jeep Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCustomJeep) RailwayGold else Color(0xFF1E2D44))
                                .border(1.dp, if (isCustomJeep) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                            .clickable { isCustomJeep = true }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Other / अन्य",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCustomJeep) Color(0xFF0F1E36) else Color.White
                            )
                        }
                    }

                    if (isCustomJeep) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customJeepNo,
                            onValueChange = { customJeepNo = it },
                            label = { Text("Enter Vehicle / Jeep Number", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. JEEP DRIVER DROPDOWN MENU
                    Text("Select Jeep Driver (चालक का नाम ड्रॉपडाउन मेनू):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = driverDropdownExpanded,
                        onExpandedChange = { driverDropdownExpanded = !driverDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (isOtherDriver) "Other / अन्य चालक" else selectedDriverName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Driver (चालक चुनें)", color = Color(0xFFA0B4D0)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverDropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold,
                                unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_jeep_driver")
                        )

                        ExposedDropdownMenu(
                            expanded = driverDropdownExpanded,
                            onDismissRequest = { driverDropdownExpanded = false },
                            modifier = Modifier
                                .background(DarkSurfaceNavy)
                                .heightIn(max = 350.dp)
                        ) {
                            // Option 1: Other / अन्य चालक
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "➕ Other / अन्य चालक (Enter Manually)",
                                            fontWeight = FontWeight.Bold,
                                            color = RailwayGold,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    isOtherDriver = true
                                    driverDropdownExpanded = false
                                    driverMobile = ""
                                }
                            )
                            HorizontalDivider(color = DarkBorderBlue)

                            // Option 2..36: 35 Drivers from PDF Directory
                            allDrivers.forEach { drv ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = drv.name,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Mobile: ${drv.mobile}${if (drv.altMobile.isNotBlank()) " / ${drv.altMobile}" else ""}",
                                                color = Color(0xFF93C5FD),
                                                fontSize = 11.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        isOtherDriver = false
                                        selectedDriverName = drv.name
                                        driverMobile = drv.mobile
                                        driverDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (isOtherDriver) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = otherDriverName,
                                onValueChange = { otherDriverName = it },
                                label = { Text("Driver Name (चालक का नाम)", color = Color(0xFFA0B4D0)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                ),
                                modifier = Modifier.weight(1f).testTag("input_other_driver_name")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = driverMobile,
                                onValueChange = { driverMobile = it },
                                label = { Text("Mobile No.", color = Color(0xFFA0B4D0)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                                ),
                                modifier = Modifier.weight(1f).testTag("input_other_driver_mobile")
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = driverMobile,
                            onValueChange = { driverMobile = it },
                            label = { Text("Driver Mobile No. (स्वतः भरा हुआ)", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_jeep_driver_mobile")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. FROM & TO STATIONS
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "KHS (Kharsia Lobby)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("From (प्रस्थान स्टेशन)", color = Color(0xFFA0B4D0)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = departureTimeInput,
                            onValueChange = { departureTimeInput = it },
                            label = { Text("Dep Time (समय)", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Destination Station Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF132238))
                            .border(1.dp, RailwayGold, RoundedCornerShape(8.dp))
                            .clickable { showStationPicker = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("To Destination (गंतव्य स्टेशन / साइडिंग):", fontSize = 11.sp, color = Color(0xFFA0B4D0))
                                Text("$selectedToCode — $selectedToName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("बदलें ✎", color = RailwayGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. CREW GOING IN JEEP (AUTO-FETCH FROM 369 CREW MASTER)
                    Text("Crew Going in Jeep (रवाना होने वाला क्रू):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = outgoingCrewId,
                            onValueChange = { input ->
                                outgoingCrewId = input
                                val found = staffRepository.findCrew(input)
                                if (found != null) {
                                    outgoingCrewName = found.name
                                    outgoingCrewDesig = found.designation
                                }
                            },
                            label = { Text("Crew ID (जैसे KHS1006)", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = outgoingCrewName,
                            onValueChange = { outgoingCrewName = it },
                            label = { Text("Crew Name", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    if (outgoingCrewDesig.isNotBlank()) {
                        Text("✓ $outgoingCrewDesig", color = RailwayGreen, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = outgoingOtherCrew,
                        onValueChange = { outgoingOtherCrew = it },
                        label = { Text("Other Crew / Guard / Staff (अन्य स्टाफ)", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // ACTION BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick 1-tap "Update Later" button requested by user
                        Button(
                            onClick = {
                                val vNo = if (isCustomJeep) customJeepNo.ifBlank { "Jeep" } else selectedJeepNo
                                val finalDriverName = if (isOtherDriver) otherDriverName.ifBlank { "Other Driver" } else selectedDriverName
                                val trip = JeepTripRecord(
                                    vehicleNo = vNo,
                                    driverName = finalDriverName,
                                    driverMobile = driverMobile,
                                    driverAadhaar = "",
                                    jeepDiName = currentDuty.diName,
                                    shift = currentDuty.shift,
                                    fromStationCode = "KHS",
                                    fromStationName = "Kharsia Lobby",
                                    departureTime = departureTimeInput,
                                    departureDate = currentDate,
                                    toStationCode = selectedToCode,
                                    toStationName = selectedToName,
                                    outgoingCrewId = outgoingCrewId,
                                    outgoingCrewName = outgoingCrewName,
                                    outgoingCrewDesig = outgoingCrewDesig,
                                    outgoingOtherCrew = outgoingOtherCrew,
                                    isUpdateLater = true,
                                    status = "ON_TRIP"
                                )
                                jeepRepository.dispatchJeep(trip)
                                onDispatched()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Update Later ⏱\n(बाकी बाद में भरें)", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }

                        Button(
                            onClick = {
                                val vNo = if (isCustomJeep) customJeepNo.ifBlank { "Jeep" } else selectedJeepNo
                                val finalDriverName = if (isOtherDriver) otherDriverName.ifBlank { "Other Driver" } else selectedDriverName
                                val trip = JeepTripRecord(
                                    vehicleNo = vNo,
                                    driverName = finalDriverName,
                                    driverMobile = driverMobile,
                                    driverAadhaar = "",
                                    jeepDiName = currentDuty.diName,
                                    shift = currentDuty.shift,
                                    fromStationCode = "KHS",
                                    fromStationName = "Kharsia Lobby",
                                    departureTime = departureTimeInput,
                                    departureDate = currentDate,
                                    toStationCode = selectedToCode,
                                    toStationName = selectedToName,
                                    outgoingCrewId = outgoingCrewId,
                                    outgoingCrewName = outgoingCrewName,
                                    outgoingCrewDesig = outgoingCrewDesig,
                                    outgoingOtherCrew = outgoingOtherCrew,
                                    isUpdateLater = false,
                                    status = "ON_TRIP"
                                )
                                jeepRepository.dispatchJeep(trip)
                                onDispatched()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f).height(46.dp)
                        ) {
                            Text("Dispatch Jeep ✓\n(रवाना करें)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    // Station Code Selector Dialog
    if (showStationPicker) {
        StationPickerDialog(
            stations = allStations,
            onDismiss = { showStationPicker = false },
            onSelect = { stn ->
                selectedToCode = stn.code
                selectedToName = stn.name
                showStationPicker = false
            }
        )
    }
}

// =============================================================================
// TAB 2: RETURN & ARRIVAL ENTRY (लॉबी वापसी व क्रू)
// =============================================================================
@Composable
private fun JeepReturnArrivalTab(
    jeepRepository: JeepRepository,
    staffRepository: StaffRepository,
    activeTrips: List<JeepTripRecord>,
    vehicles: List<JeepVehicle>,
    onArrivalRecorded: () -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    var selectedTripId by remember { mutableStateOf(activeTrips.firstOrNull()?.id ?: "") }
    var selectedVehicleNo by remember { mutableStateOf(activeTrips.firstOrNull()?.vehicleNo ?: "89") }
    var arrivalTimeInput by remember { mutableStateOf(currentTime) }

    // Returning crew details
    var returningCrewId by remember { mutableStateOf("") }
    var returningCrewName by remember { mutableStateOf("") }
    var returningCrewDesig by remember { mutableStateOf("") }
    var returningOtherCrew by remember { mutableStateOf("") }
    var returningFromCode by remember { mutableStateOf("") }
    var returningDepTime by remember { mutableStateOf("") }
    var remarksInput by remember { mutableStateOf("") }

    val currentTrip = activeTrips.firstOrNull { it.id == selectedTripId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Record Jeep Arrival at Kharsia (लॉबी वापसी दर्ज करें)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "बस Arrival time डालने पर Jeep Status में दिखने लगेगा कि Kharsia Lobby आ गया है!",
                        color = RailwayGold,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (activeTrips.isNotEmpty()) {
                        Text("Select Returning Jeep (वापस आने वाली जीप चुनें):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeTrips.forEach { trip ->
                                val isSel = trip.id == selectedTripId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF0284C7) else Color(0xFF1E2D44))
                                        .border(1.dp, if (isSel) Color(0xFF38BDF8) else DarkBorderBlue, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedTripId = trip.id
                                            selectedVehicleNo = trip.vehicleNo
                                            returningFromCode = trip.toStationCode
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        Text("Jeep ${trip.vehicleNo}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("From: ${trip.toStationCode}", fontSize = 10.sp, color = RailwayGold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Dropdown / manual vehicle selection if no active trip
                        OutlinedTextField(
                            value = selectedVehicleNo,
                            onValueChange = { selectedVehicleNo = it },
                            label = { Text("Vehicle / Jeep No.", color = Color(0xFFA0B4D0)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arrival Time Input
                    OutlinedTextField(
                        value = arrivalTimeInput,
                        onValueChange = { arrivalTimeInput = it },
                        label = { Text("Jeep Arrival Time at KHS (लॉबी पहुंचने का समय)", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkBorderBlue)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Returning Crew Details (वापस आने वाला क्रू):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = returningCrewId,
                            onValueChange = { input ->
                                returningCrewId = input
                                val found = staffRepository.findCrew(input)
                                if (found != null) {
                                    returningCrewName = found.name
                                    returningCrewDesig = found.designation
                                }
                            },
                            label = { Text("Crew ID", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = returningCrewName,
                            onValueChange = { returningCrewName = it },
                            label = { Text("Crew Name", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = returningFromCode.ifBlank { currentTrip?.toStationCode ?: "Line" },
                            onValueChange = { returningFromCode = it },
                            label = { Text("From Station Code", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = returningDepTime,
                            onValueChange = { returningDepTime = it },
                            label = { Text("Station Dep Time", color = Color(0xFFA0B4D0)) },
                            singleLine = true,
                            placeholder = { Text("e.g. 14:30", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = returningOtherCrew,
                        onValueChange = { returningOtherCrew = it },
                        label = { Text("Other Returning Crew / Guard", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = remarksInput,
                        onValueChange = { remarksInput = it },
                        label = { Text("Remarks (रिमार्क)", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (selectedTripId.isNotBlank()) {
                                jeepRepository.recordArrival(
                                    tripId = selectedTripId,
                                    arrivalTime = arrivalTimeInput,
                                    returningCrewId = returningCrewId,
                                    returningCrewName = returningCrewName,
                                    returningCrewDesig = returningCrewDesig,
                                    returningOtherCrew = returningOtherCrew,
                                    returningFromStationCode = returningFromCode,
                                    returningDepartureTime = returningDepTime,
                                    remarks = remarksInput
                                )
                            } else {
                                jeepRepository.quickVehicleArrival(selectedVehicleNo, arrivalTimeInput)
                            }
                            onArrivalRecorded()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Save Arrival (लॉबी वापसी दर्ज करें) ✓",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 3: BREAKDOWN TRACKER (जीप ब्रेकडाउन व मरम्मत)
// =============================================================================
@Composable
private fun JeepBreakdownTab(
    jeepRepository: JeepRepository,
    vehicles: List<JeepVehicle>,
    breakdowns: List<JeepBreakdownRecord>,
    onReportBreakdownClick: () -> Unit,
    onResolved: () -> Unit
) {
    val activeBreakdowns = breakdowns.filter { !it.isRestored }
    val resolvedBreakdowns = breakdowns.filter { it.isRestored }

    var resolvingBreakdown by remember { mutableStateOf<JeepBreakdownRecord?>(null) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    var restoredTimeInput by remember { mutableStateOf(currentTime) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Jeep Breakdown Tracker", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("गाड़ी खराब होने का समय व सुधार लॉग", color = RailwayGold, fontSize = 11.sp)
                }
                Button(
                    onClick = onReportBreakdownClick,
                    colors = ButtonDefaults.buttonColors(containerColor = RailwayRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("ब्रेकडाउन दर्ज करें +", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Active Breakdowns
        item {
            Text("सक्रिय ब्रेकडाउन (Active Breakdowns - ${activeBreakdowns.size}):", color = RailwayRed, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        }

        if (activeBreakdowns.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RailwayGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("No active breakdowns! All jeeps operational.", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(activeBreakdowns) { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1212)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RailwayRed, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Jeep No: ${b.vehicleNo}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RailwayRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("BREAKDOWN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Breakdown Start: ${b.breakdownStartDate} at ${b.breakdownStartTime} hrs", color = RailwayGold, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Reason / समस्या: ${b.reason}", color = Color.White, fontSize = 12.sp)
                        Text("Driver: ${b.driverName} • Reported by: ${b.reportedBy}", color = Color(0xFFA0B4D0), fontSize = 10.5.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { resolvingBreakdown = b },
                            colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("मरम्मत हो गई (Mark Restored & Available)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Resolved Breakdowns Log
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("सुधार इतिहास (Resolved Log - ${resolvedBreakdowns.size}):", color = Color(0xFFA0B4D0), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        items(resolvedBreakdowns) { b ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jeep ${b.vehicleNo} • ${b.reason}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        Text("From: ${b.breakdownStartTime} ➔ Restored: ${b.breakdownEndTime} hrs", color = RailwayGreen, fontSize = 10.5.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RailwayGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("RESTORED", color = RailwayGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Resolving Dialog
    resolvingBreakdown?.let { b ->
        AlertDialog(
            onDismissRequest = { resolvingBreakdown = null },
            containerColor = DarkSurfaceNavy,
            shape = RoundedCornerShape(14.dp),
            title = {
                Text("Jeep ${b.vehicleNo} Restored (मरम्मत पूर्ण)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text("Enter time when Jeep was restored and ready for duty at Kharsia Lobby:", fontSize = 12.sp, color = Color(0xFFA0B4D0))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoredTimeInput,
                        onValueChange = { restoredTimeInput = it },
                        label = { Text("Restored Time (समय)", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        jeepRepository.resolveBreakdown(b.id, restoredTimeInput)
                        resolvingBreakdown = null
                        onResolved()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen)
                ) {
                    Text("Confirm Restored ✓", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { resolvingBreakdown = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

// =============================================================================
// TAB 4: DRIVER DIRECTORY (35 DRIVERS FROM PDF)
// =============================================================================
@Composable
private fun JeepDriverDirectoryTab(
    drivers: List<JeepDriver>,
    onCallDriver: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredDrivers = remember(searchQuery, drivers) {
        if (searchQuery.isBlank()) drivers
        else {
            val q = searchQuery.trim().lowercase()
            drivers.filter {
                it.name.lowercase().contains(q) ||
                it.mobile.contains(q) ||
                it.altMobile.contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Driver Name or Mobile...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RailwayGold) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Kharsia Lobby Jeep Driver Directory (${filteredDrivers.size} Drivers):",
            color = RailwayGold,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredDrivers) { drv ->
                val index = drivers.indexOf(drv) + 1
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$index", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(drv.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                            Text("Mobile: ${drv.mobile}${if (drv.altMobile.isNotBlank()) " / ${drv.altMobile}" else ""}", color = Color(0xFF93C5FD), fontSize = 11.5.sp)
                        }

                        IconButton(
                            onClick = { onCallDriver(drv.mobile) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(RailwayGreen)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 5: STATION & SIDING CODES DIRECTORY
// =============================================================================
@Composable
private fun StationCodesDirectoryTab(
    stations: List<StationCodeItem>
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, stations) {
        if (query.isBlank()) stations
        else {
            val q = query.trim().lowercase()
            stations.filter { it.code.lowercase().contains(q) || it.name.lowercase().contains(q) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search Station Code or Name (e.g. ROB, CHHL, CPH)...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RailwayGold) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Kharsia Lobby Operational Stations & Sidings (${filtered.size}):", color = RailwayGold, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered) { stn ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (stn.category == "SIDING") Color(0xFF7C2D12) else Color(0xFF1E3A8A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(stn.code, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stn.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(stn.category, fontSize = 9.sp, color = Color(0xFFA0B4D0))
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// DIALOG: CHANGE JEEP DI & SHIFT
// =============================================================================
@Composable
private fun ChangeJeepDiDialog(
    currentDuty: JeepDiDuty,
    staffRepository: StaffRepository,
    onDismiss: () -> Unit,
    onSave: (JeepDiDuty) -> Unit
) {
    val shifts = listOf("06:00 - 14:00", "14:00 - 22:00", "22:00 - 06:00")
    var selectedShift by remember { mutableStateOf(currentDuty.shift) }
    var crewIdInput by remember { mutableStateOf(currentDuty.diCrewId) }
    var nameInput by remember { mutableStateOf(currentDuty.diName) }
    var desigInput by remember { mutableStateOf(currentDuty.diDesignation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Set Duty Jeep DI (ड्यूटी जीप डी.आई)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Shift (शिफ्ट चुनें):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shifts.forEach { s ->
                        val isSel = selectedShift == s
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) RailwayGold else Color(0xFF1E2D44))
                                .border(1.dp, if (isSel) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                                .clickable { selectedShift = s }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.replace(":00", ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0F1E36) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = crewIdInput,
                    onValueChange = { input ->
                        crewIdInput = input
                        val found = staffRepository.findCrew(input)
                        if (found != null) {
                            nameInput = found.name
                            desigInput = found.designation
                        }
                    },
                    label = { Text("Crew ID / Staff ID", color = Color(0xFFA0B4D0)) },
                    placeholder = { Text("e.g. KHS1001", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Jeep DI Name", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desigInput,
                    onValueChange = { desigInput = it },
                    label = { Text("Designation", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        JeepDiDuty(
                            shift = selectedShift,
                            diCrewId = crewIdInput.ifBlank { "KHS1001" },
                            diName = nameInput.ifBlank { "Duty Jeep DI" },
                            diDesignation = desigInput.ifBlank { "LPG / CLI" },
                            dutyDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RailwayGold)
            ) {
                Text("Save Jeep DI", fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// =============================================================================
// DIALOG: RECORD TRIP ARRIVAL
// =============================================================================
@Composable
private fun RecordTripArrivalDialog(
    trip: JeepTripRecord,
    staffRepository: StaffRepository,
    onDismiss: () -> Unit,
    onConfirm: (
        arrivalTime: String,
        retCrewId: String,
        retCrewName: String,
        retCrewDesig: String,
        retOtherCrew: String,
        retFromCode: String,
        retDepTime: String,
        remarks: String
    ) -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    var arrTime by remember { mutableStateOf(currentTime) }

    var retCrewId by remember { mutableStateOf("") }
    var retCrewName by remember { mutableStateOf("") }
    var retCrewDesig by remember { mutableStateOf("") }
    var retOtherCrew by remember { mutableStateOf("") }
    var retFromCode by remember { mutableStateOf(trip.toStationCode) }
    var retDepTime by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Jeep ${trip.vehicleNo} Arrival at Kharsia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("From: ${trip.toStationCode} (${trip.toStationName})", color = RailwayGold, fontSize = 12.sp)

                OutlinedTextField(
                    value = arrTime,
                    onValueChange = { arrTime = it },
                    label = { Text("Arrival Time at KHS (लॉबी पहुंचने का समय)", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Returning Crew (वापस आया क्रू):", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = retCrewId,
                        onValueChange = { input ->
                            retCrewId = input
                            val f = staffRepository.findCrew(input)
                            if (f != null) {
                                retCrewName = f.name
                                retCrewDesig = f.designation
                            }
                        },
                        label = { Text("Crew ID", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = retCrewName,
                        onValueChange = { retCrewName = it },
                        label = { Text("Crew Name", color = Color(0xFFA0B4D0)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                OutlinedTextField(
                    value = retOtherCrew,
                    onValueChange = { retOtherCrew = it },
                    label = { Text("Other Crew / Guard", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(arrTime, retCrewId, retCrewName, retCrewDesig, retOtherCrew, retFromCode, retDepTime, remarks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen)
            ) {
                Text("Confirm Arrival ✓", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// =============================================================================
// DIALOG: QUICK VEHICLE ARRIVAL
// =============================================================================
@Composable
private fun QuickVehicleArrivalDialog(
    jeepRepository: JeepRepository,
    vehicles: List<JeepVehicle>,
    onDismiss: () -> Unit,
    onConfirm: (vehicleNo: String, arrivalTime: String) -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    var selectedVNo by remember { mutableStateOf("89") }
    var arrTime by remember { mutableStateOf(currentTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Quick Jeep Arrival (लॉबी में जीप आई)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Jeep Number:", color = Color.White, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    jeepRepository.standardJeepNumbers.forEach { jNo ->
                        val isSel = selectedVNo == jNo
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) RailwayGold else Color(0xFF1E2D44))
                                .border(1.dp, if (isSel) RailwayGold else DarkBorderBlue, RoundedCornerShape(6.dp))
                                .clickable { selectedVNo = jNo }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(jNo, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF0F1E36) else Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = arrTime,
                    onValueChange = { arrTime = it },
                    label = { Text("Arrival Time at Lobby (समय)", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedVNo, arrTime) },
                colors = ButtonDefaults.buttonColors(containerColor = RailwayGreen)
            ) {
                Text("Save Available ✓", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// =============================================================================
// DIALOG: REPORT BREAKDOWN
// =============================================================================
@Composable
private fun ReportBreakdownDialog(
    vehicles: List<JeepVehicle>,
    currentDiName: String,
    onDismiss: () -> Unit,
    onConfirm: (vehicleNo: String, driverName: String, startTime: String, startDate: String, reason: String, reportedBy: String) -> Unit
) {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    var selectedVNo by remember { mutableStateOf(vehicles.firstOrNull()?.vehicleNo ?: "89") }
    var driverName by remember { mutableStateOf(vehicles.firstOrNull()?.defaultDriverName ?: "") }
    var startTime by remember { mutableStateOf(currentTime) }
    var reason by remember { mutableStateOf("Puncture / Tyre problem") }
    var reportedBy by remember { mutableStateOf(currentDiName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Report Jeep Breakdown (खराबी दर्ज करें)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Vehicle No:", color = Color.White, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    vehicles.map { it.vehicleNo }.distinct().forEach { vNo ->
                        val isSel = selectedVNo == vNo
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) RailwayRed else Color(0xFF1E2D44))
                                .border(1.dp, if (isSel) RailwayRed else DarkBorderBlue, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedVNo = vNo
                                    driverName = vehicles.firstOrNull { it.vehicleNo == vNo }?.defaultDriverName ?: ""
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(vNo, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Breakdown Start Time (खराबी का समय)", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Defect (खराबी का कारण)", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reportedBy,
                    onValueChange = { reportedBy = it },
                    label = { Text("Reported By (सूचनाकर्ता)", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedVNo, driverName, startTime, currentDate, reason, reportedBy)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RailwayRed)
            ) {
                Text("Report Breakdown", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// =============================================================================
// DIALOG: STATION PICKER
// =============================================================================
@Composable
private fun StationPickerDialog(
    stations: List<StationCodeItem>,
    onDismiss: () -> Unit,
    onSelect: (StationCodeItem) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, stations) {
        if (search.isBlank()) stations
        else {
            val q = search.trim().lowercase()
            stations.filter { it.code.lowercase().contains(q) || it.name.lowercase().contains(q) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Select Destination (गंतव्य स्टेशन / साइडिंग)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(modifier = Modifier.height(380.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search code or name...", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold, unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filtered) { stn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF132238))
                                .clickable { onSelect(stn) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stn.code, fontWeight = FontWeight.Black, fontSize = 12.5.sp, color = RailwayGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stn.name, fontSize = 12.sp, color = Color.White, maxLines = 1)
                            }
                            Text(stn.category, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}

// Retaining compatibility screens so existing routes work seamlessly
@Composable
fun JeepAvailabilityScreen(
    jeepRepository: JeepRepository,
    staffRepository: StaffRepository,
    onNavigateToEntry: () -> Unit,
    onBack: () -> Unit
) {
    JeepSubMenuScreen(
        jeepRepository = jeepRepository,
        staffRepository = staffRepository,
        initialTab = 0,
        onBack = onBack
    )
}

@Composable
fun JeepMovementEntryScreen(
    jeepRepository: JeepRepository,
    staffRepository: StaffRepository,
    onBack: () -> Unit
) {
    JeepSubMenuScreen(
        jeepRepository = jeepRepository,
        staffRepository = staffRepository,
        initialTab = 1,
        onBack = onBack
    )
}
