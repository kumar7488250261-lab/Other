package com.example.data.lr

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LrDeclarationRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kharsia_lr_declarations_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, LrDeclarationRecord::class.java)
    private val adapter = moshi.adapter<List<LrDeclarationRecord>>(listType)

    private val knownPfMap: Map<String, String> = mapOf(
        "KHS1006" to "39229802642",
        "KHS1008" to "39229802659",
        "KHS1020" to "39229802650",
        "KHS1046" to "39229802410",
        "KHS1084" to "39229802738",
        "KHS1087" to "39229802728",
        "KHS1096" to "39229802741",
        "KHS1119" to "39313AC0972",
        "KHS1120" to "39213AB1438",
        "KHS1121" to "39515AE0398",
        "KHS1126" to "39229802548",
        "KHS1127" to "12329803188",
        "KHS1129" to "53416AB0268",
        "KHS1136" to "39229801792",
        "KHS1138" to "39229802672"
    )

    private var memoryCache: MutableList<LrDeclarationRecord>? = null

    init {
        loadRecords()
    }

    fun getPfForCrew(crewId: String): String {
        return knownPfMap[crewId.trim().uppercase()] ?: ""
    }

    fun getAllRecords(): List<LrDeclarationRecord> {
        if (memoryCache == null) {
            memoryCache = loadRecords().toMutableList()
        }
        return memoryCache ?: emptyList()
    }

    fun getRecordsForMonth(monthYear: String): List<LrDeclarationRecord> {
        val target = monthYear.trim().uppercase()
        return getAllRecords().filter { it.monthYear.trim().uppercase() == target }
            .sortedBy { it.sNo }
    }

    fun saveDeclaration(record: LrDeclarationRecord): Boolean {
        try {
            val list = getAllRecords().toMutableList()
            val month = record.monthYear.trim().uppercase()
            val crewId = record.crewId.trim().uppercase()

            val existingIndex = list.indexOfFirst {
                it.monthYear.trim().uppercase() == month && it.crewId.trim().uppercase() == crewId
            }

            if (existingIndex >= 0) {
                val existing = list[existingIndex]
                list[existingIndex] = record.copy(
                    id = existing.id,
                    sNo = existing.sNo,
                    submittedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                )
            } else {
                val countInMonth = list.count { it.monthYear.trim().uppercase() == month }
                val newSNo = countInMonth + 1
                list.add(
                    record.copy(
                        sNo = newSNo,
                        submittedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    )
                )
            }

            memoryCache = list
            persistRecords(list)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun deleteDeclaration(id: String): Boolean {
        try {
            val list = getAllRecords().toMutableList()
            val removed = list.removeAll { it.id == id }
            if (removed) {
                memoryCache = list
                persistRecords(list)
            }
            return removed
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun persistRecords(records: List<LrDeclarationRecord>) {
        try {
            val json = adapter.toJson(records)
            prefs.edit().putString("lr_declarations_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadRecords(): List<LrDeclarationRecord> {
        val json = prefs.getString("lr_declarations_json", null)
        if (!json.isNullOrBlank()) {
            try {
                val parsed = adapter.fromJson(json)
                if (!parsed.isNullOrEmpty()) {
                    memoryCache = parsed.toMutableList()
                    return parsed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialize with default seeded data from the actual official register image
        val initial = getSeedRecords()
        memoryCache = initial.toMutableList()
        persistRecords(initial)
        return initial
    }

    private fun getSeedRecords(): List<LrDeclarationRecord> {
        val list = mutableListOf<LrDeclarationRecord>()
        val defaultMonths = listOf("AUG-2026", "SEP-2026")

        for (m in defaultMonths) {
            list.addAll(
                listOf(
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 1,
                        crewId = "KHS1006",
                        pfNumber = "39229802642",
                        crewName = "RAKESH KR.RAJAK",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "15/08/2026",
                        signature = "Rakesh Rajak",
                        submittedAt = "15/08/2026 10:30"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 2,
                        crewId = "KHS1008",
                        pfNumber = "39229802659",
                        crewName = "NITISH KUMAR",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "18/08/2026",
                        signature = "Nitish Kumar",
                        submittedAt = "18/08/2026 14:15"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 3,
                        crewId = "KHS1020",
                        pfNumber = "39229802650",
                        crewName = "VIDYASAGAR KUMAR",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "12/08/2026",
                        signature = "Vidyasagar",
                        submittedAt = "12/08/2026 11:20"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 4,
                        crewId = "KHS1046",
                        pfNumber = "39229802410",
                        crewName = "D.P. CHANDRA",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "14/08/2026",
                        signature = "D.P. Chandra",
                        submittedAt = "14/08/2026 09:45"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 5,
                        crewId = "KHS1084",
                        pfNumber = "39229802738",
                        crewName = "UDAYNARAYAN SAHU",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "20/08/2026",
                        signature = "Udaynarayan",
                        submittedAt = "20/08/2026 16:10"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 6,
                        crewId = "KHS1087",
                        pfNumber = "39229802728",
                        crewName = "J P SHARMA",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "16/08/2026",
                        signature = "J.P. Sharma",
                        submittedAt = "16/08/2026 12:00"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 7,
                        crewId = "KHS1096",
                        pfNumber = "39229802741",
                        crewName = "AMIT KUMAR",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = false, // Not in BYPS as seen in sheet
                        lastWorkingDate = "19/08/2026",
                        signature = "Amit Kumar",
                        submittedAt = "19/08/2026 17:30"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 8,
                        crewId = "KHS1119",
                        pfNumber = "39313AC0972",
                        crewName = "B K CHOUHAN",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "10/08/2026",
                        signature = "B.K. Chouhan",
                        submittedAt = "10/08/2026 08:30"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 9,
                        crewId = "KHS1120",
                        pfNumber = "39213AB1438",
                        crewName = "KALESHWAR ORAON",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "21/08/2026",
                        signature = "Kaleshwar",
                        submittedAt = "21/08/2026 15:45"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 10,
                        crewId = "KHS1121",
                        pfNumber = "39515AE0398",
                        crewName = "MD.TAWREJ",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "17/08/2026",
                        signature = "Md. Tawrej",
                        submittedAt = "17/08/2026 13:00"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 11,
                        crewId = "KHS1126",
                        pfNumber = "39229802548",
                        crewName = "BHARAT LAL",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "22/08/2026",
                        signature = "Bharat Lal",
                        submittedAt = "22/08/2026 18:20"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 12,
                        crewId = "KHS1127",
                        pfNumber = "12329803188",
                        crewName = "H P KARSH",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "23/08/2026",
                        signature = "H.P. Karsh",
                        submittedAt = "23/08/2026 19:10"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 13,
                        crewId = "KHS1129",
                        pfNumber = "53416AB0268",
                        crewName = "VIKAS BHAGAT",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = true,
                        lastWorkingDate = "15/08/2026",
                        signature = "Vikas Bhagat",
                        submittedAt = "15/08/2026 11:50"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 14,
                        crewId = "KHS1136",
                        pfNumber = "39229801792",
                        crewName = "AKHILESH DANSENA",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = false,
                        lastWorkingDate = "24/08/2026",
                        signature = "Akhilesh",
                        submittedAt = "24/08/2026 20:00"
                    ),
                    LrDeclarationRecord(
                        monthYear = m,
                        sNo = 15,
                        crewId = "KHS1138",
                        pfNumber = "39229802672",
                        crewName = "DEVENDRA KUMAR",
                        designation = "SALP",
                        khsNia = true,
                        niaBsp = true,
                        bspNia = true,
                        niaKhs = true,
                        khsRig = true,
                        khsKchp = true,
                        kchpKhs = true,
                        byps = false,
                        lastWorkingDate = "25/08/2026",
                        signature = "Devendra",
                        submittedAt = "25/08/2026 21:15"
                    )
                )
            )
        }
        return list
    }
}
