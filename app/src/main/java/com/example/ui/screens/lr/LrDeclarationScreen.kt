package com.example.ui.screens.lr

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffRepository
import com.example.data.lr.LrDeclarationRecord
import com.example.data.lr.LrDeclarationRepository
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
fun LrDeclarationScreen(
    lrRepository: LrDeclarationRepository,
    staffRepository: StaffRepository,
    currentUserId: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val monthsList = listOf("JUL-2026", "AUG-2026", "SEP-2026", "OCT-2026", "NOV-2026", "DEC-2026")
    var selectedMonthIndex by remember { mutableIntStateOf(2) } // Defaults to SEP-2026 (or index 1: AUG-2026)
    val currentMonthYear = monthsList.getOrElse(selectedMonthIndex) { "SEP-2026" }

    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableIntStateOf(0) } // 0: Register Table View, 1: Card View
    var showDeclarationDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<LrDeclarationRecord?>(null) }

    // Dynamic list observed from repository
    val recordsState = remember { mutableStateListOf<LrDeclarationRecord>() }

    fun refreshRecords() {
        recordsState.clear()
        recordsState.addAll(lrRepository.getRecordsForMonth(currentMonthYear))
    }

    // Refresh when month changes
    remember(currentMonthYear) {
        refreshRecords()
        true
    }

    val filteredRecords = remember(searchQuery, recordsState.toList()) {
        if (searchQuery.isBlank()) {
            recordsState.toList()
        } else {
            val q = searchQuery.trim().uppercase()
            recordsState.filter {
                it.crewId.uppercase().contains(q) ||
                it.crewName.uppercase().contains(q) ||
                it.pfNumber.contains(q) ||
                it.designation.uppercase().contains(q)
            }
        }
    }

    val totalRecords = filteredRecords.size
    val allValidCount = filteredRecords.count { it.isAllValid }
    val dueCount = totalRecords - allValidCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KharsiaLobbyEmblem(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LR DECLARATION",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Monthly Section-Wise LR • $currentMonthYear",
                                style = MaterialTheme.typography.labelSmall,
                                color = RailwayGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_lr_back")
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
                        onClick = {
                            viewMode = if (viewMode == 0) 1 else 0
                        },
                        modifier = Modifier.testTag("btn_toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (viewMode == 0) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                            contentDescription = "Toggle View",
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
            // 1. MONTH SELECTOR STRIP
            // =========================================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonthIndex > 0) {
                                selectedMonthIndex--
                                refreshRecords()
                            }
                        },
                        enabled = selectedMonthIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = if (selectedMonthIndex > 0) RailwayGold else Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        monthsList.forEachIndexed { index, mName ->
                            val isSelected = index == selectedMonthIndex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) RailwayGold else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (isSelected) RailwayGold else DarkBorderBlue,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedMonthIndex = index
                                        refreshRecords()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0F1E36) else Color.White
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            if (selectedMonthIndex < monthsList.size - 1) {
                                selectedMonthIndex++
                                refreshRecords()
                            }
                        },
                        enabled = selectedMonthIndex < monthsList.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = if (selectedMonthIndex < monthsList.size - 1) RailwayGold else Color.Gray
                        )
                    }
                }
            }

            // =========================================================================
            // 2. SUMMARY METRICS & DECLARE BUTTON
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Summary Metric Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalRecords", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text(text = "Total Crew", fontSize = 9.5.sp, color = Color(0xFFA0B4D0))
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorderBlue))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$allValidCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = RailwayGreen)
                            Text(text = "All 8 Valid", fontSize = 9.5.sp, color = Color(0xFFA0B4D0))
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorderBlue))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$dueCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (dueCount > 0) RailwayRed else RailwayGold)
                            Text(text = "Due in Sect.", fontSize = 9.5.sp, color = Color(0xFFA0B4D0))
                        }
                    }
                }

                // Add / Declare Button
                Button(
                    onClick = {
                        editingRecord = null
                        showDeclarationDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_add_lr_declaration")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F1E36), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Declare LR", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F1E36))
                    }
                }
            }

            // =========================================================================
            // 3. SEARCH BAR & VIEW MODE INDICATOR
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search Crew ID (e.g. KHS1008), Name or PF Number...", color = Color(0xFF7E8EA6), fontSize = 12.5.sp)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RailwayGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold,
                        unfocusedBorderColor = DarkBorderBlue,
                        focusedContainerColor = DarkSurfaceNavy,
                        unfocusedContainerColor = DarkSurfaceNavy
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("input_search_lr")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // =========================================================================
            // 4. MAIN REGISTER VIEW (TABLE OR CARD)
            // =========================================================================
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No LR Declaration found for $currentMonthYear",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Declare LR' above to submit your monthly section LR declaration.",
                            color = Color(0xFFA0B4D0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (viewMode == 0) {
                // EXACT OFFICIAL REGISTER TABLE FORMAT
                RegisterTableView(
                    records = filteredRecords,
                    monthYear = currentMonthYear,
                    onEdit = {
                        editingRecord = it
                        showDeclarationDialog = true
                    }
                )
            } else {
                // MOBILE COMPACT CARDS VIEW
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { rec ->
                        LrCrewCard(
                            record = rec,
                            onEdit = {
                                editingRecord = rec
                                showDeclarationDialog = true
                            }
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 5. FILL / EDIT LR DECLARATION DIALOG (AUTO-FETCH ENABLED)
        // =========================================================================
        if (showDeclarationDialog) {
            LrDeclarationFormDialog(
                initialRecord = editingRecord,
                currentMonthYear = currentMonthYear,
                currentUserId = currentUserId,
                staffRepository = staffRepository,
                lrRepository = lrRepository,
                onDismiss = {
                    showDeclarationDialog = false
                    editingRecord = null
                },
                onSave = { newRecord ->
                    val success = lrRepository.saveDeclaration(newRecord)
                    if (success) {
                        Toast.makeText(context, "LR Declaration saved for ${newRecord.crewId}!", Toast.LENGTH_SHORT).show()
                        refreshRecords()
                    }
                    showDeclarationDialog = false
                    editingRecord = null
                }
            )
        }
    }
}

// =============================================================================
// REGISTER TABLE VIEW COMPONENT (MATCHING ATTACHED IMAGE)
// =============================================================================
@Composable
private fun RegisterTableView(
    records: List<LrDeclarationRecord>,
    monthYear: String,
    onEdit: (LrDeclarationRecord) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // Table container with horizontal scroll
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .border(1.2.dp, DarkBorderBlue, RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Table Header Banner
                Row(
                    modifier = Modifier
                        .background(Color(0xFF0F1E36))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LR DECLARATION $monthYear",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = RailwayGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "NAME OF SECTION: 8 SECTIONS (YES / NO / TICKS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF93C5FD)
                    )
                }

                // Header Row
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1E3A8A))
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("S.", width = 36.dp)
                    TableHeaderCell("ID", width = 80.dp)
                    TableHeaderCell("PF", width = 110.dp)
                    TableHeaderCell("NAME OF ALP / LP", width = 170.dp)
                    TableHeaderCell("KHS-NIA", width = 68.dp)
                    TableHeaderCell("NIA-BSP", width = 68.dp)
                    TableHeaderCell("BSP-NIA", width = 68.dp)
                    TableHeaderCell("NIA-KHS", width = 68.dp)
                    TableHeaderCell("KHS-RIG", width = 68.dp)
                    TableHeaderCell("KHS-KCHP", width = 72.dp)
                    TableHeaderCell("KCHP-KHS", width = 72.dp)
                    TableHeaderCell("BYPS", width = 62.dp)
                    TableHeaderCell("SIGN", width = 100.dp)
                }

                HorizontalDivider(color = DarkBorderBlue, thickness = 1.dp)

                // Data Rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(records, key = { it.id }) { rec ->
                        val isEven = rec.sNo % 2 == 0
                        Row(
                            modifier = Modifier
                                .background(if (isEven) Color(0xFF132238) else DarkSurfaceNavy)
                                .clickable { onEdit(rec) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // S. No
                            TableCellText("${rec.sNo}", width = 36.dp, color = Color.White, isBold = true)
                            // ID
                            TableCellBadge(rec.crewId, width = 80.dp)
                            // PF
                            TableCellText(rec.pfNumber.ifBlank { "-" }, width = 110.dp, color = Color(0xFF93C5FD))
                            // Name & Last Working Date
                            Column(modifier = Modifier.width(170.dp).padding(horizontal = 6.dp)) {
                                Text(
                                    text = rec.crewName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (rec.lastWorkingDate.isNotBlank()) "Last: ${rec.lastWorkingDate}" else "LAST WORKING DATE IN SECTION",
                                    fontSize = 9.sp,
                                    color = Color(0xFFA0B4D0),
                                    maxLines = 1
                                )
                            }
                            // 8 Sections
                            TableSectionStatusCell(rec.khsNia, width = 68.dp)
                            TableSectionStatusCell(rec.niaBsp, width = 68.dp)
                            TableSectionStatusCell(rec.bspNia, width = 68.dp)
                            TableSectionStatusCell(rec.niaKhs, width = 68.dp)
                            TableSectionStatusCell(rec.khsRig, width = 68.dp)
                            TableSectionStatusCell(rec.khsKchp, width = 72.dp)
                            TableSectionStatusCell(rec.kchpKhs, width = 72.dp)
                            TableSectionStatusCell(rec.byps, width = 62.dp)
                            // Sign
                            TableCellSign(rec.signature.ifBlank { "Declared" }, width = 100.dp)
                        }
                        HorizontalDivider(color = Color(0xFF1E2D44), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TableCellText(text: String, width: androidx.compose.ui.unit.Dp, color: Color, isBold: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun TableCellBadge(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(RailwayNavy)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = RailwayGold
            )
        }
    }
}

