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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.attendance.AttendanceConstants
import com.example.data.attendance.AttendanceRepository
import com.example.data.attendance.JeepDriverAttendanceRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeepDriverAttendanceScreen(
    attendanceRepository: AttendanceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedShift by remember { mutableStateOf("ALL") }
    var records by remember { mutableStateOf(attendanceRepository.getJeepDriverAttendances(selectedDate, selectedShift)) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<JeepDriverAttendanceRecord?>(null) }

    fun refreshList() {
        records = attendanceRepository.getJeepDriverAttendances(selectedDate, selectedShift)
    }

    val shifts = listOf(
        "ALL" to "All Shifts (सभी)",
        "08-20" to "Shift 08-20",
        "20-08" to "Shift 20-08",
        "05-17" to "Shift 05-17",
        "17-05" to "Shift 17-05"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Jeep Driver Attendance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "जीप चालक उपस्थिति • टेकन चार्ज व बी.ए. रजिस्टर",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_jeep_driver")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingRecord = null
                        showAddDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "Add Driver Attendance", tint = RailwayGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackgroundNavy)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingRecord = null
                    showAddDialog = true
                },
                containerColor = Color(0xFF0891B2),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("चालक उपस्थिति दर्ज करें", fontWeight = FontWeight.Bold) }
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF164E63)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KHARSIA LOBBY • JEEP DRIVERS",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Date: $selectedDate",
                                color = RailwayGold,
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeBox(label = "Drivers on Duty", count = records.size.toString(), color = Color(0xFF22D3EE))
                        }
                    }
                }
            }

            // Shift Filter
            item {
                Text(
                    text = "SELECT SHIFT (शिफ्ट चुनें):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shifts) { (shiftKey, shiftLabel) ->
                        val isSelected = selectedShift == shiftKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedShift = shiftKey
                                refreshList()
                            },
                            label = {
                                Text(
                                    text = shiftLabel,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0891B2),
                                containerColor = DarkSurfaceNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF22D3EE) else DarkBorderBlue
                            )
                        )
                    }
                }
            }

            // List of Records
            if (records.isEmpty()) {
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
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("इस शिफ्ट में कोई जीप चालक उपस्थिति नहीं मिली", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("उपस्थिति दर्ज करने के लिए नीचे दिए गए बटन पर टैप करें", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    JeepDriverAttendanceCard(
                        record = record,
                        onCall = { phone ->
                            if (phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "मोबाइल नंबर उपलब्ध नहीं है", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            editingRecord = record
                            showAddDialog = true
                        },
                        onDelete = {
                            attendanceRepository.deleteJeepDriverAttendance(record.id)
                            refreshList()
                            Toast.makeText(context, "${record.driverName} का रिकॉर्ड हटाया गया", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        JeepDriverAttendanceDialog(
            initialRecord = editingRecord,
            defaultDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = { record ->
                attendanceRepository.saveJeepDriverAttendance(record)
                refreshList()
                showAddDialog = false
                Toast.makeText(context, "जीप चालक उपस्थिति सुरक्षित की गई!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun JeepDriverAttendanceCard(
    record: JeepDriverAttendanceRecord,
    onCall: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF0891B2).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Driver Name & Jeep No Badge
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
                            .background(Color(0xFF164E63)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF22D3EE), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.driverName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Date: ${record.date} • Shift: ${record.shiftTime}",
                            color = Color(0xFF67E8F9),
                            fontSize = 12.sp
                        )
                    }
                }

                // Jeep No Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0891B2).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF22D3EE), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Jeep #${record.jeepNo}",
                        color = Color(0xFF22D3EE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorderBlue.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: BA No & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = RailwayGold, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BA No: ${record.baNo}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (record.mobileNo.isNotBlank()) {
                        FilledTonalButton(
                            onClick = { onCall(record.mobileNo) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = RailwayGreen.copy(alpha = 0.2f),
                                contentColor = RailwayGreen
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(record.mobileNo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF93C5FD), modifier = Modifier.size(17.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeepDriverAttendanceDialog(
    initialRecord: JeepDriverAttendanceRecord?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (JeepDriverAttendanceRecord) -> Unit
) {
    var date by remember { mutableStateOf(initialRecord?.date ?: defaultDate) }
    var selectedDriverName by remember { mutableStateOf(initialRecord?.driverName ?: AttendanceConstants.JEEP_DRIVERS[0].name) }
    var customDriverName by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    var mobileNo by remember { mutableStateOf(initialRecord?.mobileNo ?: AttendanceConstants.JEEP_DRIVERS[0].mobile) }
    var jeepNo by remember { mutableStateOf(initialRecord?.jeepNo ?: "89") }
    var shiftTime by remember { mutableStateOf(initialRecord?.shiftTime ?: "08-20") }
    var baNo by remember { mutableStateOf(initialRecord?.baNo ?: "") }

    var driverExpanded by remember { mutableStateOf(false) }
    var jeepExpanded by remember { mutableStateOf(false) }
    var shiftExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialRecord != null) {
            val exists = AttendanceConstants.JEEP_DRIVERS.any { it.name.equals(initialRecord.driverName, ignoreCase = true) }
            if (!exists) {
                isOtherSelected = true
                customDriverName = initialRecord.driverName
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRecord == null) "Jeep Driver Attendance (उपस्थिति)" else "Edit Driver Attendance",
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
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }

                // 2. Driver Name Dropdown (with Other option)
                item {
                    ExposedDropdownMenuBox(
                        expanded = driverExpanded,
                        onExpandedChange = { driverExpanded = !driverExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (isOtherSelected) "Other (अन्य चालक)" else selectedDriverName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Driver Name (चालक का नाम)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = driverExpanded,
                            onDismissRequest = { driverExpanded = false }
                        ) {
                            AttendanceConstants.JEEP_DRIVERS.forEach { driver ->
                                DropdownMenuItem(
                                    text = { Text("${driver.name} (${driver.mobile})") },
                                    onClick = {
                                        selectedDriverName = driver.name
                                        mobileNo = driver.mobile
                                        isOtherSelected = false
                                        driverExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Other (अन्य नया नाम दर्ज करें)", fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE)) },
                                onClick = {
                                    isOtherSelected = true
                                    selectedDriverName = ""
                                    mobileNo = ""
                                    driverExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isOtherSelected) {
                    item {
                        OutlinedTextField(
                            value = customDriverName,
                            onValueChange = { customDriverName = it },
                            label = { Text("Enter Other Driver Name*") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                }

                // 3. Driver Mobile No
                item {
                    OutlinedTextField(
                        value = mobileNo,
                        onValueChange = { if (it.length <= 10) mobileNo = it },
                        label = { Text("Driver Mobile No (मोबाइल नं)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }

                // 4. Taken Charge Jeep No (89, 89(II), 79, 31, 22, 91)
                item {
                    ExposedDropdownMenuBox(
                        expanded = jeepExpanded,
                        onExpandedChange = { jeepExpanded = !jeepExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = jeepNo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Taken Charge Jeep No (89, 89(II), 79, 31, 22, 91)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jeepExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = jeepExpanded,
                            onDismissRequest = { jeepExpanded = false }
                        ) {
                            AttendanceConstants.JEEP_NUMBERS.forEach { jNo ->
                                DropdownMenuItem(
                                    text = { Text("Jeep #$jNo") },
                                    onClick = {
                                        jeepNo = jNo
                                        jeepExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Shift Time (20-08, 08-20, 05-17, 17-05)
                item {
                    ExposedDropdownMenuBox(
                        expanded = shiftExpanded,
                        onExpandedChange = { shiftExpanded = !shiftExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = shiftTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Shift Time (20-08, 08-20, 05-17, 17-05)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = shiftExpanded,
                            onDismissRequest = { shiftExpanded = false }
                        ) {
                            AttendanceConstants.JEEP_SHIFT_TIMES.forEach { sTime ->
                                DropdownMenuItem(
                                    text = { Text("Shift $sTime") },
                                    onClick = {
                                        shiftTime = sTime
                                        shiftExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. BA No
                item {
                    OutlinedTextField(
                        value = baNo,
                        onValueChange = { baNo = it },
                        label = { Text("BA (Breathalyzer) No*") },
                        placeholder = { Text("e.g. BA-J10 (0.00%)", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            val finalName = if (isOtherSelected) customDriverName.trim() else selectedDriverName.trim()
            Button(
                onClick = {
                    if (finalName.isBlank() || mobileNo.isBlank()) return@Button
                    val finalRecord = initialRecord?.copy(
                        date = date.trim(),
                        driverName = finalName,
                        mobileNo = mobileNo.trim(),
                        jeepNo = jeepNo,
                        shiftTime = shiftTime,
                        baNo = baNo.trim(),
                        timestamp = System.currentTimeMillis()
                    ) ?: JeepDriverAttendanceRecord(
                        date = date.trim(),
                        driverName = finalName,
                        mobileNo = mobileNo.trim(),
                        jeepNo = jeepNo,
                        shiftTime = shiftTime,
                        baNo = baNo.trim()
                    )
                    onSave(finalRecord)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2)),
                enabled = finalName.isNotBlank() && mobileNo.isNotBlank()
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
