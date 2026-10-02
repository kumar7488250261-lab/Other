package com.example.data.jeep

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JeepRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kharsia_jeep_di_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val tripListType = Types.newParameterizedType(List::class.java, JeepTripRecord::class.java)
    private val tripAdapter = moshi.adapter<List<JeepTripRecord>>(tripListType)

    private val vehicleListType = Types.newParameterizedType(List::class.java, JeepVehicle::class.java)
    private val vehicleAdapter = moshi.adapter<List<JeepVehicle>>(vehicleListType)

    private val driverListType = Types.newParameterizedType(List::class.java, JeepDriver::class.java)
    private val driverAdapter = moshi.adapter<List<JeepDriver>>(driverListType)

    private val breakdownListType = Types.newParameterizedType(List::class.java, JeepBreakdownRecord::class.java)
    private val breakdownAdapter = moshi.adapter<List<JeepBreakdownRecord>>(breakdownListType)

    private val diDutyAdapter = moshi.adapter(JeepDiDuty::class.java)

    val standardJeepNumbers = listOf("89", "89(II)", "79", "31", "22", "91")

    // In-memory caches
    private var cachedTrips: MutableList<JeepTripRecord>? = null
    private var cachedVehicles: MutableList<JeepVehicle>? = null
    private var cachedDrivers: MutableList<JeepDriver>? = null
    private var cachedBreakdowns: MutableList<JeepBreakdownRecord>? = null
    private var cachedDiDuty: JeepDiDuty? = null

    init {
        loadDrivers()
        loadVehicles()
        loadTrips()
        loadBreakdowns()
        loadDiDuty()
    }

    // =========================================================================
    // SHIFT & JEEP DI MANAGEMENT
    // =========================================================================
    fun getCurrentShift(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..13 -> "06:00 - 14:00"
            in 14..21 -> "14:00 - 22:00"
            else -> "22:00 - 06:00"
        }
    }

    fun getJeepDiDuty(): JeepDiDuty {
        if (cachedDiDuty == null) {
            loadDiDuty()
        }
        val currentShift = getCurrentShift()
        val duty = cachedDiDuty ?: JeepDiDuty(
            shift = currentShift,
            diCrewId = "KHS1001",
            diName = "ROHIT KU KURRE",
            diDesignation = "LPG - Loco Pilot (Goods)",
            dutyDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )
        return duty
    }

    fun saveJeepDiDuty(duty: JeepDiDuty) {
        cachedDiDuty = duty
        try {
            val json = diDutyAdapter.toJson(duty)
            prefs.edit().putString("key_jeep_di_duty", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDiDuty() {
        val json = prefs.getString("key_jeep_di_duty", null)
        if (!json.isNullOrBlank()) {
            try {
                cachedDiDuty = diDutyAdapter.fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (cachedDiDuty == null) {
            cachedDiDuty = JeepDiDuty(
                shift = getCurrentShift(),
                diCrewId = "KHS1001",
                diName = "ROHIT KU KURRE",
                diDesignation = "LPG - Loco Pilot (Goods)",
                dutyDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )
        }
    }

    // =========================================================================
    // JEEP DRIVERS DIRECTORY (35 DRIVERS FROM PDF)
    // =========================================================================
    fun getDrivers(): List<JeepDriver> {
        if (cachedDrivers == null) {
            loadDrivers()
        }
        return cachedDrivers ?: emptyList()
    }

    fun findDriverByName(name: String): JeepDriver? {
        val q = name.trim().lowercase()
        return getDrivers().firstOrNull { it.name.lowercase().contains(q) }
    }

    fun addOrUpdateDriver(driver: JeepDriver) {
        val list = getDrivers().toMutableList()
        val idx = list.indexOfFirst { it.name.equals(driver.name, ignoreCase = true) || it.id == driver.id }
        if (idx >= 0) {
            list[idx] = driver
        } else {
            list.add(driver)
        }
        cachedDrivers = list
        persistDrivers(list)
    }

    private fun loadDrivers() {
        val json = prefs.getString("key_jeep_drivers", null)
        if (!json.isNullOrBlank()) {
            try {
                val list = driverAdapter.fromJson(json)
                if (!list.isNullOrEmpty()) {
                    cachedDrivers = list.toMutableList()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val seed = getSeedDrivers()
        cachedDrivers = seed.toMutableList()
        persistDrivers(seed)
    }

    private fun persistDrivers(list: List<JeepDriver>) {
        try {
            val json = driverAdapter.toJson(list)
            prefs.edit().putString("key_jeep_drivers", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSeedDrivers(): List<JeepDriver> {
        return listOf(
            JeepDriver(name = "Rajendra Patel", mobile = "6265395782", altMobile = "9343764095"),
            JeepDriver(name = "Deepak Sahu", mobile = "8817361305"),
            JeepDriver(name = "Sagar", mobile = "9770612511"),
            JeepDriver(name = "Kundan", mobile = "7000592530"),
            JeepDriver(name = "Yuval (Golu)", mobile = "8839384199"),
            JeepDriver(name = "Bhag Singh", mobile = "9081175369"),
            JeepDriver(name = "Assem / Sushil Kumar", mobile = "8450865891", altMobile = "9202797597"),
            JeepDriver(name = "Anurag", mobile = "6261445934"),
            JeepDriver(name = "Pintu Chouhan", mobile = "7970256421"),
            JeepDriver(name = "Nihal", mobile = "9201415930"),
            JeepDriver(name = "Jayant", mobile = "7415365109"),
            JeepDriver(name = "Yogesh", mobile = "9575904597"),
            JeepDriver(name = "Dilip Kumar", mobile = "8817842929"),
            JeepDriver(name = "Dayanand", mobile = "8839976221"),
            JeepDriver(name = "Shiv Nishad", mobile = "8358082886"),
            JeepDriver(name = "Ravi Rathia (1)", mobile = "7000468629"),
            JeepDriver(name = "Pappu Chouhan", mobile = "7999810631"),
            JeepDriver(name = "Goldu / Golu", mobile = "7724050115"),
            JeepDriver(name = "Arjun", mobile = "9340504813"),
            JeepDriver(name = "Pankaj", mobile = "6204069455"),
            JeepDriver(name = "Rajesh", mobile = "7898468217"),
            JeepDriver(name = "Surya Prakash (Page 2)", mobile = "7898468217"),
            JeepDriver(name = "Nilesh", mobile = "6264965208"),
            JeepDriver(name = "Ganesh (GS)", mobile = "9201525083"),
            JeepDriver(name = "Deepak Chouhan", mobile = "9111058832"),
            JeepDriver(name = "Manish Rathiya", mobile = "8305660080"),
            JeepDriver(name = "Ravi Rathia (2)", mobile = "7724050010", altMobile = "7000468629"),
            JeepDriver(name = "Mukesh", mobile = "8629994690"),
            JeepDriver(name = "Prakash", mobile = "9243737179"),
            JeepDriver(name = "Sanjay", mobile = "7999787360"),
            JeepDriver(name = "Jitendra Chouhan", mobile = "9343935824"),
            JeepDriver(name = "Ayshu", mobile = "9202797597"),
            JeepDriver(name = "Sahu Kumar", mobile = "7879858764"),
            JeepDriver(name = "Sanu (Page 2)", mobile = "7878858764", altMobile = "7879858764"),
            JeepDriver(name = "Devansh", mobile = "6262815619")
        )
    }

    // =========================================================================
    // JEEP VEHICLES & FIFO QUEUE AT KHARSIA LOBBY
    // =========================================================================
    fun getVehicles(): List<JeepVehicle> {
        if (cachedVehicles == null) {
            loadVehicles()
        }
        return cachedVehicles ?: emptyList()
    }

    /**
     * Returns available Jeeps at Kharsia Lobby sorted by FIFO (first arrived at lobby is first out).
     */
    fun getAvailableJeepsFifo(): List<JeepVehicle> {
        return getVehicles()
            .filter { it.status == "AVAILABLE" }
            .sortedBy { it.lobbyArrivalTime }
    }

    fun getOnTripJeeps(): List<JeepVehicle> {
        return getVehicles().filter { it.status == "ON_TRIP" }
    }

    fun getBreakdownJeeps(): List<JeepVehicle> {
        return getVehicles().filter { it.status == "BREAKDOWN" }
    }

    private fun loadVehicles() {
        val json = prefs.getString("key_jeep_vehicles", null)
        if (!json.isNullOrBlank()) {
            try {
                val list = vehicleAdapter.fromJson(json)
                if (!list.isNullOrEmpty()) {
                    cachedVehicles = list.toMutableList()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val seed = getSeedVehicles()
        cachedVehicles = seed.toMutableList()
        persistVehicles(seed)
    }

    private fun persistVehicles(list: List<JeepVehicle>) {
        try {
            val json = vehicleAdapter.toJson(list)
            prefs.edit().putString("key_jeep_vehicles", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSeedVehicles(): List<JeepVehicle> {
        return listOf(
            JeepVehicle(
                vehicleNo = "89",
                defaultDriverName = "Rajendra Patel",
                defaultDriverMobile = "6265395782",
                status = "AVAILABLE",
                lobbyArrivalTime = "07:15"
            ),
            JeepVehicle(
                vehicleNo = "89(II)",
                defaultDriverName = "Deepak Sahu",
                defaultDriverMobile = "8817361305",
                status = "AVAILABLE",
                lobbyArrivalTime = "08:00"
            ),
            JeepVehicle(
                vehicleNo = "79",
                defaultDriverName = "Sagar",
                defaultDriverMobile = "9770612511",
                status = "AVAILABLE",
                lobbyArrivalTime = "08:45"
            ),
            JeepVehicle(
                vehicleNo = "31",
                defaultDriverName = "Kundan",
                defaultDriverMobile = "7000592530",
                status = "AVAILABLE",
                lobbyArrivalTime = "09:30"
            ),
            JeepVehicle(
                vehicleNo = "22",
                defaultDriverName = "Yuval (Golu)",
                defaultDriverMobile = "8839384199",
                status = "ON_TRIP",
                currentDestination = "ROB (Robertson)",
                lastDispatchedTime = "11:20"
            ),
            JeepVehicle(
                vehicleNo = "91",
                defaultDriverName = "Bhag Singh",
                defaultDriverMobile = "9081175369",
                status = "AVAILABLE",
                lobbyArrivalTime = "10:15"
            )
        )
    }

    // =========================================================================
    // JEEP MOVEMENT & TRIPS
    // =========================================================================
    fun getTrips(): List<JeepTripRecord> {
        if (cachedTrips == null) {
            loadTrips()
        }
        return cachedTrips ?: emptyList()
    }

    fun getActiveTrips(): List<JeepTripRecord> {
        return getTrips().filter { it.status == "ON_TRIP" }
    }

    fun dispatchJeep(trip: JeepTripRecord): Boolean {
        try {
            val tripList = getTrips().toMutableList()
            tripList.add(0, trip)
            cachedTrips = tripList
            persistTrips(tripList)

            // Update Vehicle Status to ON_TRIP
            val vehicleList = getVehicles().toMutableList()
            val vIdx = vehicleList.indexOfFirst { it.vehicleNo == trip.vehicleNo }
            if (vIdx >= 0) {
                vehicleList[vIdx] = vehicleList[vIdx].copy(
                    status = "ON_TRIP",
                    currentDestination = "${trip.toStationCode} (${trip.toStationName})",
                    lastDispatchedTime = trip.departureTime,
                    defaultDriverName = trip.driverName,
                    defaultDriverMobile = trip.driverMobile
                )
            } else {
                vehicleList.add(
                    JeepVehicle(
                        vehicleNo = trip.vehicleNo,
                        defaultDriverName = trip.driverName,
                        defaultDriverMobile = trip.driverMobile,
                        status = "ON_TRIP",
                        currentDestination = "${trip.toStationCode} (${trip.toStationName})",
                        lastDispatchedTime = trip.departureTime
                    )
                )
            }
            cachedVehicles = vehicleList
            persistVehicles(vehicleList)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * When Jeep arrives back at Kharsia Lobby:
     * - Record Arrival Time at Kharsia
     * - Record Returning Crew info (if any)
     * - Mark trip as COMPLETED
     * - Put vehicle back in AVAILABLE state with new arrival time for FIFO queuing!
     */
    fun recordArrival(
        tripId: String,
        arrivalTime: String,
        returningCrewId: String = "",
        returningCrewName: String = "",
        returningCrewDesig: String = "",
        returningOtherCrew: String = "",
        returningFromStationCode: String = "",
        returningDepartureTime: String = "",
        remarks: String = ""
    ): Boolean {
        try {
            val tripList = getTrips().toMutableList()
            val tIdx = tripList.indexOfFirst { it.id == tripId }
            var vehicleNoToUpdate = ""

            if (tIdx >= 0) {
                val existing = tripList[tIdx]
                vehicleNoToUpdate = existing.vehicleNo
                tripList[tIdx] = existing.copy(
                    status = "COMPLETED",
                    arrivalAtKharsiaTime = arrivalTime,
                    returningCrewId = returningCrewId,
                    returningCrewName = returningCrewName,
                    returningCrewDesig = returningCrewDesig,
                    returningOtherCrew = returningOtherCrew,
                    returningFromStationCode = returningFromStationCode.ifBlank { existing.toStationCode },
                    returningDepartureTime = returningDepartureTime,
                    remarks = remarks
                )
                cachedTrips = tripList
                persistTrips(tripList)
            }

            if (vehicleNoToUpdate.isNotBlank()) {
                val vehicleList = getVehicles().toMutableList()
                val vIdx = vehicleList.indexOfFirst { it.vehicleNo == vehicleNoToUpdate }
                if (vIdx >= 0) {
                    vehicleList[vIdx] = vehicleList[vIdx].copy(
                        status = "AVAILABLE",
                        lobbyArrivalTime = arrivalTime,
                        currentDestination = "",
                        breakdownStartTime = "",
                        breakdownEndTime = "",
                        breakdownReason = ""
                    )
                    cachedVehicles = vehicleList
                    persistVehicles(vehicleList)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Direct Quick Arrival for a Vehicle (if no open trip selected)
    fun quickVehicleArrival(vehicleNo: String, arrivalTime: String): Boolean {
        try {
            val vehicleList = getVehicles().toMutableList()
            val vIdx = vehicleList.indexOfFirst { it.vehicleNo == vehicleNo }
            if (vIdx >= 0) {
                vehicleList[vIdx] = vehicleList[vIdx].copy(
                    status = "AVAILABLE",
                    lobbyArrivalTime = arrivalTime,
                    currentDestination = ""
                )
                cachedVehicles = vehicleList
                persistVehicles(vehicleList)
            }

            // Also close any active trip for this vehicle
            val tripList = getTrips().toMutableList()
            val tIdx = tripList.indexOfFirst { it.vehicleNo == vehicleNo && it.status == "ON_TRIP" }
            if (tIdx >= 0) {
                tripList[tIdx] = tripList[tIdx].copy(
                    status = "COMPLETED",
                    arrivalAtKharsiaTime = arrivalTime
                )
                cachedTrips = tripList
                persistTrips(tripList)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // =========================================================================
    // JEEP BREAKDOWN TRACKING
    // =========================================================================
    fun getBreakdowns(): List<JeepBreakdownRecord> {
        if (cachedBreakdowns == null) {
            loadBreakdowns()
        }
        return cachedBreakdowns ?: emptyList()
    }

    fun reportBreakdown(
        vehicleNo: String,
        driverName: String,
        startTime: String,
        startDate: String,
        reason: String,
        reportedBy: String
    ): Boolean {
        try {
            val list = getBreakdowns().toMutableList()
            val record = JeepBreakdownRecord(
                vehicleNo = vehicleNo,
                driverName = driverName,
                breakdownStartTime = startTime,
                breakdownStartDate = startDate,
                reason = reason,
                reportedBy = reportedBy,
                isRestored = false
            )
            list.add(0, record)
            cachedBreakdowns = list
            persistBreakdowns(list)

            // Mark vehicle as BREAKDOWN
            val vehicleList = getVehicles().toMutableList()
            val vIdx = vehicleList.indexOfFirst { it.vehicleNo == vehicleNo }
            if (vIdx >= 0) {
                vehicleList[vIdx] = vehicleList[vIdx].copy(
                    status = "BREAKDOWN",
                    breakdownStartTime = "$startDate $startTime",
                    breakdownReason = reason
                )
                cachedVehicles = vehicleList
                persistVehicles(vehicleList)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun resolveBreakdown(
        breakdownId: String,
        restoredTime: String
    ): Boolean {
        try {
            val list = getBreakdowns().toMutableList()
            val idx = list.indexOfFirst { it.id == breakdownId }
            var vehicleNo = ""
            if (idx >= 0) {
                vehicleNo = list[idx].vehicleNo
                list[idx] = list[idx].copy(
                    breakdownEndTime = restoredTime,
                    isRestored = true
                )
                cachedBreakdowns = list
                persistBreakdowns(list)
            }

            if (vehicleNo.isNotBlank()) {
                val vehicleList = getVehicles().toMutableList()
                val vIdx = vehicleList.indexOfFirst { it.vehicleNo == vehicleNo }
                if (vIdx >= 0) {
                    vehicleList[vIdx] = vehicleList[vIdx].copy(
                        status = "AVAILABLE",
                        lobbyArrivalTime = restoredTime,
                        breakdownEndTime = restoredTime,
                        breakdownReason = ""
                    )
                    cachedVehicles = vehicleList
                    persistVehicles(vehicleList)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun loadTrips() {
        val json = prefs.getString("key_jeep_trips", null)
        if (!json.isNullOrBlank()) {
            try {
                val list = tripAdapter.fromJson(json)
                if (!list.isNullOrEmpty()) {
                    cachedTrips = list.toMutableList()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val seed = getSeedTrips()
        cachedTrips = seed.toMutableList()
        persistTrips(seed)
    }

    private fun persistTrips(list: List<JeepTripRecord>) {
        try {
            val json = tripAdapter.toJson(list)
            prefs.edit().putString("key_jeep_trips", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBreakdowns() {
        val json = prefs.getString("key_jeep_breakdowns", null)
        if (!json.isNullOrBlank()) {
            try {
                val list = breakdownAdapter.fromJson(json)
                if (!list.isNullOrEmpty()) {
                    cachedBreakdowns = list.toMutableList()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cachedBreakdowns = mutableListOf()
    }

    private fun persistBreakdowns(list: List<JeepBreakdownRecord>) {
        try {
            val json = breakdownAdapter.toJson(list)
            prefs.edit().putString("key_jeep_breakdowns", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSeedTrips(): List<JeepTripRecord> {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        return listOf(
            JeepTripRecord(
                vehicleNo = "22",
                driverName = "Yuval (Golu)",
                driverMobile = "8839384199",
                jeepDiName = "ROHIT KU KURRE (LPG)",
                shift = "14:00 - 22:00",
                fromStationCode = "KHS",
                fromStationName = "Kharsia Lobby",
                departureTime = "11:20",
                departureDate = today,
                toStationCode = "ROB",
                toStationName = "Robertson",
                outgoingCrewId = "KHS1006",
                outgoingCrewName = "RAKESH KR.RAJAK",
                outgoingCrewDesig = "SALP",
                status = "ON_TRIP"
            ),
            JeepTripRecord(
                vehicleNo = "89",
                driverName = "Rajendra Patel",
                driverMobile = "6265395782",
                jeepDiName = "ROHIT KU KURRE (LPG)",
                shift = "06:00 - 14:00",
                fromStationCode = "KHS",
                fromStationName = "Kharsia Lobby",
                departureTime = "06:40",
                departureDate = today,
                toStationCode = "CPH",
                toStationName = "Champa",
                outgoingCrewId = "KHS1008",
                outgoingCrewName = "NITISH KUMAR",
                outgoingCrewDesig = "SALP",
                status = "COMPLETED",
                arrivalAtKharsiaTime = "09:10",
                returningCrewId = "KHS1020",
                returningCrewName = "VIDYASAGAR KUMAR",
                returningCrewDesig = "SALP",
                returningFromStationCode = "CPH",
                returningDepartureTime = "08:15"
            )
        )
    }

    // =========================================================================
    // STATIONS AND SIDINGS DIRECTORY (ALL USER REQUESTED CODES)
    // =========================================================================
    fun getStationCodes(): List<StationCodeItem> {
        return listOf(
            // Primary lobby
            StationCodeItem("KHS", "Kharsia Lobby", "MAINLINE"),

            // User's specified Sidings and Stations
            StationCodeItem("VWLR", "VWLR Siding", "SIDING"),
            StationCodeItem("DBPR", "DBPR Siding", "SIDING"),
            StationCodeItem("BEMR", "BEMR Siding", "SIDING"),
            StationCodeItem("ROB", "Robertson", "MAINLINE"),
            StationCodeItem("BEF", "Bhupdeopur", "MAINLINE"),
            StationCodeItem("VIMLA", "Vimla Infrastructure Siding", "SIDING"),
            StationCodeItem("MONET", "Monnet Ispat & Energy Siding", "SIDING"),
            StationCodeItem("RIG", "Raigarh", "MAINLINE"),
            StationCodeItem("KDTR", "Kotarlia", "MAINLINE"),
            StationCodeItem("GURA", "Gurda", "MAINLINE"),
            StationCodeItem("CHHL", "Chhal Colliery", "SIDING"),
            StationCodeItem("CHHL HOLDING YARD", "Chhal Holding Yard", "SIDING"),
            StationCodeItem("CHHL SILO", "Chhal Silo (SLCC)", "SIDING"),
            StationCodeItem("GGDA", "Gharghoda", "SIDING"),
            StationCodeItem("BUMA", "Bhalumuda", "SIDING"),
            StationCodeItem("KCHP", "Karichapar", "SIDING"),
            StationCodeItem("BOMK", "Bomek Siding", "SIDING"),
            StationCodeItem("BAROD", "Baroud Colliery / Siding", "SIDING"),
            StationCodeItem("DMJG", "Dharmjaygarh", "SIDING"),
            StationCodeItem("JDI", "Jharradih", "MAINLINE"),
            StationCodeItem("SKT", "Sakti", "MAINLINE"),
            StationCodeItem("BUA", "Baraduar", "MAINLINE"),
            StationCodeItem("SGRD", "Saragaon", "MAINLINE"),
            StationCodeItem("CPH", "Champa", "MAINLINE"),

            // Kharsia to Bilaspur (BSP) Route Stations
            StationCodeItem("NIA", "Janjgir Naila", "MAINLINE"),
            StationCodeItem("KP", "Kotmi Sonar", "MAINLINE"),
            StationCodeItem("AKT", "Akaltara", "MAINLINE"),
            StationCodeItem("JRMG", "Jairamnagar", "MAINLINE"),
            StationCodeItem("GTW", "Gatora", "MAINLINE"),
            StationCodeItem("BSP", "Bilaspur Jn", "MAINLINE"),
            StationCodeItem("KRBA", "Korba", "BRANCH"),
            StationCodeItem("GAD", "Gevra Road", "BRANCH"),

            // Kharsia to Raigarh / Jharsuguda Route Stations
            StationCodeItem("JMG", "Jamga", "MAINLINE"),
            StationCodeItem("DAO", "Daghora", "MAINLINE"),
            StationCodeItem("BPH", "Belpahar", "MAINLINE"),
            StationCodeItem("BRJN", "Brajrajnagar", "MAINLINE"),
            StationCodeItem("IB", "Ib", "MAINLINE"),
            StationCodeItem("JSG", "Jharsuguda Jn", "MAINLINE")
        )
    }
}
