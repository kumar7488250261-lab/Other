package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class StaffRepository(private val context: Context) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val cachedLobbies: List<Lobby> by lazy {
        loadLobbies()
    }

    private val cachedStations: List<StationContact> by lazy {
        loadStations()
    }

    private val phoneMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (lobby in cachedLobbies) {
            for (category in lobby.categories) {
                for (contact in category.contacts) {
                    val phone = if (contact.mobile.isNotBlank()) contact.mobile else contact.cug
                    if (contact.name.isNotBlank() && phone.isNotBlank()) {
                        map[contact.name.trim().uppercase()] = phone
                        val simplified = contact.name.replace(Regex("""\b(G-?12|COLP|\d+)\b""", RegexOption.IGNORE_CASE), "").trim().uppercase()
                        if (simplified.isNotEmpty()) {
                            map[simplified] = phone
                        }
                    }
                }
            }
        }
        map
    }

    private val cachedCrewMembers: List<CrewMember> by lazy {
        loadCrewMaster()
    }

    private fun loadLobbies(): List<Lobby> {
        return try {
            val json = context.assets.open("kharsia_directory.json").bufferedReader().use {
                it.readText()
            }
            val adapter = moshi.adapter(DirectoryResponse::class.java)
            adapter.fromJson(json)?.lobbies ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadStations(): List<StationContact> {
        return try {
            val json = context.assets.open("secr_stations.json").bufferedReader().use {
                it.readText()
            }
            val adapter = moshi.adapter(StationResponse::class.java)
            adapter.fromJson(json)?.stations ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadCrewMaster(): List<CrewMember> {
        return try {
            val json = context.assets.open("kharsia_crew_master.json").bufferedReader().use {
                it.readText()
            }
            val listType = Types.newParameterizedType(List::class.java, CrewMember::class.java)
            val adapter = moshi.adapter<List<CrewMember>>(listType)
            val rawList = adapter.fromJson(json) ?: emptyList()
            rawList.map { crew ->
                if (crew.mobile.isNotBlank()) {
                    crew
                } else {
                    val phone = phoneMap[crew.name.trim().uppercase()]
                        ?: phoneMap[crew.name.replace(Regex("""\b(G-?12|COLP|\d+)\b""", RegexOption.IGNORE_CASE), "").trim().uppercase()]
                        ?: ""
                    crew.copy(mobile = phone)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getLobbies(): List<Lobby> = cachedLobbies

    fun getStations(): List<StationContact> = cachedStations

    fun getControlCenterLobby(): Lobby? {
        return cachedLobbies.find { it.code.equals("CTRL", ignoreCase = true) }
    }

    fun getKharsiaLobby(): Lobby? {
        return cachedLobbies.find { it.code.equals("KHS", ignoreCase = true) }
    }

    fun getOtherLobbies(): List<Lobby> {
        val cccMap = getLobbyCccMap()
        return cachedLobbies
            .filter { !it.code.equals("CTRL", ignoreCase = true) && !it.code.equals("KHS", ignoreCase = true) }
            .map { lobby ->
                val cccContacts = cccMap[lobby.code] ?: emptyList()
                if (cccContacts.isNotEmpty() && lobby.categories.none { it.category.contains("CCC", ignoreCase = true) }) {
                    val cccCategory = LobbyCategory(
                        category = "Crew Controlling Centre (CCC)",
                        contacts = cccContacts
                    )
                    lobby.copy(categories = listOf(cccCategory) + lobby.categories)
                } else {
                    lobby
                }
            }
    }

    fun getAllOtherLobbiesCrew(): List<OtherLobbyCrewItem> {
        val list = mutableListOf<OtherLobbyCrewItem>()
        for (lobby in getOtherLobbies()) {
            for (category in lobby.categories) {
                for (contact in category.contacts) {
                    val phone = if (contact.mobile.isNotBlank()) contact.mobile else contact.cug
                    list.add(
                        OtherLobbyCrewItem(
                            lobbyCode = lobby.code,
                            lobbyName = lobby.name,
                            category = category.category,
                            name = contact.name,
                            designation = contact.designation,
                            mobile = phone
                        )
                    )
                }
            }
        }
        return list
    }

    fun getKharsiaStaffItems(): List<KharsiaStaffItem> {
        val items = mutableListOf<KharsiaStaffItem>()

        // 1. Running Staff from crew master (LPG, TM, ALP)
        for (crew in cachedCrewMembers) {
            val cat = when {
                crew.category.equals("LP", ignoreCase = true) || (crew.designation.contains("LP", ignoreCase = true) && !crew.category.equals("ALP", ignoreCase = true)) -> "LPG"
                crew.category.equals("GUARD", ignoreCase = true) || crew.designation.contains("Guard", ignoreCase = true) || crew.designation.contains("Train Manager", ignoreCase = true) -> "TM"
                crew.category.equals("ALP", ignoreCase = true) || crew.designation.contains("ALP", ignoreCase = true) -> "ALP"
                else -> "LPG"
            }
            items.add(
                KharsiaStaffItem(
                    id = crew.crewId,
                    name = crew.name,
                    role = crew.designation,
                    category = cat,
                    subDetail = if (crew.cadre.isNotBlank()) "Cadre: ${crew.cadre}" else "",
                    mobile = crew.mobile,
                    badge = crew.crewId
                )
            )
        }

        // 2. Chief Loco Inspectors (CLI) - 12
        for (cli in getKharsiaClis()) {
            items.add(
                KharsiaStaffItem(
                    id = "CLI_${cli.id}",
                    name = cli.name,
                    role = "Chief Loco Inspector (CLI)",
                    category = "CLI",
                    subDetail = "Weekly Rest: ${cli.restDay}",
                    mobile = cli.mobile,
                    badge = "CLI-${cli.id}"
                )
            )
        }

        // 3. Jeep Drivers - 35
        getKharsiaJeepDrivers().forEachIndexed { idx, driver ->
            items.add(
                KharsiaStaffItem(
                    id = "DRIVER_${idx + 1}",
                    name = driver.name,
                    role = driver.designation,
                    category = "JEEP DRIVER",
                    subDetail = if (driver.cug.isNotBlank()) "Alt: ${driver.cug}" else "Lobby Attached",
                    mobile = driver.mobile,
                    altMobile = driver.cug,
                    badge = "DRIVER"
                )
            )
        }

        // 4. Loco Sanding Staff (Sander Boys) - 6
        for (sb in getKharsiaSanderBoys()) {
            items.add(
                KharsiaStaffItem(
                    id = "SANDER_${sb.id}",
                    name = sb.name,
                    role = "Loco Sanding Staff / Sander Boy",
                    category = "SANDER BOY",
                    subDetail = sb.shift,
                    mobile = sb.mobile,
                    badge = "SANDER"
                )
            )
        }

        // 5. CCC / CC - Kharsia Crew Controller
        val khsCccList = getLobbyCccMap()["KHS"] ?: emptyList()
        khsCccList.forEachIndexed { idx, ccc ->
            items.add(
                KharsiaStaffItem(
                    id = "CCC_KHS_${idx + 1}",
                    name = ccc.name,
                    role = ccc.designation,
                    category = "CCC",
                    subDetail = "Kharsia Lobby Control Desk",
                    mobile = ccc.mobile,
                    altMobile = ccc.cug,
                    badge = "CCC"
                )
            )
        }

        return items
    }

    fun getDivisionalControlCategories(): List<LobbyCategory> {
        val ctrl = getControlCenterLobby()
        val originalCategories = ctrl?.categories ?: emptyList()
        val result = mutableListOf<LobbyCategory>()

        // 1. Divisional Officers (Sr. DEE, DEE, ADEE, Sr. DME, DME, DOM)
        val officerContacts = listOf(
            StaffContact("Sr. DEE / OP / BSP", "Senior Divisional Electrical Engineer (OP)", "9752442004"),
            StaffContact("DEE / OP / BSP", "Divisional Electrical Engineer (OP)", "9752442005"),
            StaffContact("ADEE / OP / BSP", "Assistant Divisional Electrical Engineer (OP)", "9752442006"),
            StaffContact("Sr. DME / BSP", "Senior Divisional Mechanical Engineer", "9752442007"),
            StaffContact("DME / BSP", "Divisional Mechanical Engineer", "9752442008"),
            StaffContact("ADME / BSP", "Assistant Divisional Mechanical Engineer", "9752442009"),
            StaffContact("Sr. DOM / BSP", "Senior Divisional Operations Manager", "9752442010"),
            StaffContact("DOM / BSP", "Divisional Operations Manager", "9752442011"),
            StaffContact("Dy Chief Controller / BSP", "Deputy Chief Controller (Divisional Control)", "9752442003")
        )
        result.add(LobbyCategory("Divisional Officers", officerContacts))

        // 2. Power & Movement Control (TPC, DPC, Test Room, Safety)
        val powerMovement = listOf(
            StaffContact("TPC Bilaspur", "Traction Power Controller (24x7)", "9752442115"),
            StaffContact("Test Room / Telecom BSP", "Railway Telecom / Fault Repair", "9752442131"),
            StaffContact("Safety Control / DRM", "Emergency & Safety Helpline", "9752442120"),
            StaffContact("Central Control Room", "Divisional Control Operations", "9752442110")
        )
        result.add(LobbyCategory("Power & Movement Control", powerMovement))

        // 3. DPC Contacts
        val dpcCat = originalCategories.find { it.category.contains("DPC", ignoreCase = true) }
        if (dpcCat != null && dpcCat.contacts.isNotEmpty()) {
            result.add(dpcCat)
        }

        // 4. Lobby Emergency Landlines
        val landlinesCat = originalCategories.find { it.category.contains("Landline", ignoreCase = true) }
        if (landlinesCat != null && landlinesCat.contacts.isNotEmpty()) {
            result.add(landlinesCat)
        }

        return result
    }

    fun getEmergencyQuickContacts(): List<StaffContact> {
        return listOf(
            StaffContact("TLC Bilaspur", "Traction Loco Controller (24x7)", "9752442111", "9752442111"),
            StaffContact("TPC Bilaspur", "Traction Power Controller", "9752442115", "9752442115"),
            StaffContact("Chief Controller / CCC", "Divisional Control Office", "9752442110", "9752442110"),
            StaffContact("Kharsia Lobby Desk", "Lobby Master Landline", "07766276100", "07766276100"),
            StaffContact("Test Room / Telecom BSP", "Railway Telecom / Fault Repair", "9752442131", "9752442131"),
            StaffContact("Safety Control / DRM", "Emergency & Safety Helpline", "9752442120", "9752442120")
        )
    }

    private val cachedAllCrewMembers: List<CrewMember> by lazy {
        val result = mutableListOf<CrewMember>()
        // 1. Kharsia Crew Master
        result.addAll(cachedCrewMembers)

        // 2. All other lobbies crew
        for (lobby in getOtherLobbies()) {
            var counter = 1
            for (category in lobby.categories) {
                val catNormalized = when {
                    category.category.contains("Goods", ignoreCase = true) || category.category.contains("LP", ignoreCase = true) -> "LP"
                    category.category.contains("ALP", ignoreCase = true) || category.category.contains("Assistant", ignoreCase = true) -> "ALP"
                    category.category.contains("Guard", ignoreCase = true) || category.category.contains("Manager", ignoreCase = true) || category.category.contains("TM", ignoreCase = true) -> "GUARD"
                    category.category.contains("Shunting", ignoreCase = true) -> "SHUNTING"
                    category.category.contains("CLI", ignoreCase = true) -> "CLI"
                    category.category.contains("CCC", ignoreCase = true) -> "CCC"
                    else -> "LP"
                }
                for (contact in category.contacts) {
                    val phone = if (contact.mobile.isNotBlank()) contact.mobile else contact.cug
                    val generatedId = "${lobby.code}%03d".format(counter++)
                    result.add(
                        CrewMember(
                            crewId = generatedId,
                            name = contact.name,
                            designation = contact.designation,
                            category = catNormalized,
                            cadre = "${lobby.name} (${lobby.code})",
                            mobile = phone
                        )
                    )
                }
            }
        }
        result
    }

    fun getCrewMaster(): List<CrewMember> = cachedCrewMembers

    fun getAllCrewMaster(): List<CrewMember> = cachedAllCrewMembers

    fun findCrewById(crewId: String): CrewMember? {
        val trimmed = crewId.trim().uppercase()
        if (trimmed.isEmpty()) return null

        // 1. Search Kharsia crew first
        cachedCrewMembers.find { it.crewId.equals(trimmed, ignoreCase = true) }?.let { return it }

        if (trimmed.all { it.isDigit() }) {
            val withPrefix = "KHS$trimmed"
            cachedCrewMembers.find { it.crewId.equals(withPrefix, ignoreCase = true) }?.let { return it }
        }

        cachedCrewMembers.find { it.crewId.endsWith(trimmed, ignoreCase = true) }?.let { return it }

        // 2. Search other lobbies crew by ID (e.g. BSP001, RIG010) or mobile
        cachedAllCrewMembers.find { it.crewId.equals(trimmed, ignoreCase = true) }?.let { return it }
        cachedAllCrewMembers.find { it.mobile.isNotBlank() && it.mobile == trimmed }?.let { return it }

        return null
    }

    fun findCrewByName(name: String): CrewMember? {
        val trimmed = name.trim()
        if (trimmed.length < 3) return null

        // Exact match in Kharsia
        cachedCrewMembers.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }
        // Exact match in all lobbies
        cachedAllCrewMembers.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }

        // Contains match in Kharsia
        val khsMatches = cachedCrewMembers.filter { it.name.contains(trimmed, ignoreCase = true) }
        if (khsMatches.size == 1) return khsMatches.first()

        // Contains match in all lobbies
        val allMatches = cachedAllCrewMembers.filter { it.name.contains(trimmed, ignoreCase = true) }
        if (allMatches.isNotEmpty()) return allMatches.first()

        return null
    }

    fun findCrew(query: String): CrewMember? {
        return findCrewById(query) ?: findCrewByName(query)
    }

    fun searchCrew(query: String, limit: Int = 30): List<CrewMember> {
        val q = query.trim().uppercase()
        if (q.isEmpty()) return cachedCrewMembers.take(limit)

        return cachedAllCrewMembers.filter { crew ->
            crew.crewId.uppercase().contains(q) ||
            crew.name.uppercase().contains(q) ||
            crew.designation.uppercase().contains(q) ||
            crew.category.uppercase().contains(q) ||
            crew.cadre.uppercase().contains(q) ||
            crew.mobile.contains(q)
        }.sortedWith(
            compareBy<CrewMember> {
                when {
                    it.crewId.uppercase() == q -> 0
                    it.crewId.uppercase().startsWith(q) -> 1
                    it.name.uppercase().startsWith(q) -> 2
                    it.name.uppercase().contains(q) -> 3
                    else -> 4
                }
            }
        ).take(limit)
    }

    // =========================================================================
    // KHARSIA CHIEF LOCO INSPECTORS (CLI) - 12 (FROM OFFICIAL ROSTER PDF)
    // =========================================================================
    fun getKharsiaClis(): List<CliContact> {
        return listOf(
            CliContact(1, "CLI SHRI VIJAY KUMAR", "MONDAY", "9752442527"),
            CliContact(2, "CLI SHRI M. ANAND RAO", "TUESDAY", "9752441206"),
            CliContact(3, "CLI SHRI K. N. VERMA", "TUESDAY", "9752442765"),
            CliContact(4, "CLI SHRI D. K. YADAV", "WEDNESDAY", "9752441423"),
            CliContact(5, "CLI SHRI NEEMESH TURKANE", "WEDNESDAY", "9752442747"),
            CliContact(6, "CLI SHRI SATYADEV", "THURSDAY", "7225020871"),
            CliContact(7, "CLI SHRI TRIBHUWAN", "THURSDAY", "9752441912"),
            CliContact(8, "CLI SHRI S. K. BAGHMAR", "FRIDAY", "9752442338"),
            CliContact(9, "CLI SHRI KULDEEP", "SATURDAY", "9752442139"),
            CliContact(10, "CLI SHRI M. LAXMAN RAO", "SATURDAY", "9752491759"),
            CliContact(11, "CLI SHRI L. K. SHARAFF", "SUNDAY", "9752442147"),
            CliContact(12, "CLI SHRI LAXMIKANT SAHU", "SUNDAY", "9752442786")
        )
    }

    // =========================================================================
    // KHARSIA LOCO SANDING STAFF (SANDER BOYS) - 6 ACROSS 3 SHIFTS
    // =========================================================================
    fun getKharsiaSanderBoys(): List<SanderBoyContact> {
        return listOf(
            SanderBoyContact(1, "Shift 1 (00:00 - 08:00)", "Kishan Kumar", "6266056927"),
            SanderBoyContact(2, "Shift 1 (00:00 - 08:00)", "Mukul Kumar", "9399675597"),
            SanderBoyContact(3, "Shift 2 (08:00 - 16:00)", "Umang / Tarang", "7470744231"),
            SanderBoyContact(4, "Shift 2 (08:00 - 16:00)", "Vinay", "8770252740"),
            SanderBoyContact(5, "Shift 3 (16:00 - 00:00)", "Dharam", "9131815191"),
            SanderBoyContact(6, "Shift 3 (16:00 - 00:00)", "Rihant", "7389656758")
        )
    }

    // =========================================================================
    // KHARSIA JEEP DRIVERS - 35 (FROM JEEP DRIVER CONTACT DIRECTORY PDF)
    // =========================================================================
    fun getKharsiaJeepDrivers(): List<StaffContact> {
        return listOf(
            StaffContact("Rajendra Patel", "Jeep Driver (01)", "6265395782", "9343764095"),
            StaffContact("Deepak Sahu", "Jeep Driver (02)", "8817361305"),
            StaffContact("Sagar", "Jeep Driver (03)", "9770612511"),
            StaffContact("Kundan", "Jeep Driver (04)", "7000592530"),
            StaffContact("Yuval (Golu)", "Jeep Driver (05)", "8839384199"),
            StaffContact("Bhag Singh", "Jeep Driver (06)", "9081175369"),
            StaffContact("Assem / Sushil Kumar", "Jeep Driver (07)", "8450865891", "9202797597"),
            StaffContact("Anurag", "Jeep Driver (08)", "6261445934"),
            StaffContact("Pintu Chouhan", "Jeep Driver (09)", "7970256421"),
            StaffContact("Nihal", "Jeep Driver (10)", "9201415930"),
            StaffContact("Jayant", "Jeep Driver (11)", "7415365109"),
            StaffContact("Yogesh", "Jeep Driver (12)", "9575904597"),
            StaffContact("Dilip Kumar", "Jeep Driver (13)", "8817842929"),
            StaffContact("Dayanand", "Jeep Driver (14)", "8839976221"),
            StaffContact("Shiv Nishad", "Jeep Driver (15)", "8358082886"),
            StaffContact("Ravi Rathia (1)", "Jeep Driver (16)", "7000468629"),
            StaffContact("Pappu Chouhan", "Jeep Driver (17)", "7999810631"),
            StaffContact("Goldu / Golu", "Jeep Driver (18)", "7724050115"),
            StaffContact("Arjun", "Jeep Driver (19)", "9340504813"),
            StaffContact("Pankaj", "Jeep Driver (20)", "6204069455"),
            StaffContact("Rajesh", "Jeep Driver (21)", "7898468217"),
            StaffContact("Surya Prakash", "Jeep Driver (22)", "7898468217"),
            StaffContact("Nilesh", "Jeep Driver (23)", "6264965208"),
            StaffContact("Ganesh (GS)", "Jeep Driver (24)", "9201525083"),
            StaffContact("Deepak Chouhan", "Jeep Driver (25)", "9111058832"),
            StaffContact("Manish Rathiya", "Jeep Driver (26)", "8305660080"),
            StaffContact("Ravi Rathia (2)", "Jeep Driver (27)", "7724050010", "7000468629"),
            StaffContact("Mukesh", "Jeep Driver (28)", "8629994690"),
            StaffContact("Prakash", "Jeep Driver (29)", "9243737179"),
            StaffContact("Sanjay", "Jeep Driver (30)", "7999787360"),
            StaffContact("Jitendra Chouhan", "Jeep Driver (31)", "9343935824"),
            StaffContact("Ayshu", "Jeep Driver (32)", "9202797597"),
            StaffContact("Sahu Kumar", "Jeep Driver (33)", "7879858764"),
            StaffContact("Sanu", "Jeep Driver (34)", "7878858764", "7879858764"),
            StaffContact("Devansh", "Jeep Driver (35)", "6262815619")
        )
    }

    // =========================================================================
    // BSP TRACTION LOCO CONTROLLER (TLC) DIRECTORY - 44 (FROM TLC PDF)
    // =========================================================================
    fun getTlcContacts(): List<TlcContact> {
        return listOf(
            TlcContact(1, "A K Verma", "9752441935"),
            TlcContact(2, "AKesh Verma", "9752441935"),
            TlcContact(3, "Anurag Mishra", "9131048018"),
            TlcContact(4, "Ashish Mishra", "9752442422"),
            TlcContact(5, "AT Mukharjee", "9752442848"),
            TlcContact(6, "Atul Kr Parate", "9752441515"),
            TlcContact(7, "B Ravikant", "9752441363"),
            TlcContact(8, "Bhanu Prakash", "9752491733"),
            TlcContact(9, "Bhaskar Das", "9752444206"),
            TlcContact(10, "BK Yadav", "9752442191"),
            TlcContact(11, "C K Dubey", "9777575469"),
            TlcContact(12, "C S Kurrey", "9752441736"),
            TlcContact(13, "CL Dewangan", "9752442768"),
            TlcContact(14, "HK Lasher", "9752442692"),
            TlcContact(15, "HSS Sharma", "9752442795"),
            TlcContact(16, "Jitendra Naidu", "9753444239"),
            TlcContact(17, "K Sai Kumar", "9752441362"),
            TlcContact(18, "Lala Ram Sahu", "9752435349"),
            TlcContact(19, "M Saleem Khan", "9752442202"),
            TlcContact(20, "Manish Kumar", "9752442593"),
            TlcContact(21, "Manish Kumar 2", "9752441929"),
            TlcContact(22, "Manoj Kumar Sharma", "9752442480"),
            TlcContact(23, "Nagendra Kumar", "9752491186"),
            TlcContact(24, "P K Nangal", "9752441354"),
            TlcContact(25, "P R Chakraborty", "9752442850"),
            TlcContact(26, "P R Pandit", "9752441632"),
            TlcContact(27, "Pankaj Kr Singh", "9752491729"),
            TlcContact(28, "Pankaj Kumar", "9522221848"),
            TlcContact(29, "R B Ray", "9752442457"),
            TlcContact(30, "R K Mishra", "9752441359"),
            TlcContact(31, "Rahul Kaushik", "9752441356"),
            TlcContact(32, "Ravikant", "8982121892"),
            TlcContact(33, "Rodrizg James", "9752491738"),
            TlcContact(34, "S Bhattacharge", "9752444221"),
            TlcContact(35, "S K Gupta", "9752593395"),
            TlcContact(36, "S K Yadav", "9752441984"),
            TlcContact(37, "S O Dubey", "9752441938"),
            TlcContact(38, "Santosh Kumar", "9752441494"),
            TlcContact(39, "Sanyasi Rao", "9752598193"),
            TlcContact(40, "Seema Tolwani", "9752442334"),
            TlcContact(41, "Sudhir Choudhary", "9752442727"),
            TlcContact(42, "U K Yadav", "8085956315"),
            TlcContact(43, "Vishweshwar Kumar", "9752491206"),
            TlcContact(44, "VK Sharma", "9752441913"),
            TlcContact(45, "TLC Emergency Desk (BSP)", "99810501154", "Emergency Desk"),
            TlcContact(46, "TLC Emergency Desk (CKP)", "9771444598", "Emergency Desk")
        )
    }

    // =========================================================================
    // CCC (CREW CONTROLLING CENTRE) PER LOBBY
    // =========================================================================
    fun getLobbyCccMap(): Map<String, List<StaffContact>> {
        return mapOf(
            "KHS" to listOf(
                StaffContact("L.K. Sahu", "CCC In-Charge (Kharsia)", "9752442786"),
                StaffContact("V.K. Chandra", "CCG (Kharsia)", "9752442023"),
                StaffContact("Kharsia Lobby Desk", "Lobby Master Landline", "07767281220", "58820")
            ),
            "BSP" to listOf(
                StaffContact("L.D. Diwan", "CCG (Bilaspur)", "9752441952"),
                StaffContact("P.N. Rao", "CCC (Bilaspur)", "9752441439")
            ),
            "RIG" to listOf(
                StaffContact("P.K. Gupta", "CCC (Raigarh)", "9752442164"),
                StaffContact("Manish Kumar", "CCG (Raigarh)", "9752448724")
            ),
            "BRJN" to listOf(
                StaffContact("Mukesh Kumar", "CCG (Brajrajnagar)", "9752442686"),
                StaffContact("Rajesh", "CC (Brajrajnagar)", "9777575989"),
                StaffContact("Sanjay Giri", "CCC (Brajrajnagar)", "9777575329")
            ),
            "KRBA" to listOf(
                StaffContact("Akhilesh Sahu", "CCG (Korba)", "9752442705"),
                StaffContact("H.K. Mahor", "CCC (Korba)", "9752442817")
            ),
            "PND" to listOf(
                StaffContact("Abdul Samad Gauri", "CCC (Pendra Road)", "9777575446")
            ),
            "SDL" to listOf(
                StaffContact("Kamlesh Verma", "CCC (Saraidih/SDL)", "9752442520")
            ),
            "DBEC" to listOf(
                StaffContact("Kashyap", "CCC (DBEC)", "9752443905")
            ),
            "BYT" to listOf(
                StaffContact("S.K. Dewangan", "CCC (Bhatapara)", "9752442839")
            ),
            "BJRI" to listOf(
                StaffContact("Tanveer Alam", "CCC (Bhilai/BJRI)", "9752442540")
            ),
            "AKT" to listOf(
                StaffContact("B.S. Raju", "CCC (Akaltara)", "9752441575")
            ),
            "JSG" to listOf(
                StaffContact("H.S. Swain", "CCC (Jharsuguda)", "9777582335"),
                StaffContact("M. Ali", "CCC (Jharsuguda)", "9777582620")
            ),
            "USL" to listOf(
                StaffContact("R.K. Swarnakar", "CCC (Urkura/USL)", "9752442331")
            ),
            "SJQ" to listOf(
                StaffContact("Krishnandan", "CC (SJQ)", "9752441680")
            )
        )
    }
}
