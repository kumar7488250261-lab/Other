package com.example.data.lr

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class LrDeclarationRecord(
    val id: String = UUID.randomUUID().toString(),
    val monthYear: String = "AUG-2026", // e.g. AUG-2026, SEP-2026, OCT-2026
    val sNo: Int = 1,
    val crewId: String = "",
    val pfNumber: String = "",
    val crewName: String = "",
    val designation: String = "ALP",
    val khsNia: Boolean = true,
    val niaBsp: Boolean = true,
    val bspNia: Boolean = true,
    val niaKhs: Boolean = true,
    val khsRig: Boolean = true,
    val khsKchp: Boolean = true,
    val kchpKhs: Boolean = true,
    val byps: Boolean = true,
    val lastWorkingDate: String = "",
    val signature: String = "Declared",
    val submittedAt: String = "",
    val remarks: String = ""
) {
    val totalValidSections: Int
        get() = listOf(khsNia, niaBsp, bspNia, niaKhs, khsRig, khsKchp, kchpKhs, byps).count { it }

    val isAllValid: Boolean
        get() = totalValidSections == 8
}

@JsonClass(generateAdapter = true)
data class LrMonthlyContainer(
    val records: List<LrDeclarationRecord> = emptyList()
)