@Composable
private fun TableSectionStatusCell(hasLr: Boolean, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasLr) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(RailwayGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "YES",
                    tint = RailwayGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(RailwayRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "NO",
                    tint = RailwayRed,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun TableCellSign(sign: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sign,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            color = Color(0xFFFDE68A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// COMPACT CARD VIEW (MOBILE OPTIMIZED)
// =============================================================================
@Composable
private fun LrCrewCard(
    record: LrDeclarationRecord,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: S.No, ID, Name, Edit icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(RailwayNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${record.sNo}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RailwayGold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = record.crewName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RailwayGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = record.crewId,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailwayGold
                                )
                            }
                        }
                        Text(
                            text = "PF: ${record.pfNumber.ifBlank { "N/A" }} • ${record.designation}",
                            fontSize = 10.5.sp,
                            color = Color(0xFFA0B4D0)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (record.isAllValid) RailwayGreen.copy(alpha = 0.2f) else RailwayGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${record.totalValidSections}/8 Sections",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (record.isAllValid) RailwayGreen else RailwayGold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 8 Section Status Badges (Grid of 4x2)
            val sections = listOf(
                "KHS-NIA" to record.khsNia,
                "NIA-BSP" to record.niaBsp,
                "BSP-NIA" to record.bspNia,
                "NIA-KHS" to record.niaKhs,
                "KHS-RIG" to record.khsRig,
                "KHS-KCHP" to record.khsKchp,
                "KCHP-KHS" to record.kchpKhs,
                "BYPS" to record.byps
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sections.forEach { (name, hasLr) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (hasLr) Color(0xFF064E3B) else Color(0xFF450A0A))
                            .border(
                                1.dp,
                                if (hasLr) RailwayGreen.copy(alpha = 0.4f) else RailwayRed.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasLr) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (hasLr) RailwayGreen else RailwayRed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasLr) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            if (record.lastWorkingDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last Working Date: ${record.lastWorkingDate}",
                        fontSize = 10.sp,
                        color = Color(0xFFA0B4D0)
                    )
                    Text(
                        text = "Sign: ${record.signature}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = RailwayGold
                    )
                }
            }
        }
    }
}

