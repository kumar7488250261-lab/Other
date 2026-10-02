package com.example.ui.screens.attendance

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.attendance.CommonBoyAttendanceRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningRoomBoyScreen(
    attendanceRepository: AttendanceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedShift by remember { mutableStateOf("ALL") }
    var records by remember { mutableStateOf(attendanceRepository.getRunningRoomRecords(selectedDate, selectedShift)) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<CommonBoyAttendanceRecord?>(null) }

    fun refreshList() {
        records = attendanceRepository.getRunningRoomRecords(selectedDate, selectedShift)
    }

    val shifts = listOf(
        "ALL" to "All Shifts (सभी)",
        "00-08" to "Shift 00-08",
        "08-16" to "Shift 08-16",
        "16-00" to "Shift 16-00"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Running Room Boy Attendance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "रनिंग रूम बॉय उपस्थिति • शिफ्ट अनुसार",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_rr")) {
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
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Attendance", tint = RailwayGold)
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
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("उपस्थिति दर्ज करें (Add)", fontWeight = FontWeight.Bold) }
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
            // Header Info Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1065)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                text = "KHARSIA RUNNING ROOM",
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
                            BadgeBox(label = "Total Present", count = records.size.toString(), color = Color(0xFF7C3AED))
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
                                selectedContainerColor = Color(0xFF7C3AED),
                                containerColor = DarkSurfaceNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFFA78BFA) else DarkBorderBlue
                            )
                        )
                    }
                }
            }

            // List of Records (Strictly 5 fields: Date, Shift, Name, Mobile No, BA No)
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
                            Icon(
                                imageVector = Icons.Default.Bed,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "इस शिफ्ट में कोई उपस्थिति दर्ज नहीं है",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "उपस्थिति दर्ज करने के लिए नीचे दिए गए बटन पर टैप करें",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    CommonAttendanceCard(
                        record = record,
                        accentColor = Color(0xFFA78BFA),
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
                            attendanceRepository.deleteRunningRoomRecord(record.id)
                            refreshList()
                            Toast.makeText(context, "${record.name} का रिकॉर्ड हटाया गया", Toast.LENGTH_SHORT).show()
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
        CommonBoyAttendanceDialog(
            title = "Running Room Boy Attendance",
            initialRecord = editingRecord,
            defaultDate = selectedDate,
            boysList = AttendanceConstants.RUNNING_ROOM_BOYS,
            accentColor = Color(0xFF7C3AED),
            onDismiss = { showAddDialog = false },
            onSave = { newRecord ->
                attendanceRepository.saveRunningRoomRecord(newRecord)
                refreshList()
                showAddDialog = false
                Toast.makeText(context, "उपस्थिति सुरक्षित की गई!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CommonAttendanceCard(
    record: CommonBoyAttendanceRecord,
    accentColor: Color,
    onCall: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Name and Shift Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Date: ${record.date}",
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp
                        )
                    }
                }

                // Shift Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Shift ${record.shift}",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorderBlue.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: BA No & Mobile / Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = RailwayGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BA No: ${record.baNo}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Call Button
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

                    // Edit
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF93C5FD), modifier = Modifier.size(17.dp))
                    }

                    // Delete
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
fun CommonBoyAttendanceDialog(
    title: String,
    initialRecord: CommonBoyAttendanceRecord?,
    defaultDate: String,
    boysList: List<com.example.data.attendance.StaffDirectoryItem>,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (CommonBoyAttendanceRecord) -> Unit
) {
    var date by remember { mutableStateOf(initialRecord?.date ?: defaultDate) }
    var shift by remember { mutableStateOf(initialRecord?.shift ?: "08-16") }
    var selectedBoyName by remember { mutableStateOf(initialRecord?.name ?: "") }
    var customName by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    var mobileNo by remember { mutableStateOf(initialRecord?.mobileNo ?: "") }
    var baNo by remember { mutableStateOf(initialRecord?.baNo ?: "") }

    var shiftExpanded by remember { mutableStateOf(false) }
    var nameExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialRecord != null) {
            val exists = boysList.any { it.name.equals(initialRecord.name, ignoreCase = true) }
            if (!exists) {
                isOtherSelected = true
                customName = initialRecord.name
            }
        } else if (boysList.isNotEmpty()) {
            selectedBoyName = boysList[0].name
            mobileNo = boysList[0].mobile
            if (boysList[0].defaultShift.isNotBlank()) {
                shift = boysList[0].defaultShift
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRecord == null) title else "Edit Attendance",
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
                        label = { Text("Date (YYYY-MM-DD)*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }

                // 2. Shift Dropdown (00-08, 08-16, 16-00)
                item {
                    ExposedDropdownMenuBox(
                        expanded = shiftExpanded,
                        onExpandedChange = { shiftExpanded = !shiftExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = shift,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Shift (00-08, 08-16, 16-00)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = shiftExpanded,
                            onDismissRequest = { shiftExpanded = false }
                        ) {
                            AttendanceConstants.SHIFTS_COMMON.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("Shift $s") },
                                    onClick = {
                                        shift = s
                                        shiftExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Name Dropdown (with "Other" option)
                item {
                    ExposedDropdownMenuBox(
                        expanded = nameExpanded,
                        onExpandedChange = { nameExpanded = !nameExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (isOtherSelected) "Other (अन्य)" else selectedBoyName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Name (नाम - ड्रॉपडाउन)*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = nameExpanded,
                            onDismissRequest = { nameExpanded = false }
                        ) {
                            boysList.forEach { boy ->
                                DropdownMenuItem(
                                    text = { Text("${boy.name} (${boy.mobile})") },
                                    onClick = {
                                        selectedBoyName = boy.name
                                        mobileNo = boy.mobile
                                        if (boy.defaultShift.isNotBlank()) {
                                            shift = boy.defaultShift
                                        }
                                        isOtherSelected = false
                                        nameExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Other (अन्य नया नाम दर्ज करें)", fontWeight = FontWeight.Bold, color = accentColor) },
                                onClick = {
                                    isOtherSelected = true
                                    selectedBoyName = ""
                                    mobileNo = ""
                                    nameExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom Name field if "Other" is chosen
                if (isOtherSelected) {
                    item {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Enter Other Boy Name (नया नाम)*") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = DarkBorderBlue
                            )
                        )
                    }
                }

                // 4. Mobile No (auto-filled from dropdown or editable)
                item {
                    OutlinedTextField(
                        value = mobileNo,
                        onValueChange = { if (it.length <= 10) mobileNo = it },
                        label = { Text("Mobile No (मोबाइल नं)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }

                // 5. BA (breathalyzer) No
                item {
                    OutlinedTextField(
                        value = baNo,
                        onValueChange = { baNo = it },
                        label = { Text("BA (Breathalyzer) No*") },
                        placeholder = { Text("e.g. BA-104 (0.00%)", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = DarkBorderBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            val effectiveName = if (isOtherSelected) customName.trim() else selectedBoyName.trim()
            Button(
                onClick = {
                    if (effectiveName.isBlank() || mobileNo.isBlank()) return@Button
                    val finalRecord = initialRecord?.copy(
                        date = date.trim(),
                        shift = shift,
                        name = effectiveName,
                        mobileNo = mobileNo.trim(),
                        baNo = baNo.trim(),
                        timestamp = System.currentTimeMillis()
                    ) ?: CommonBoyAttendanceRecord(
                        date = date.trim(),
                        shift = shift,
                        name = effectiveName,
                        mobileNo = mobileNo.trim(),
                        baNo = baNo.trim()
                    )
                    onSave(finalRecord)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                enabled = effectiveName.isNotBlank() && mobileNo.isNotBlank()
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
