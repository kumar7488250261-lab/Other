package com.example.ui.screens.attendance

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.attendance.AttendanceConstants
import com.example.data.attendance.AttendanceRepository
import com.example.data.attendance.CliPositionRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CliPositionScreen(
    attendanceRepository: AttendanceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedFilterStatus by remember { mutableStateOf("ALL") }
    var records by remember { mutableStateOf(attendanceRepository.getCliPositions(selectedDate)) }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCliRecord by remember { mutableStateOf<CliPositionRecord?>(null) }

    fun refreshList() {
        records = attendanceRepository.getCliPositions(selectedDate)
    }

    val displayRecords = remember(records, selectedFilterStatus) {
        if (selectedFilterStatus == "ALL") records
        else records.filter { it.status.equals(selectedFilterStatus, ignoreCase = true) }
    }

    val filterTabs = listOf("ALL", "Available", "Not Available", "Rest Day", "On Leave")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CLI Position",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "चीफ लोको इंस्पेक्टर स्थिति • ड्यूटी व मूवमेंट रजिस्टर",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_cli")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedCliRecord = null
                        showEditDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.EditCalendar, contentDescription = "Update Position", tint = RailwayGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedCliRecord = null
                    showEditDialog = true
                },
                containerColor = Color(0xFF1E3A8A),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("CLI स्थिति अपडेट करें", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = DarkCanvasBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF172554)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KHARSIA LOBBY • CLI CADRE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Date: $selectedDate",
                                    color = RailwayGold,
                                    fontSize = 12.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val avail = records.count { it.status == "Available" }
                                val notAvail = records.count { it.status == "Not Available" }
                                val rest = records.count { it.status == "Rest Day" }
                                val leave = records.count { it.status == "On Leave" }

                                BadgeBox(label = "Avail", count = avail.toString(), color = RailwayGreen)
                                BadgeBox(label = "Movement", count = notAvail.toString(), color = Color(0xFF38BDF8))
                                BadgeBox(label = "Rest", count = rest.toString(), color = Color(0xFFA78BFA))
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Text(
                    text = "FILTER STATUS (स्थिति अनुसार देखें):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { statusKey ->
                        val isSelected = selectedFilterStatus == statusKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterStatus = statusKey },
                            label = {
                                Text(
                                    text = statusKey,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                containerColor = DarkSurfaceNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF60A5FA) else DarkBorderBlue
                            )
                        )
                    }
                }
            }

            // List of CLI Positions
            if (displayRecords.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.PersonOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("कोई CLI इस स्थिति में नहीं है", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                items(displayRecords, key = { it.id }) { item ->
                    CliPositionCard(
                        item = item,
                        onCall = { phone ->
                            if (phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "मोबाइल नंबर उपलब्ध नहीं है", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            selectedCliRecord = item
                            showEditDialog = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showEditDialog) {
        CliPositionDialog(
            initialRecord = selectedCliRecord,
            defaultDate = selectedDate,
            onDismiss = { showEditDialog = false },
            onSave = { record ->
                attendanceRepository.saveCliPosition(record)
                refreshList()
                showEditDialog = false
                Toast.makeText(context, "CLI स्थिति अपडेट की गई!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CliPositionCard(
    item: CliPositionRecord,
    onCall: (String) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A8A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.cliName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        if (item.scheduledRestDay.isNotBlank()) {
                            Text(
                                text = "Weekly Rest: ${item.scheduledRestDay}",
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                val (badgeBg, badgeText) = when (item.status) {
                    "Available" -> RailwayGreen to "Available (उपलब्ध)"
                    "Not Available" -> Color(0xFF0284C7) to "Out / Movement"
                    "Rest Day" -> Color(0xFF7C3AED) to "Rest Day (रेस्ट)"
                    else -> RailwayRed to "On Leave (अवकाश)"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg.copy(alpha = 0.2f))
                        .border(1.dp, badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorderBlue.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Conditional Info Display
            when (item.status) {
                "Available" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = RailwayGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Available Time: ${if (item.availableTime.isNotBlank()) item.availableTime else "08:00 HRS"}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                "Not Available" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Departure: ${item.departureTime.ifBlank { "--:--" }} • Return: ${item.arrivalTime.ifBlank { "--:--" }}",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (item.sectionOrTrain.isNotBlank()) {
                            Text(
                                text = "Location/Duty: ${item.sectionOrTrain}",
                                color = RailwayGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                "Rest Day" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hotel, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scheduled Weekly Rest: ${item.scheduledRestDay}",
                            color = Color(0xFFDDD6FE),
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = RailwayRed, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "On Leave / Sanctioned Rest: ${item.remarks.ifBlank { "Approved" }}",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Call & Update
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.remarks.isNotBlank() && item.status != "On Leave") {
                    Text(text = "Note: ${item.remarks}", color = Color.Gray, fontSize = 11.sp)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.cugMobile.isNotBlank()) {
                        FilledTonalButton(
                            onClick = { onCall(item.cugMobile) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = RailwayGreen.copy(alpha = 0.2f),
                                contentColor = RailwayGreen
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(item.cugMobile, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    FilledTonalButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFF93C5FD)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Update", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CliPositionDialog(
    initialRecord: CliPositionRecord?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (CliPositionRecord) -> Unit
) {
    var date by remember { mutableStateOf(initialRecord?.date ?: defaultDate) }
    var cliName by remember { mutableStateOf(initialRecord?.cliName ?: AttendanceConstants.CLI_CADRE[0].name) }
    var cugMobile by remember { mutableStateOf(initialRecord?.cugMobile ?: AttendanceConstants.CLI_CADRE[0].mobile) }
    var scheduledRestDay by remember { mutableStateOf(initialRecord?.scheduledRestDay ?: AttendanceConstants.CLI_CADRE[0].extraInfo) }
    var status by remember { mutableStateOf(initialRecord?.status ?: "Available") }

    // Conditional Fields
    var availableTime by remember { mutableStateOf(initialRecord?.availableTime ?: "08:00") }
    var departureTime by remember { mutableStateOf(initialRecord?.departureTime ?: "09:00") }
    var arrivalTime by remember { mutableStateOf(initialRecord?.arrivalTime ?: "18:00") }
    var sectionOrTrain by remember { mutableStateOf(initialRecord?.sectionOrTrain ?: "") }
    var remarks by remember { mutableStateOf(initialRecord?.remarks ?: "") }

    var cliExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CLI Position Update (स्थिति अपडेट)",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Date
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (तारीख YYYY-MM-DD)*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }

                // 2. CLI Name Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = cliExpanded,
                        onExpandedChange = { cliExpanded = !cliExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = cliName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("CLI Name (चीफ लोको इंस्पेक्टर)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cliExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = cliExpanded,
                            onDismissRequest = { cliExpanded = false }
                        ) {
                            AttendanceConstants.CLI_CADRE.forEach { cli ->
                                DropdownMenuItem(
                                    text = { Text("${cli.name} (Rest: ${cli.extraInfo})") },
                                    onClick = {
                                        cliName = cli.name
                                        cugMobile = cli.mobile
                                        scheduledRestDay = cli.extraInfo
                                        cliExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Status Dropdown (Available, Not Available, Rest Day, On Leave)
                item {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("CLI Status (स्थिति)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            AttendanceConstants.CLI_STATUS_LIST.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        status = s
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Conditional Fields: If Available
                if (status == "Available") {
                    item {
                        OutlinedTextField(
                            value = availableTime,
                            onValueChange = { availableTime = it },
                            label = { Text("Available Time (उपलब्ध समय)*") },
                            placeholder = { Text("e.g. 08:00 HRS", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RailwayGreen,
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                }

                // If Not Available (Departure & Arrival Time + Section/Train)
                if (status == "Not Available") {
                    item {
                        OutlinedTextField(
                            value = departureTime,
                            onValueChange = { departureTime = it },
                            label = { Text("Departure Time (प्रस्थान समय)*") },
                            placeholder = { Text("e.g. 09:30 HRS", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = arrivalTime,
                            onValueChange = { arrivalTime = it },
                            label = { Text("Expected Arrival Time (वापसी समय)*") },
                            placeholder = { Text("e.g. 18:00 HRS", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = sectionOrTrain,
                            onValueChange = { sectionOrTrain = it },
                            label = { Text("Section / Footplate / Train / Duty") },
                            placeholder = { Text("e.g. Footplate Train 12834 KHS-BSP", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                }

                // If Rest Day
                if (status == "Rest Day") {
                    item {
                        Text(
                            text = "Scheduled Rest Day: $scheduledRestDay",
                            color = Color(0xFFA78BFA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Remarks
                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (रिमार्क)") },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val record = CliPositionRecord(
                        id = initialRecord?.id ?: UUID.randomUUID().toString(),
                        date = date.trim(),
                        cliName = cliName,
                        cugMobile = cugMobile,
                        scheduledRestDay = scheduledRestDay,
                        status = status,
                        availableTime = availableTime.trim(),
                        departureTime = departureTime.trim(),
                        arrivalTime = arrivalTime.trim(),
                        sectionOrTrain = sectionOrTrain.trim(),
                        remarks = remarks.trim()
                    )
                    onSave(record)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("सुरक्षित करें (Save)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें (Cancel)", color = Color.White)
            }
        },
        containerColor = DarkBackgroundNavy
    )
}
