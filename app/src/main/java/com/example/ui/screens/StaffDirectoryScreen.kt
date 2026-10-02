package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KharsiaStaffItem
import com.example.data.Lobby
import com.example.data.OtherLobbyCrewItem
import com.example.data.StaffContact
import com.example.data.StaffRepository
import com.example.data.StationContact
import com.example.data.TlcContact
import com.example.ui.components.KharsiaLobbyEmblem
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkBorderBlue
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.DarkSurfaceNavy
import com.example.ui.theme.RailwayAmber
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import com.example.ui.theme.RailwayNavy
import com.example.ui.theme.RailwayRed

private fun dialPhoneNumber(context: Context, number: String) {
    val clean = number.replace(Regex("[^0-9+]"), "")
    if (clean.isNotBlank()) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDirectoryScreen(
    staffRepository: StaffRepository,
    onLobbyClick: (Lobby) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val kharsiaStaff = remember { staffRepository.getKharsiaStaffItems() }
    val otherLobbies = remember { staffRepository.getOtherLobbies() }
    val allOtherCrew = remember { staffRepository.getAllOtherLobbiesCrew() }
    val tlcContacts = remember { staffRepository.getTlcContacts() }
    val stations = remember { staffRepository.getStations() }
    val divisionalControlCategories = remember { staffRepository.getDivisionalControlCategories() }

    // Tabs: 0: Kharsia Lobby, 1: Other Lobbies, 2: TLC, 3: Stations CUG (141), 4: Divisional Control
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Kharsia Category Filter: "ALL", "LPG", "TM", "ALP", "CLI", "JEEP DRIVER", "SANDER BOY", "CCC"
    var kharsiaCategoryFilter by remember { mutableStateOf("ALL") }

    // Station Section Filter
    var stationSectionFilter by remember { mutableStateOf("ALL") }

    // Divisional Control Category Filter
    var divisionalFilter by remember { mutableStateOf("ALL") }

    // Filtered Kharsia Staff
    val filteredKharsiaStaff = remember(searchQuery, kharsiaCategoryFilter, kharsiaStaff) {
        kharsiaStaff.filter { item ->
            val matchesCategory = when (kharsiaCategoryFilter) {
                "ALL" -> true
                else -> item.category.equals(kharsiaCategoryFilter, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().uppercase()
                item.name.uppercase().contains(q) ||
                item.id.uppercase().contains(q) ||
                item.badge.uppercase().contains(q) ||
                item.role.uppercase().contains(q) ||
                item.mobile.contains(q) ||
                item.altMobile.contains(q) ||
                item.category.uppercase().contains(q)
            }
            matchesCategory && matchesQuery
        }
    }

    // Filtered Other Lobbies
    val filteredOtherLobbies = remember(searchQuery, otherLobbies) {
        if (searchQuery.isBlank()) otherLobbies
        else otherLobbies.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.categories.any { cat ->
                cat.contacts.any { c ->
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.mobile.contains(searchQuery)
                }
            }
        }
    }

    // Filtered TLC Contacts
    val filteredTlcContacts = remember(searchQuery, tlcContacts) {
        if (searchQuery.isBlank()) tlcContacts
        else {
            val q = searchQuery.trim().uppercase()
            tlcContacts.filter {
                it.name.uppercase().contains(q) ||
                it.mobile.contains(q) ||
                it.designation.uppercase().contains(q) ||
                it.division.uppercase().contains(q)
            }
        }
    }

    // Filtered Stations
    val filteredStations = remember(searchQuery, stationSectionFilter, stations) {
        stations.filter { st ->
            val matchesSection = when (stationSectionFilter) {
                "SEC1" -> st.section.contains("Bilaspur – Jharsuguda", ignoreCase = true)
                "SEC2" -> st.section.contains("Champa – Gevra", ignoreCase = true)
                "SEC3" -> st.section.contains("Bilaspur – Jhalwara", ignoreCase = true)
                "SEC4" -> st.section.contains("Durg – Raipur", ignoreCase = true)
                "SEC5" -> st.section.contains("Itwari", ignoreCase = true)
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().uppercase()
                st.code.uppercase().contains(q) ||
                st.name.uppercase().contains(q) ||
                st.cugMobile.contains(q) ||
                st.landline.contains(q)
            }
            matchesSection && matchesQuery
        }
    }

    // Filtered Divisional Contacts
    val filteredDivisionalContacts = remember(searchQuery, divisionalFilter, divisionalControlCategories) {
        val all = mutableListOf<Pair<String, StaffContact>>()
        divisionalControlCategories.forEach { cat ->
            if (divisionalFilter == "ALL" || cat.category == divisionalFilter) {
                cat.contacts.forEach { c ->
                    all.add(Pair(cat.category, c))
                }
            }
        }
        if (searchQuery.isBlank()) all
        else {
            val q = searchQuery.trim().uppercase()
            all.filter { (cat, c) ->
                cat.uppercase().contains(q) ||
                c.name.uppercase().contains(q) ||
                c.designation.uppercase().contains(q) ||
                c.mobile.contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KharsiaLobbyEmblem(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Staff Directory (स्टाफ डायरेक्टरी)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Kharsia Lobby • Other Lobbies • TLC • Stations",
                                style = MaterialTheme.typography.labelSmall,
                                color = RailwayGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_directory_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
            // SEARCH BAR (Displayed for Kharsia, TLC, Stations, and Divisional Control; Other Lobbies has dedicated lobby & category search)
            if (selectedTab != 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            val placeholder = when (selectedTab) {
                                0 -> "Search Kharsia Staff (Name, ID e.g. KHS1001, Driver, Mobile)..."
                                2 -> "Search TLC Controller by Name or Mobile..."
                                3 -> "Search Station Code (KHS, RIG, BSP) or Name..."
                                else -> "Search Officers, TPC, DPC, Test Room..."
                            }
                            Text(placeholder, color = Color(0xFF7E8EA6), fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = RailwayGold
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_directory"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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
            }

            // MAIN NAVIGATION TABS
            // Kharsia Lobby | Other Lobbies | TLC | Stations CUG | Divisional Control
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceNavy,
                contentColor = RailwayGold,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RailwayGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            "Kharsia Lobby (${kharsiaStaff.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) RailwayGold else Color.White,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            "Other Lobbies (${allOtherCrew.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) RailwayGold else Color.White,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            "TLC (${tlcContacts.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) RailwayGold else Color.White,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            "Stations CUG (${stations.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 3) RailwayGold else Color.White,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            "Divisional Control",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 4) RailwayGold else Color.White,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            // TAB CONTENT VIEWS
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                when (selectedTab) {
                    0 -> KharsiaLobbyTabView(
                        staffList = filteredKharsiaStaff,
                        allStaff = kharsiaStaff,
                        currentCategory = kharsiaCategoryFilter,
                        onCategoryChange = { kharsiaCategoryFilter = it },
                        onCall = { phone -> dialPhoneNumber(context, phone) }
                    )
                    1 -> OtherLobbiesTabView(
                        lobbies = otherLobbies,
                        allCrew = allOtherCrew,
                        searchQuery = searchQuery,
                        onLobbyClick = onLobbyClick,
                        onCall = { phone -> dialPhoneNumber(context, phone) }
                    )
                    2 -> TlcTabView(
                        tlcList = filteredTlcContacts,
                        onCall = { phone -> dialPhoneNumber(context, phone) }
                    )
                    3 -> StationsTabView(
                        stations = filteredStations,
                        currentFilter = stationSectionFilter,
                        onFilterChange = { stationSectionFilter = it },
                        onCall = { phone -> dialPhoneNumber(context, phone) }
                    )
                    4 -> DivisionalControlTabView(
                        categories = divisionalControlCategories,
                        filteredContacts = filteredDivisionalContacts,
                        currentFilter = divisionalFilter,
                        onFilterChange = { divisionalFilter = it },
                        onCall = { phone -> dialPhoneNumber(context, phone) }
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 0: KHARSIA LOBBY DEDICATED VIEW (CATEGORIES WISE: LPG, TM, ALP, CLI, JEEP DRIVER, SANDER BOY, CCC)
// =============================================================================
@Composable
private fun KharsiaLobbyTabView(
    staffList: List<KharsiaStaffItem>,
    allStaff: List<KharsiaStaffItem>,
    currentCategory: String,
    onCategoryChange: (String) -> Unit,
    onCall: (String) -> Unit
) {
    // Counts per category
    val countAll = allStaff.size
    val countLpg = remember(allStaff) { allStaff.count { it.category == "LPG" } }
    val countTm = remember(allStaff) { allStaff.count { it.category == "TM" } }
    val countAlp = remember(allStaff) { allStaff.count { it.category == "ALP" } }
    val countCli = remember(allStaff) { allStaff.count { it.category == "CLI" } }
    val countDriver = remember(allStaff) { allStaff.count { it.category == "JEEP DRIVER" } }
    val countSander = remember(allStaff) { allStaff.count { it.category == "SANDER BOY" } }
    val countCcc = remember(allStaff) { allStaff.count { it.category == "CCC" } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Kharsia Lobby Header Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E48)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, RailwayGold, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KharsiaLobbyEmblem(size = 46.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "खरसिया कंबाइंड लॉबी • KHARSIA LOBBY",
                        fontWeight = FontWeight.Bold,
                        color = RailwayGold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "425 Running Staff & Supervisors • 24x7 Operations",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Desk: 07766-276100 • CCC: 9752442786 • Station: 9752090650",
                        fontSize = 10.sp,
                        color = Color(0xFF93C5FD)
                    )
                }
                IconButton(
                    onClick = { onCall("07766276100") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RailwayGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Kharsia Lobby",
                        tint = Color(0xFF0F1E36),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CATEGORY WISE SELECTOR
        // LPG, TM (right beside LPG), ALP, CLI, JEEP DRIVER, SANDER BOY, CCC
        val categories = listOf(
            "ALL" to "All ($countAll)",
            "LPG" to "LPG ($countLpg)",
            "TM" to "TM / Guard ($countTm)",
            "ALP" to "ALP / SALP ($countAlp)",
            "CLI" to "CLI ($countCli)",
            "JEEP DRIVER" to "Jeep Driver ($countDriver)",
            "SANDER BOY" to "Sander Boy ($countSander)",
            "CCC" to "CCC / CC ($countCcc)"
        )

        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == currentCategory }.coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = RailwayGold,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            categories.forEach { (catKey, label) ->
                val isSelected = currentCategory == catKey
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                        .border(
                            1.dp,
                            if (isSelected) RailwayGold else DarkBorderBlue,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onCategoryChange(catKey) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Showing ${staffList.size} Kharsia Staff members in $currentCategory:",
            fontSize = 11.5.sp,
            color = Color(0xFFA0B4D0)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(staffList, key = { "${it.category}_${it.id}_${it.name}" }) { item ->
                KharsiaStaffCard(item = item, onCall = onCall)
            }
        }
    }
}

@Composable
private fun KharsiaStaffCard(
    item: KharsiaStaffItem,
    onCall: (String) -> Unit
) {
    // Category distinct badge icon and gradient
    val (icon, gradient, badgeColor) = when (item.category) {
        "LPG" -> Triple(
            Icons.Default.Train,
            Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))),
            Color(0xFF60A5FA)
        )
        "TM" -> Triple(
            Icons.Default.Train,
            Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF059669))),
            Color(0xFF34D399)
        )
        "ALP" -> Triple(
            Icons.Default.Person,
            Brush.verticalGradient(listOf(Color(0xFF0E7490), Color(0xFF0891B2))),
            Color(0xFF38BDF8)
        )
        "CLI" -> Triple(
            Icons.Default.Security,
            Brush.verticalGradient(listOf(Color(0xFF5B21B6), Color(0xFF7C3AED))),
            Color(0xFFA78BFA)
        )
        "JEEP DRIVER" -> Triple(
            Icons.Default.DirectionsCar,
            Brush.verticalGradient(listOf(Color(0xFF9A3412), Color(0xFFEA580C))),
            Color(0xFFFB923C)
        )
        "SANDER BOY" -> Triple(
            Icons.Default.Build,
            Brush.verticalGradient(listOf(Color(0xFF0F766E), Color(0xFF0D9488))),
            Color(0xFF2DD4BF)
        )
        else -> Triple(
            Icons.Default.SupportAgent,
            Brush.verticalGradient(listOf(Color(0xFF991B1B), Color(0xFFDC2626))),
            Color(0xFFF87171)
        )
    }

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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(0.8.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badge.ifBlank { item.category },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.role,
                    fontSize = 11.5.sp,
                    color = Color(0xFFA0B4D0)
                )

                if (item.subDetail.isNotBlank()) {
                    Text(
                        text = item.subDetail,
                        fontSize = 10.5.sp,
                        color = Color(0xFF93C5FD)
                    )
                }

                if (item.mobile.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Phone: ${item.mobile}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RailwayGreen
                    )
                }
            }

            // Call Action Buttons
            if (item.mobile.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onCall(item.mobile) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RailwayGreen.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = RailwayGreen,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: OTHER LOBBIES (SECR BILASPUR DIVISION WITH LOBBY CCC INCLUDED)
// =============================================================================
@Composable
private fun OtherLobbiesTabView(
    lobbies: List<Lobby>,
    allCrew: List<OtherLobbyCrewItem>,
    searchQuery: String = "",
    onLobbyClick: (Lobby) -> Unit,
    onCall: (String) -> Unit
) {
    var selectedLobbyCode by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var localSearchQuery by remember { mutableStateOf("") }

    val otherLobbies = remember(lobbies) {
        lobbies.filter { !it.code.equals("CTRL", ignoreCase = true) }
    }

    val selectedLobby = remember(selectedLobbyCode, otherLobbies) {
        if (selectedLobbyCode == "ALL") null else otherLobbies.find { it.code.equals(selectedLobbyCode, ignoreCase = true) }
    }

    // Available categories based on selected lobby or across all lobbies
    val categoryOptions = remember(selectedLobby, allCrew) {
        if (selectedLobby != null) {
            val list = mutableListOf<Pair<String, Int>>()
            list.add("ALL" to selectedLobby.totalContacts)
            for (cat in selectedLobby.categories) {
                list.add(cat.category to cat.contacts.size)
            }
            list
        } else {
            val list = mutableListOf<Pair<String, Int>>()
            val total = allCrew.size
            list.add("ALL" to total)
            val lpGoods = allCrew.count { it.category.contains("Goods", ignoreCase = true) && !it.category.contains("ALP", ignoreCase = true) && !it.category.contains("Assistant", ignoreCase = true) }
            if (lpGoods > 0) list.add("Loco Pilots (Goods)" to lpGoods)
            val alp = allCrew.count { it.category.contains("ALP", ignoreCase = true) || it.category.contains("Assistant", ignoreCase = true) }
            if (alp > 0) list.add("Assistant Loco Pilots (ALP)" to alp)
            val tm = allCrew.count { it.category.contains("Guard", ignoreCase = true) || it.category.contains("Manager", ignoreCase = true) || it.category.contains("TM", ignoreCase = true) }
            if (tm > 0) list.add("Train Managers (Guards)" to tm)
            val shunting = allCrew.count { it.category.contains("Shunting", ignoreCase = true) }
            if (shunting > 0) list.add("Shunting Staff" to shunting)
            val pass = allCrew.count { it.category.contains("Passenger", ignoreCase = true) }
            if (pass > 0) list.add("Loco Pilots (Passenger)" to pass)
            val cli = allCrew.count { it.category.contains("CLI", ignoreCase = true) }
            if (cli > 0) list.add("Chief Loco Inspectors (CLI)" to cli)
            val cc = allCrew.count { it.category.contains("Crew Controller", ignoreCase = true) }
            if (cc > 0) list.add("Crew Controllers (CC)" to cc)
            val ccc = allCrew.count { it.category.contains("CCC", ignoreCase = true) || it.category.contains("Controlling", ignoreCase = true) }
            if (ccc > 0) list.add("Chief Crew Controllers (CCC)" to ccc)
            val misc = allCrew.count { it.category.contains("Office", ignoreCase = true) || it.category.contains("Miscellaneous", ignoreCase = true) }
            if (misc > 0) list.add("Office & Misc Staff" to misc)
            list
        }
    }

    // Reset selectedCategory when lobby changes
    androidx.compose.runtime.LaunchedEffect(selectedLobbyCode) {
        selectedCategory = "ALL"
    }

    val effectiveQuery = localSearchQuery.trim().uppercase()

    // Filter crew strictly by selected Lobby, selected Category, and Search Query by Name
    val filteredCrew = remember(allCrew, selectedLobbyCode, selectedCategory, effectiveQuery) {
        allCrew.filter { item ->
            val matchesLobby = if (selectedLobbyCode == "ALL") true else item.lobbyCode.equals(selectedLobbyCode, ignoreCase = true)
            val matchesCategory = when {
                selectedCategory == "ALL" -> true
                selectedCategory.equals(item.category, ignoreCase = true) -> true
                selectedCategory.contains("Goods", ignoreCase = true) && item.category.contains("Goods", ignoreCase = true) && !item.category.contains("ALP", ignoreCase = true) && !item.category.contains("Assistant", ignoreCase = true) -> true
                (selectedCategory.contains("ALP", ignoreCase = true) || selectedCategory.contains("Assistant", ignoreCase = true)) &&
                    (item.category.contains("ALP", ignoreCase = true) || item.category.contains("Assistant", ignoreCase = true)) -> true
                (selectedCategory.contains("Guard", ignoreCase = true) || selectedCategory.contains("Manager", ignoreCase = true) || selectedCategory.contains("TM", ignoreCase = true)) &&
                    (item.category.contains("Guard", ignoreCase = true) || item.category.contains("Manager", ignoreCase = true) || item.category.contains("TM", ignoreCase = true)) -> true
                selectedCategory.contains("Passenger", ignoreCase = true) && item.category.contains("Passenger", ignoreCase = true) -> true
                selectedCategory.contains("Shunting", ignoreCase = true) && item.category.contains("Shunting", ignoreCase = true) -> true
                selectedCategory.contains("CLI", ignoreCase = true) && item.category.contains("CLI", ignoreCase = true) -> true
                selectedCategory.contains("Crew Controller", ignoreCase = true) && item.category.contains("Crew Controller", ignoreCase = true) -> true
                (selectedCategory.contains("CCC", ignoreCase = true) || selectedCategory.contains("Controlling", ignoreCase = true)) &&
                    (item.category.contains("CCC", ignoreCase = true) || item.category.contains("Controlling", ignoreCase = true)) -> true
                selectedCategory.contains("Office", ignoreCase = true) && (item.category.contains("Office", ignoreCase = true) || item.category.contains("Miscellaneous", ignoreCase = true)) -> true
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = if (effectiveQuery.isBlank()) true else {
                item.name.uppercase().contains(effectiveQuery) ||
                item.mobile.contains(effectiveQuery) ||
                item.designation.uppercase().contains(effectiveQuery)
            }
            matchesLobby && matchesCategory && matchesSearch
        }
    }

    val groupedCrew = remember(filteredCrew, selectedCategory) {
        if (selectedCategory == "ALL") {
            filteredCrew.groupBy { it.category }
        } else {
            mapOf(selectedCategory to filteredCrew)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. LOBBY SELECTOR (HORIZONTAL SCROLLABLE CHIPS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Select Lobby (${otherLobbies.size} Division Lobbies):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RailwayGold
            )
            if (selectedLobbyCode != "ALL") {
                Text(
                    text = "Clear Lobby (Show All)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF60A5FA),
                    modifier = Modifier.clickable { selectedLobbyCode = "ALL" }
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                val isSelected = selectedLobbyCode == "ALL"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                        .border(1.dp, if (isSelected) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                        .clickable { selectedLobbyCode = "ALL" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "All Lobbies (${allCrew.size})",
                        color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(otherLobbies, key = { it.code }) { lobby ->
                val isSelected = selectedLobbyCode == lobby.code
                val shortName = when (lobby.code) {
                    "BSP" -> "Bilaspur (BSP)"
                    "RIG" -> "Raigarh (RIG)"
                    "KRBA" -> "Korba (KRBA)"
                    "SDL" -> "Shahdol (SDL)"
                    "BRJN" -> "Brajrajnagar (BRJN)"
                    "BYT" -> "Bhatapara (BYT)"
                    "BJRI" -> "Bijuri (BJRI)"
                    "DBEC" -> "DBEC (Durg)"
                    "AKT" -> "Akaltara (AKT)"
                    "USL" -> "Uslapur (USL)"
                    "PND" -> "Pendra Rd (PND)"
                    "SJQ" -> "Surajpur (SJQ)"
                    else -> "${lobby.name} (${lobby.code})"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                        .border(1.dp, if (isSelected) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                        .clickable { selectedLobbyCode = lobby.code }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$shortName (${lobby.totalContacts})",
                        color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. CATEGORY SELECTOR (HORIZONTAL SCROLLABLE CHIPS)
        Text(
            text = "Categories Wise Crew (श्रेणी अनुसार क्रू सूची):",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA0B4D0),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(categoryOptions, key = { it.first }) { (catName, count) ->
                val isSelected = selectedCategory == catName
                val shortLabel = when (catName) {
                    "Loco Pilots (Goods)" -> "LP (Goods)"
                    "Assistant Loco Pilots (ALP)" -> "ALP"
                    "Train Managers (Guards)" -> "TM / Guard"
                    "Loco Pilots (Passenger)" -> "Passenger LP"
                    "Chief Loco Inspectors (CLI)" -> "CLI"
                    "Crew Controllers (CC)" -> "CC"
                    "Chief Crew Controllers (CCC)" -> "CCC"
                    "Crew Controlling Centre (CCC)" -> "CCC"
                    "Office & Misc Staff" -> "Office Staff"
                    else -> catName
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF2563EB) else DarkSurfaceNavy)
                        .border(1.dp, if (isSelected) Color(0xFF60A5FA) else DarkBorderBlue, RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = catName }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$shortLabel ($count)",
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // 2b. DEDICATED IN-LOBBY & CATEGORY SEARCH BAR
        val categoryLabel = when (selectedCategory) {
            "ALL" -> "All Categories"
            "Loco Pilots (Goods)" -> "Loco Pilot (LP)"
            "Assistant Loco Pilots (ALP)" -> "ALP"
            "Train Managers (Guards)" -> "Train Manager (Guard)"
            "Loco Pilots (Passenger)" -> "Passenger LP"
            "Shunting Staff" -> "Shunting"
            "Chief Loco Inspectors (CLI)" -> "CLI"
            "Chief Crew Controllers (CCC)", "Crew Controlling Centre (CCC)" -> "CCC"
            else -> selectedCategory
        }

        val searchPlaceholder = when {
            selectedLobby != null && selectedCategory != "ALL" ->
                "Search in ${selectedLobby.code} - $categoryLabel by Name..."
            selectedLobby != null ->
                "Search in ${selectedLobby.name} by Name or Mobile..."
            selectedCategory != "ALL" ->
                "Search in $categoryLabel across Lobbies by Name..."
            else ->
                "Search Crew by Name, Designation, or Mobile in Other Lobbies..."
        }

        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = { localSearchQuery = it },
            placeholder = {
                Text(
                    text = searchPlaceholder,
                    fontSize = 12.sp,
                    color = Color(0xFFA0B4D0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = RailwayGold,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (localSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { localSearchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("input_lobby_local_search"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RailwayGold,
                unfocusedBorderColor = DarkBorderBlue,
                focusedContainerColor = DarkSurfaceNavy,
                unfocusedContainerColor = DarkSurfaceNavy
            )
        )

        // Search scope status line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (localSearchQuery.isNotBlank()) {
                    "Found ${filteredCrew.size} matches in ${selectedLobby?.code ?: "All Lobbies"} ($categoryLabel)"
                } else if (selectedLobby != null) {
                    "Searching in ${selectedLobby.name} (${selectedLobby.code}) • $categoryLabel"
                } else {
                    "All Division Lobbies (${allCrew.size} Crew) • $categoryLabel"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (localSearchQuery.isNotBlank()) RailwayGold else Color(0xFF93C5FD),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (localSearchQuery.isNotBlank()) {
                Text(
                    text = "Clear Search",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5),
                    modifier = Modifier
                        .clickable { localSearchQuery = "" }
                        .padding(start = 8.dp)
                )
            }
        }

        // 3. SELECTED LOBBY HEADER CARD (if a specific lobby is chosen)
        if (selectedLobby != null) {
            val cccContact = selectedLobby.categories
                .find { it.category.contains("CCC", ignoreCase = true) }
                ?.contacts?.firstOrNull()

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E48)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, DarkBorderBlue, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RailwayNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedLobby.code,
                                color = RailwayGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedLobby.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "${selectedLobby.totalContacts} Total Crew • ${selectedLobby.categories.size} Categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0B4D0),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (cccContact != null && cccContact.mobile.isNotBlank()) {
                            IconButton(
                                onClick = { onCall(cccContact.mobile) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RailwayGreen.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call CCC",
                                    tint = RailwayGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        IconButton(
                            onClick = { onLobbyClick(selectedLobby) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RailwayGold.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Details",
                                tint = RailwayGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. CREW COUNT SUMMARY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredCrew.size} Crew Members:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = RailwayGold
            )
            if (selectedLobby != null) {
                Text(
                    text = "Lobby: ${selectedLobby.code}",
                    fontSize = 11.sp,
                    color = Color(0xFF67E8F9),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. CREW CARDS IN LAZYCOLUMN
        if (filteredCrew.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (localSearchQuery.isNotBlank())
                            "No staff found named \"$localSearchQuery\" in ${selectedLobby?.code ?: "All Lobbies"} ($categoryLabel)"
                        else
                            "No crew found in this category.",
                        color = Color(0xFFA0B4D0),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (localSearchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RailwayGold)
                                .clickable { localSearchQuery = "" }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Clear Search", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                groupedCrew.forEach { (catTitle, crewList) ->
                    if (selectedCategory == "ALL") {
                        item(key = "header_${selectedLobbyCode}_$catTitle") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.8.dp, DarkBorderBlue, RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = catTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = RailwayGold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RailwayGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${crewList.size} Crew",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = RailwayGold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(crewList, key = { index, item -> "${item.lobbyCode}_${item.category}_${item.name}_${item.mobile}_$index" }) { _, crewMember ->
                        OtherLobbyCrewCard(item = crewMember, onCall = onCall)
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherLobbyCrewCard(
    item: OtherLobbyCrewItem,
    onCall: (String) -> Unit
) {
    val (badgeBg, badgeText, icon) = when {
        item.category.contains("Goods", ignoreCase = true) -> Triple(
            Color(0xFF1E3A8A).copy(alpha = 0.35f),
            Color(0xFF93C5FD),
            Icons.Default.Train
        )
        item.category.contains("ALP", ignoreCase = true) || item.category.contains("Assistant", ignoreCase = true) -> Triple(
            Color(0xFF0E7490).copy(alpha = 0.35f),
            Color(0xFF67E8F9),
            Icons.Default.Person
        )
        item.category.contains("Guard", ignoreCase = true) || item.category.contains("Manager", ignoreCase = true) || item.category.contains("TM", ignoreCase = true) -> Triple(
            Color(0xFF065F46).copy(alpha = 0.35f),
            Color(0xFF6EE7B7),
            Icons.Default.Train
        )
        item.category.contains("Shunting", ignoreCase = true) -> Triple(
            Color(0xFF581C87).copy(alpha = 0.35f),
            Color(0xFFD8B4FE),
            Icons.Default.Build
        )
        item.category.contains("Passenger", ignoreCase = true) -> Triple(
            Color(0xFF78350F).copy(alpha = 0.35f),
            Color(0xFFFDE68A),
            Icons.Default.Train
        )
        item.category.contains("CLI", ignoreCase = true) || item.category.contains("Inspector", ignoreCase = true) -> Triple(
            Color(0xFF4C1D95).copy(alpha = 0.35f),
            Color(0xFFC4B5FD),
            Icons.Default.Security
        )
        item.category.contains("CCC", ignoreCase = true) || item.category.contains("Controlling", ignoreCase = true) -> Triple(
            Color(0xFF991B1B).copy(alpha = 0.35f),
            Color(0xFFFCA5A5),
            Icons.Default.Phone
        )
        else -> Triple(
            Color(0xFF334155).copy(alpha = 0.35f),
            Color(0xFFCBD5E1),
            Icons.Default.Person
        )
    }

    val shortCategory = when {
        item.category.contains("Goods", ignoreCase = true) -> "LP (Goods)"
        item.category.contains("ALP", ignoreCase = true) || item.category.contains("Assistant", ignoreCase = true) -> "ALP"
        item.category.contains("Guard", ignoreCase = true) || item.category.contains("Manager", ignoreCase = true) -> "TM / Guard"
        item.category.contains("Shunting", ignoreCase = true) -> "Shunting"
        item.category.contains("Passenger", ignoreCase = true) -> "LP (Pass)"
        item.category.contains("CLI", ignoreCase = true) -> "CLI"
        item.category.contains("Crew Controller", ignoreCase = true) -> "CC"
        item.category.contains("Controlling", ignoreCase = true) || item.category.contains("CCC", ignoreCase = true) -> "CCC"
        else -> item.category
    }

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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .border(0.8.dp, badgeText.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = shortCategory,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RailwayNavy)
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = item.lobbyCode,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RailwayGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.designation} • ${item.lobbyName}",
                    fontSize = 11.5.sp,
                    color = Color(0xFFA0B4D0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.mobile.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Phone: ${item.mobile}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RailwayGreen
                    )
                }
            }

            if (item.mobile.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onCall(item.mobile) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RailwayGreen.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call ${item.name}",
                        tint = RailwayGreen,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: TLC (TRACTION LOCO CONTROLLERS) DEDICATED DIRECTORY (46 CONTROLLERS)
// =============================================================================
@Composable
private fun TlcTabView(
    tlcList: List<TlcContact>,
    onCall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RailwayGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFFD97706)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Traction Loco Controllers (TLC)",
                        fontWeight = FontWeight.Bold,
                        color = RailwayGold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "SECR Bilaspur Division • 24x7 Electric Loco Power Control",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Active TLC Officers & Desks (${tlcList.size}):",
            fontSize = 11.5.sp,
            color = Color(0xFFA0B4D0)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(tlcList, key = { "${it.sNo}_${it.name}_${it.mobile}" }) { tlc ->
                TlcCardItem(tlc = tlc, onCall = onCall)
            }
        }
    }
}

@Composable
private fun TlcCardItem(
    tlc: TlcContact,
    onCall: (String) -> Unit
) {
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E3A8A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tlc.sNo}",
                    fontWeight = FontWeight.Bold,
                    color = RailwayGold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tlc.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tlc.designation} • ${tlc.division}",
                    fontSize = 11.sp,
                    color = Color(0xFF93C5FD)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "CUG: ${tlc.mobile}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RailwayGreen
                )
            }

            IconButton(
                onClick = { onCall(tlc.mobile) },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RailwayGreen.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call TLC",
                    tint = RailwayGreen,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

// =============================================================================
// TAB 3: STATIONS CUG DIRECTORY (141 STATIONS)
// =============================================================================
@Composable
private fun StationsTabView(
    stations: List<StationContact>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    onCall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal scroll section chips
        ScrollableTabRow(
            selectedTabIndex = when (currentFilter) {
                "SEC1" -> 1
                "SEC2" -> 2
                "SEC3" -> 3
                "SEC4" -> 4
                "SEC5" -> 5
                else -> 0
            },
            containerColor = Color.Transparent,
            contentColor = RailwayGold,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            val chips = listOf(
                "ALL" to "All (141)",
                "SEC1" to "BSP-IB (27)",
                "SEC2" to "Champa-Korba (13)",
                "SEC3" to "Katni Route (38)",
                "SEC4" to "Durg-Raipur (32)",
                "SEC5" to "Nagpur-Gondia (31)"
            )
            chips.forEach { (key, label) ->
                val isSelected = currentFilter == key
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                        .border(1.dp, if (isSelected) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                        .clickable { onFilterChange(key) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Official SECR Operational CUG & Landline Directory (${stations.size} stations shown):",
            fontSize = 11.5.sp,
            color = Color(0xFFA0B4D0),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(stations, key = { "${it.section}_${it.code}_${it.sNo}" }) { st ->
                StationCardItem(station = st, onCall = onCall)
            }
        }
    }
}

@Composable
private fun StationCardItem(
    station: StationContact,
    onCall: (String) -> Unit
) {
    val isKharsia = station.code.equals("KHS", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isKharsia) Color(0xFF1E2E48) else DarkSurfaceNavy
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.2.dp,
                if (isKharsia) RailwayGold else DarkBorderBlue,
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Code Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isKharsia) Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFFD97706)))
                        else Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = station.code,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (station.code.length > 5) 10.sp else 12.5.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isKharsia) RailwayGold else Color.White,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isKharsia) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RailwayGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "HOME LOBBY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F1E36)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = station.section,
                    fontSize = 10.5.sp,
                    color = Color(0xFF93C5FD),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CUG: ${station.cugMobile}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RailwayGreen
                    )
                    if (station.landline.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "STD: ${station.landline}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF67E8F9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Call Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onCall(station.cugMobile) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RailwayGreen.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call CUG",
                        tint = RailwayGreen,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (station.landline.isNotBlank()) {
                    IconButton(
                        onClick = { onCall(station.landline) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0891B2).copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Call Landline",
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 4: DIVISIONAL CONTROL & OFFICERS
// =============================================================================
@Composable
private fun DivisionalControlTabView(
    categories: List<com.example.data.LobbyCategory>,
    filteredContacts: List<Pair<String, StaffContact>>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    onCall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filters for Divisional Control
        ScrollableTabRow(
            selectedTabIndex = when (currentFilter) {
                "Divisional Officers" -> 1
                "Power & Movement Control" -> 2
                "DPC Contacts" -> 3
                "Lobby Emergency Landlines" -> 4
                else -> 0
            },
            containerColor = Color.Transparent,
            contentColor = RailwayGold,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            val chips = listOf("ALL" to "All") + categories.map { it.category to it.category }
            chips.forEach { (key, label) ->
                val isSelected = currentFilter == key
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RailwayGold else DarkSurfaceNavy)
                        .border(1.dp, if (isSelected) RailwayGold else DarkBorderBlue, RoundedCornerShape(8.dp))
                        .clickable { onFilterChange(key) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF0F1E36) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Divisional Officers & Control Center (${filteredContacts.size}):",
            fontSize = 11.5.sp,
            color = Color(0xFFA0B4D0),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(filteredContacts, key = { "${it.first}_${it.second.name}_${it.second.mobile}" }) { (dept, contact) ->
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
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF7F1D1D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$dept • ${contact.designation}",
                                fontSize = 11.sp,
                                color = Color(0xFF93C5FD)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Number: ${contact.mobile}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RailwayGreen
                            )
                        }

                        IconButton(
                            onClick = { onCall(contact.mobile) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(RailwayGreen.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = RailwayGreen,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
