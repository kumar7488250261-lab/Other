package com.example.data.attendance

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(private val context: Context) {
    private val prefsRunningRoom: SharedPreferences =
        context.getSharedPreferences("kharsia_running_room_boy_v2_prefs", Context.MODE_PRIVATE)
    private val prefsBoxBoy: SharedPreferences =
        context.getSharedPreferences("kharsia_box_boy_v2_prefs", Context.MODE_PRIVATE)
    private val prefsSanderBoy: SharedPreferences =
        context.getSharedPreferences("kharsia_sander_boy_v2_prefs", Context.MODE_PRIVATE)
    private val prefsCliPosition: SharedPreferences =
        context.getSharedPreferences("kharsia_cli_position_prefs", Context.MODE_PRIVATE)
    private val prefsJeepDriver: SharedPreferences =
        context.getSharedPreferences("kharsia_jeep_driver_attendance_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val boyListType = Types.newParameterizedType(List::class.java, CommonBoyAttendanceRecord::class.java)
    private val boyAdapter = moshi.adapter<List<CommonBoyAttendanceRecord>>(boyListType)

    private val cliListType = Types.newParameterizedType(List::class.java, CliPositionRecord::class.java)
    private val cliAdapter = moshi.adapter<List<CliPositionRecord>>(cliListType)

    private val jeepDriverListType = Types.newParameterizedType(List::class.java, JeepDriverAttendanceRecord::class.java)
    private val jeepDriverAdapter = moshi.adapter<List<JeepDriverAttendanceRecord>>(jeepDriverListType)

    private var cachedRunningRoom: MutableList<CommonBoyAttendanceRecord>? = null
    private var cachedBoxBoy: MutableList<CommonBoyAttendanceRecord>? = null
    private var cachedSanderBoy: MutableList<CommonBoyAttendanceRecord>? = null
    private var cachedCliPosition: MutableList<CliPositionRecord>? = null
    private var cachedJeepDriver: MutableList<JeepDriverAttendanceRecord>? = null

    init {
        loadAll()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun loadAll() {
        val today = getTodayDateString()

        // 1. Running Room Boys
        val rrJson = prefsRunningRoom.getString("records", null)
        if (rrJson.isNullOrBlank()) {
            val initial = listOf(
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "00-08",
                    name = "Parmeshwar",
                    mobileNo = "6268971289",
                    baNo = "BA-101 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "08-16",
                    name = "Sanju",
                    mobileNo = "7067243113",
                    baNo = "BA-102 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "08-16",
                    name = "Ajay",
                    mobileNo = "8889528490",
                    baNo = "BA-103 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "16-00",
                    name = "Umesh",
                    mobileNo = "7225913727",
                    baNo = "BA-104 (0.00%)"
                )
            )
            cachedRunningRoom = initial.toMutableList()
            saveRunningRoomToDisk()
        } else {
            cachedRunningRoom = try {
                boyAdapter.fromJson(rrJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        // 2. Box Boys
        val bbJson = prefsBoxBoy.getString("records", null)
        if (bbJson.isNullOrBlank()) {
            val initial = listOf(
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "00-08",
                    name = "Bharat Bhushan Yadav",
                    mobileNo = "8319632548",
                    baNo = "BA-201 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "08-16",
                    name = "Rakesh Bhardwaj",
                    mobileNo = "9301939231",
                    baNo = "BA-202 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "16-00",
                    name = "Narendra",
                    mobileNo = "6262735467",
                    baNo = "BA-203 (0.00%)"
                )
            )
            cachedBoxBoy = initial.toMutableList()
            saveBoxBoyToDisk()
        } else {
            cachedBoxBoy = try {
                boyAdapter.fromJson(bbJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        // 3. Sander Boys
        val sbJson = prefsSanderBoy.getString("records", null)
        if (sbJson.isNullOrBlank()) {
            val initial = listOf(
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "00-08",
                    name = "Ishan",
                    mobileNo = "8871045991",
                    baNo = "BA-301 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "08-16",
                    name = "Dharam",
                    mobileNo = "9131815151",
                    baNo = "BA-302 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "08-16",
                    name = "Kishun",
                    mobileNo = "6266056927",
                    baNo = "BA-303 (0.00%)"
                ),
                CommonBoyAttendanceRecord(
                    date = today,
                    shift = "16-00",
                    name = "Bharat",
                    mobileNo = "8319632548",
                    baNo = "BA-304 (0.00%)"
                )
            )
            cachedSanderBoy = initial.toMutableList()
            saveSanderBoyToDisk()
        } else {
            cachedSanderBoy = try {
                boyAdapter.fromJson(sbJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        // 4. CLI Position
        val cliJson = prefsCliPosition.getString("records", null)
        if (cliJson.isNullOrBlank()) {
            val initialCli = AttendanceConstants.CLI_CADRE.map { item ->
                CliPositionRecord(
                    date = today,
                    cliName = item.name,
                    cugMobile = item.mobile,
                    scheduledRestDay = item.extraInfo,
                    status = if (item.extraInfo.equals("MONDAY", ignoreCase = true)) "Rest Day" else "Available",
                    availableTime = if (!item.extraInfo.equals("MONDAY", ignoreCase = true)) "08:00" else "",
                    remarks = "Kharsia Lobby HQ"
                )
            }
            cachedCliPosition = initialCli.toMutableList()
            saveCliPositionToDisk()
        } else {
            cachedCliPosition = try {
                cliAdapter.fromJson(cliJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        // 5. Jeep Driver Attendance
        val jdJson = prefsJeepDriver.getString("records", null)
        if (jdJson.isNullOrBlank()) {
            val initialJd = listOf(
                JeepDriverAttendanceRecord(
                    date = today,
                    driverName = "Rajendra Patel",
                    mobileNo = "6265395782",
                    jeepNo = "89",
                    shiftTime = "08-20",
                    baNo = "BA-J01 (0.00%)"
                ),
                JeepDriverAttendanceRecord(
                    date = today,
                    driverName = "Deepak Sahu",
                    mobileNo = "8817361305",
                    jeepNo = "79",
                    shiftTime = "08-20",
                    baNo = "BA-J02 (0.00%)"
                ),
                JeepDriverAttendanceRecord(
                    date = today,
                    driverName = "Kundan",
                    mobileNo = "7000592530",
                    jeepNo = "89(II)",
                    shiftTime = "20-08",
                    baNo = "BA-J03 (0.00%)"
                ),
                JeepDriverAttendanceRecord(
                    date = today,
                    driverName = "Sagar",
                    mobileNo = "9770612511",
                    jeepNo = "31",
                    shiftTime = "05-17",
                    baNo = "BA-J04 (0.00%)"
                )
            )
            cachedJeepDriver = initialJd.toMutableList()
            saveJeepDriverToDisk()
        } else {
            cachedJeepDriver = try {
                jeepDriverAdapter.fromJson(jdJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }
    }

    // --- Running Room Boys ---
    fun getRunningRoomRecords(filterDate: String? = null, filterShift: String? = null): List<CommonBoyAttendanceRecord> {
        var list = cachedRunningRoom?.toList() ?: emptyList()
        if (!filterDate.isNullOrBlank() && filterDate != "ALL") {
            list = list.filter { it.date == filterDate }
        }
        if (!filterShift.isNullOrBlank() && filterShift != "ALL") {
            list = list.filter { it.shift == filterShift }
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun saveRunningRoomRecord(record: CommonBoyAttendanceRecord) {
        val list = cachedRunningRoom ?: mutableListOf()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(0, record)
        }
        cachedRunningRoom = list
        saveRunningRoomToDisk()
    }

    fun deleteRunningRoomRecord(id: String) {
        val list = cachedRunningRoom ?: return
        list.removeAll { it.id == id }
        cachedRunningRoom = list
        saveRunningRoomToDisk()
    }

    private fun saveRunningRoomToDisk() {
        val json = boyAdapter.toJson(cachedRunningRoom ?: emptyList())
        prefsRunningRoom.edit().putString("records", json).apply()
    }

    // --- Box Boys ---
    fun getBoxBoyRecords(filterDate: String? = null, filterShift: String? = null): List<CommonBoyAttendanceRecord> {
        var list = cachedBoxBoy?.toList() ?: emptyList()
        if (!filterDate.isNullOrBlank() && filterDate != "ALL") {
            list = list.filter { it.date == filterDate }
        }
        if (!filterShift.isNullOrBlank() && filterShift != "ALL") {
            list = list.filter { it.shift == filterShift }
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun saveBoxBoyRecord(record: CommonBoyAttendanceRecord) {
        val list = cachedBoxBoy ?: mutableListOf()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(0, record)
        }
        cachedBoxBoy = list
        saveBoxBoyToDisk()
    }

    fun deleteBoxBoyRecord(id: String) {
        val list = cachedBoxBoy ?: return
        list.removeAll { it.id == id }
        cachedBoxBoy = list
        saveBoxBoyToDisk()
    }

    private fun saveBoxBoyToDisk() {
        val json = boyAdapter.toJson(cachedBoxBoy ?: emptyList())
        prefsBoxBoy.edit().putString("records", json).apply()
    }

    // --- Sander Boys ---
    fun getSanderBoyRecords(filterDate: String? = null, filterShift: String? = null): List<CommonBoyAttendanceRecord> {
        var list = cachedSanderBoy?.toList() ?: emptyList()
        if (!filterDate.isNullOrBlank() && filterDate != "ALL") {
            list = list.filter { it.date == filterDate }
        }
        if (!filterShift.isNullOrBlank() && filterShift != "ALL") {
            list = list.filter { it.shift == filterShift }
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun saveSanderBoyRecord(record: CommonBoyAttendanceRecord) {
        val list = cachedSanderBoy ?: mutableListOf()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(0, record)
        }
        cachedSanderBoy = list
        saveSanderBoyToDisk()
    }

    fun deleteSanderBoyRecord(id: String) {
        val list = cachedSanderBoy ?: return
        list.removeAll { it.id == id }
        cachedSanderBoy = list
        saveSanderBoyToDisk()
    }

    private fun saveSanderBoyToDisk() {
        val json = boyAdapter.toJson(cachedSanderBoy ?: emptyList())
        prefsSanderBoy.edit().putString("records", json).apply()
    }

    // --- CLI Position ---
    fun getCliPositions(filterDate: String? = null): List<CliPositionRecord> {
        var list = cachedCliPosition?.toList() ?: emptyList()
        if (!filterDate.isNullOrBlank() && filterDate != "ALL") {
            list = list.filter { it.date == filterDate }
        }
        return list.sortedBy { it.cliName }
    }

    fun saveCliPosition(record: CliPositionRecord) {
        val list = cachedCliPosition ?: mutableListOf()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(0, record)
        }
        cachedCliPosition = list
        saveCliPositionToDisk()
    }

    fun deleteCliPosition(id: String) {
        val list = cachedCliPosition ?: return
        list.removeAll { it.id == id }
        cachedCliPosition = list
        saveCliPositionToDisk()
    }

    private fun saveCliPositionToDisk() {
        val json = cliAdapter.toJson(cachedCliPosition ?: emptyList())
        prefsCliPosition.edit().putString("records", json).apply()
    }

    // --- Jeep Driver Attendance ---
    fun getJeepDriverAttendances(filterDate: String? = null, filterShift: String? = null): List<JeepDriverAttendanceRecord> {
        var list = cachedJeepDriver?.toList() ?: emptyList()
        if (!filterDate.isNullOrBlank() && filterDate != "ALL") {
            list = list.filter { it.date == filterDate }
        }
        if (!filterShift.isNullOrBlank() && filterShift != "ALL") {
            list = list.filter { it.shiftTime == filterShift }
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun saveJeepDriverAttendance(record: JeepDriverAttendanceRecord) {
        val list = cachedJeepDriver ?: mutableListOf()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(0, record)
        }
        cachedJeepDriver = list
        saveJeepDriverToDisk()
    }

    fun deleteJeepDriverAttendance(id: String) {
        val list = cachedJeepDriver ?: return
        list.removeAll { it.id == id }
        cachedJeepDriver = list
        saveJeepDriverToDisk()
    }

    private fun saveJeepDriverToDisk() {
        val json = jeepDriverAdapter.toJson(cachedJeepDriver ?: emptyList())
        prefsJeepDriver.edit().putString("records", json).apply()
    }
}
