package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.CrewMember
import com.example.data.StaffRepository
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayNavy

@Composable
fun CrewAutoFetchPicker(
    staffRepository: StaffRepository,
    crewId: String,
    onCrewIdChange: (String) -> Unit,
    crewName: String,
    onCrewNameChange: (String) -> Unit,
    designation: String,
    onDesignationChange: (String) -> Unit,
    onCrewSelected: ((CrewMember) -> Unit)? = null,
    label: String = "Crew ID / CMS ID (e.g. KHS1001)",
    testTagPrefix: String = "crew",
    modifier: Modifier = Modifier
) {
    var showBrowseDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<CrewMember?>(null) }
    var isSuggestionsExpanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<CrewMember>>(emptyList()) }

    // When crewId changes externally or internally, auto-fetch
    LaunchedEffect(crewId) {
        if (crewId.isNotBlank()) {
            val matched = staffRepository.findCrewById(crewId)
            if (matched != null) {
                selectedMember = matched
                onCrewNameChange(matched.name)
                onDesignationChange(matched.designation)
                onCrewSelected?.invoke(matched)
                isSuggestionsExpanded = false
            } else {
                // If not exact match yet, search suggestions
                val results = staffRepository.searchCrew(crewId, limit = 5)
                suggestions = results
                isSuggestionsExpanded = results.isNotEmpty() && !results.any { it.crewId.equals(crewId, true) }
            }
        } else {
            isSuggestionsExpanded = false
            suggestions = emptyList()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Crew ID Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = crewId,
                onValueChange = { input ->
                    onCrewIdChange(input)
                    val matched = staffRepository.findCrewById(input)
                    if (matched != null) {
                        selectedMember = matched
                        onCrewNameChange(matched.name)
                        onDesignationChange(matched.designation)
                        onCrewSelected?.invoke(matched)
                        isSuggestionsExpanded = false
                    }
                },
                label = { Text(label, color = Color(0xFFA0B4D0)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTagPrefix}_input_id"),
                singleLine = true,
                placeholder = { Text("KHS1001 / 1001 / Name", color = Color(0xFF6B7280)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = RailwayGold
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (crewId.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onCrewIdChange("")
                                    onCrewNameChange("")
                                    onDesignationChange("")
                                    selectedMember = null
                                    isSuggestionsExpanded = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        IconButton(
                            onClick = { showBrowseDialog = true },
                            modifier = Modifier
                                .testTag("${testTagPrefix}_btn_browse")
                                .padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = "Search All Crew",
                                tint = RailwayGold
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RailwayGold,
                    unfocusedBorderColor = DarkBorderBlue,
                    focusedContainerColor = DarkSurfaceNavy,
                    unfocusedContainerColor = DarkSurfaceNavy
                )
            )
        }

        // Suggestions Dropdown (Quick Selection while typing)
        if (isSuggestionsExpanded && suggestions.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBackgroundNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .border(1.dp, RailwayGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        text = "Matching Crew in Master (Tap to Auto-Fetch):",
                        fontSize = 11.sp,
                        color = RailwayGold,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    suggestions.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onCrewIdChange(member.crewId)
                                    onCrewNameChange(member.name)
                                    onDesignationChange(member.designation)
                                    selectedMember = member
                                    isSuggestionsExpanded = false
                                    onCrewSelected?.invoke(member)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RailwayNavy)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = member.crewId,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailwayGold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = member.designation,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA0B4D0)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Auto-Fetched Confirmation Badge / Card
        if (crewName.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = RailwayGreen.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RailwayGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RailwayGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTO-FETCHED FROM CREW MASTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RailwayGreen
                            )
                        }
                        if (crewId.isNotBlank()) {
                            Text(
                                text = "ID: $crewId",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RailwayGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = crewName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = designation,
                            fontSize = 12.sp,
                            color = Color(0xFFA0B4D0)
                        )

                        val phone = selectedMember?.mobile ?: ""
                        if (phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = RailwayGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = phone,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RailwayGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Directory Dialog to Pick Any Crew from Master (369 Crew Members)
    if (showBrowseDialog) {
        CrewBrowseDialog(
            staffRepository = staffRepository,
            onDismiss = { showBrowseDialog = false },
            onSelect = { member ->
                onCrewIdChange(member.crewId)
                onCrewNameChange(member.name)
                onDesignationChange(member.designation)
                selectedMember = member
                onCrewSelected?.invoke(member)
                showBrowseDialog = false
            }
        )
    }
}

@Composable
fun CrewBrowseDialog(
    staffRepository: StaffRepository,
    onDismiss: () -> Unit,
    onSelect: (CrewMember) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf(0) } // 0: ALL, 1: Kharsia, 2: LP, 3: ALP, 4: TM/Guard
    val allCrew = remember { staffRepository.getAllCrewMaster() }

    val filteredList = remember(searchQuery, selectedCategoryTab, allCrew) {
        allCrew.filter { crew ->
            val matchesCategory = when (selectedCategoryTab) {
                1 -> crew.cadre.contains("Kharsia", ignoreCase = true) || crew.crewId.startsWith("KHS", ignoreCase = true)
                2 -> crew.category.equals("LP", ignoreCase = true) || crew.designation.contains("LP", ignoreCase = true)
                3 -> crew.category.equals("ALP", ignoreCase = true) || crew.designation.contains("ALP", ignoreCase = true)
                4 -> crew.category.equals("GUARD", ignoreCase = true) || crew.designation.contains("Guard", ignoreCase = true) || crew.designation.contains("Manager", ignoreCase = true)
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().uppercase()
                crew.crewId.uppercase().contains(q) ||
                crew.name.uppercase().contains(q) ||
                crew.designation.uppercase().contains(q) ||
                crew.cadre.uppercase().contains(q) ||
                crew.mobile.contains(q)
            }
            matchesCategory && matchesSearch
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Division Crew Master Directory",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "Auto-fetch crew details across Kharsia & All Division Lobbies",
                    color = RailwayGold,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Search bar inside dialog
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by CMS ID, Name, Lobby...", color = Color(0xFFA0B4D0)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = RailwayGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = RailwayGold,
                        unfocusedBorderColor = DarkBorderBlue,
                        focusedContainerColor = DarkBackgroundNavy,
                        unfocusedContainerColor = DarkBackgroundNavy
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: All, Kharsia, LP, ALP, TM
                TabRow(
                    selectedTabIndex = selectedCategoryTab,
                    containerColor = DarkBackgroundNavy,
                    contentColor = RailwayGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                            color = RailwayGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedCategoryTab == 0,
                        onClick = { selectedCategoryTab = 0 },
                        text = { Text("ALL (${allCrew.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategoryTab == 0) RailwayGold else Color.White) }
                    )
                    Tab(
                        selected = selectedCategoryTab == 1,
                        onClick = { selectedCategoryTab = 1 },
                        text = { Text("KHS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategoryTab == 1) RailwayGold else Color.White) }
                    )
                    Tab(
                        selected = selectedCategoryTab == 2,
                        onClick = { selectedCategoryTab = 2 },
                        text = { Text("LP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategoryTab == 2) RailwayGold else Color.White) }
                    )
                    Tab(
                        selected = selectedCategoryTab == 3,
                        onClick = { selectedCategoryTab = 3 },
                        text = { Text("ALP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategoryTab == 3) RailwayGold else Color.White) }
                    )
                    Tab(
                        selected = selectedCategoryTab == 4,
                        onClick = { selectedCategoryTab = 4 },
                        text = { Text("TM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategoryTab == 4) RailwayGold else Color.White) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Showing ${filteredList.size} Crew Members:",
                    fontSize = 11.sp,
                    color = Color(0xFFA0B4D0),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList) { crew ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBackgroundNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorderBlue, RoundedCornerShape(8.dp))
                                .clickable { onSelect(crew) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RailwayNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = RailwayGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = crew.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RailwayGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = crew.crewId,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RailwayGold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${crew.designation} • ${crew.cadre}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFA0B4D0)
                                    )
                                    if (crew.mobile.isNotBlank()) {
                                        Text(
                                            text = "CUG: ${crew.mobile}",
                                            fontSize = 11.sp,
                                            color = RailwayGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RailwayGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurfaceNavy,
        shape = RoundedCornerShape(16.dp)
    )
}