// =============================================================================
// FILL / UPDATE DECLARATION FORM DIALOG (AUTO-FETCH ENABLED)
// =============================================================================
@Composable
private fun LrDeclarationFormDialog(
    initialRecord: LrDeclarationRecord?,
    currentMonthYear: String,
    currentUserId: String,
    staffRepository: StaffRepository,
    lrRepository: LrDeclarationRepository,
    onDismiss: () -> Unit,
    onSave: (LrDeclarationRecord) -> Unit
) {
    var monthYear by remember { mutableStateOf(initialRecord?.monthYear ?: currentMonthYear) }
    var crewIdInput by remember { mutableStateOf(initialRecord?.crewId ?: if (currentUserId.isNotBlank() && currentUserId.startsWith("KHS", ignoreCase = true)) currentUserId else "") }
    var pfNumberInput by remember { mutableStateOf(initialRecord?.pfNumber ?: "") }
    var crewNameInput by remember { mutableStateOf(initialRecord?.crewName ?: "") }
    var designationInput by remember { mutableStateOf(initialRecord?.designation ?: "SALP") }
    var lastWorkingDateInput by remember {
        mutableStateOf(
            initialRecord?.lastWorkingDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )
    }
    var signatureInput by remember { mutableStateOf(initialRecord?.signature ?: crewNameInput.ifBlank { "Declared" }) }

    // 8 Section Checkboxes
    var khsNia by remember { mutableStateOf(initialRecord?.khsNia ?: true) }
    var niaBsp by remember { mutableStateOf(initialRecord?.niaBsp ?: true) }
    var bspNia by remember { mutableStateOf(initialRecord?.bspNia ?: true) }
    var niaKhs by remember { mutableStateOf(initialRecord?.niaKhs ?: true) }
    var khsRig by remember { mutableStateOf(initialRecord?.khsRig ?: true) }
    var khsKchp by remember { mutableStateOf(initialRecord?.khsKchp ?: true) }
    var kchpKhs by remember { mutableStateOf(initialRecord?.kchpKhs ?: true) }
    var byps by remember { mutableStateOf(initialRecord?.byps ?: true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-fetch helper when Crew ID changes
    fun autoFetchCrewDetails(id: String) {
        val trimmed = id.trim().uppercase()
        if (trimmed.isNotBlank()) {
            val crew = staffRepository.findCrewById(trimmed)
            if (crew != null) {
                crewNameInput = crew.name
                designationInput = crew.designation
                if (signatureInput.isBlank() || signatureInput == "Declared") {
                    signatureInput = crew.name
                }
            }
            val knownPf = lrRepository.getPfForCrew(trimmed)
            if (knownPf.isNotBlank()) {
                pfNumberInput = knownPf
            }
        }
    }

    // Trigger initial fetch if new entry with crewId
    remember(crewIdInput) {
        if (initialRecord == null && crewIdInput.isNotBlank()) {
            autoFetchCrewDetails(crewIdInput)
        }
        true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FactCheck,
                    contentDescription = null,
                    tint = RailwayGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialRecord != null) "Update LR Declaration" else "Declare LR (डिक्लेरेशन भरें)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Declaration Month:", fontSize = 12.sp, color = Color(0xFFA0B4D0))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RailwayNavy)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = monthYear, fontWeight = FontWeight.Bold, color = RailwayGold, fontSize = 12.sp)
                    }
                }

                // Crew ID with auto-fetch
                OutlinedTextField(
                    value = crewIdInput,
                    onValueChange = {
                        crewIdInput = it
                        errorMessage = null
                        autoFetchCrewDetails(it)
                    },
                    label = { Text("Crew CMS ID (e.g. KHS1008)", fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold,
                        unfocusedBorderColor = DarkBorderBlue
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_form_crew_id")
                )

                // Name & PF Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = crewNameInput,
                        onValueChange = { crewNameInput = it },
                        label = { Text("Name of ALP / LP", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold,
                            unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1.2f).testTag("input_form_crew_name")
                    )

                    OutlinedTextField(
                        value = pfNumberInput,
                        onValueChange = { pfNumberInput = it },
                        label = { Text("PF Number", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold,
                            unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1f).testTag("input_form_pf_number")
                    )
                }

                // Quick Select All / Clear All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Section LR Status (Yes/No):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = RailwayGold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "All YES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailwayGreen,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RailwayGreen.copy(alpha = 0.15f))
                                .clickable {
                                    khsNia = true
                                    niaBsp = true
                                    bspNia = true
                                    niaKhs = true
                                    khsRig = true
                                    khsKchp = true
                                    kchpKhs = true
                                    byps = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailwayRed,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RailwayRed.copy(alpha = 0.15f))
                                .clickable {
                                    khsNia = false
                                    niaBsp = false
                                    bspNia = false
                                    niaKhs = false
                                    khsRig = false
                                    khsKchp = false
                                    kchpKhs = false
                                    byps = false
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // 8 Section Checkboxes (2 columns x 4 rows)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorderBlue, RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SectionCheckboxItem("KHS - NIA", khsNia, { khsNia = it }, Modifier.weight(1f))
                            SectionCheckboxItem("NIA - BSP", niaBsp, { niaBsp = it }, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SectionCheckboxItem("BSP - NIA", bspNia, { bspNia = it }, Modifier.weight(1f))
                            SectionCheckboxItem("NIA - KHS", niaKhs, { niaKhs = it }, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SectionCheckboxItem("KHS - RIG", khsRig, { khsRig = it }, Modifier.weight(1f))
                            SectionCheckboxItem("KHS - KCHP", khsKchp, { khsKchp = it }, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SectionCheckboxItem("KCHP - KHS", kchpKhs, { kchpKhs = it }, Modifier.weight(1f))
                            SectionCheckboxItem("BYPS (Bypass)", byps, { byps = it }, Modifier.weight(1f))
                        }
                    }
                }

                // Last working date & Signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lastWorkingDateInput,
                        onValueChange = { lastWorkingDateInput = it },
                        label = { Text("Last Working Date", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold,
                            unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = signatureInput,
                        onValueChange = { signatureInput = it },
                        label = { Text("Sign / Signature", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RailwayGold,
                            unfocusedBorderColor = DarkBorderBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = RailwayRed, fontSize = 11.5.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (crewIdInput.isBlank()) {
                        errorMessage = "Please enter Crew CMS ID"
                        return@Button
                    }
                    if (crewNameInput.isBlank()) {
                        errorMessage = "Please enter Crew Name"
                        return@Button
                    }
                    val rec = (initialRecord ?: LrDeclarationRecord()).copy(
                        monthYear = monthYear.trim().uppercase(),
                        crewId = crewIdInput.trim().uppercase(),
                        pfNumber = pfNumberInput.trim(),
                        crewName = crewNameInput.trim().uppercase(),
                        designation = designationInput.trim(),
                        khsNia = khsNia,
                        niaBsp = niaBsp,
                        bspNia = bspNia,
                        niaKhs = niaKhs,
                        khsRig = khsRig,
                        khsKchp = khsKchp,
                        kchpKhs = kchpKhs,
                        byps = byps,
                        lastWorkingDate = lastWorkingDateInput.trim(),
                        signature = signatureInput.ifBlank { "Declared" }.trim()
                    )
                    onSave(rec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RailwayGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_submit_declaration")
            ) {
                Text(text = "Submit Declaration", fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
private fun SectionCheckboxItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = RailwayGreen,
                uncheckedColor = DarkBorderBlue,
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
            color = if (checked) Color.White else Color.Gray
        )
    }
}
