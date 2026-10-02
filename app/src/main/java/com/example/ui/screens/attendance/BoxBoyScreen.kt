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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun BoxBoyScreen(
    attendanceRepository: AttendanceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedShift by remember { mutableStateOf("ALL") }
    var records by remember { mutableStateOf(attendanceRepository.getBoxBoyRecords(selectedDate, selectedShift)) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<CommonBoyAttendanceRecord?>(null) }

    fun refreshList() {
        records = attendanceRepository.getBoxBoyRecords(selectedDate, selectedShift)
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
                            text = "Box Boy Attendance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "बॉक्स बॉय उपस्थिति • शिफ्ट व तारीख अनुसार",
                            style = MaterialTheme.typography.labelSmall,
                            color = RailwayGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_boxboy")) {
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
                        Icon(imageVector = Icons.Default.AddBox, contentDescription = "Add Box Boy", tint = RailwayGold)
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
                containerColor = Color(0xFF0D9488),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("बॉक्स बॉय उपस्थिति दर्ज करें", fontWeight = FontWeight.Bold) }
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                text = "KHARSIA CREW LINE BOX REGISTER",
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
                            BadgeBox(label = "Total Present", count = records.size.toString(), color = Color(0xFF0D9488))
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
                                selectedContainerColor = Color(0xFF0D9488),
                                containerColor = DarkSurfaceNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF2DD4BF) else DarkBorderBlue
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
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "इस शिफ्ट में कोई बॉक्स बॉय उपस्थिति नहीं मिली",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "उपस्थिति जोड़ने के लिए नीचे दिए गए बटन पर टैप करें",
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
                        accentColor = Color(0xFF2DD4BF),
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
                            attendanceRepository.deleteBoxBoyRecord(record.id)
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
            title = "Box Boy Attendance",
            initialRecord = editingRecord,
            defaultDate = selectedDate,
            boysList = AttendanceConstants.BOX_BOYS,
            accentColor = Color(0xFF0D9488),
            onDismiss = { showAddDialog = false },
            onSave = { newRecord ->
                attendanceRepository.saveBoxBoyRecord(newRecord)
                refreshList()
                showAddDialog = false
                Toast.makeText(context, "बॉक्स बॉय उपस्थिति सुरक्षित की गई!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
