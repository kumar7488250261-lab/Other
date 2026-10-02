package com.example.data.equipment

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CrewMember(
    val crewId: String,
    val name: String,
    val designation: String,
    val category: String = "",
    val mobile: String = "",
    val cmsId: String = ""
)

data class CrewSlotItem(
    val slotNumber: Int,
    val crewId: String = "",
    val name: String = "",
    val designation: String = "",
    val mobile: String = "",
    val isPresent: Boolean = false
)

data class EquipmentRecord(
    val id: Long = 0,
    val equipmentType: String,
    val serialNumber: String,
    val crewId: String,
    val crewName: String,
    val trainNumber: String = "",
    val fromStation: String = "",
    val toStation: String = "",
    val issuedAt: String = "",
    val returnedAt: String? = null,
    val status: String = "ISSUED", // ISSUED, RETURNED
    val remarks: String = ""
)

@JsonClass(generateAdapter = true)
data class PrRequest(
    val id: Long = 0,
    val crewId: String,
    val crewName: String,
    val designation: String,
    val signOffDate: String,
    val signOffTime: String,
    val requestDate: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val remarks: String = "",
    val reviewedBy: String? = null,
    val reviewedAt: String? = null
)

data class LongHourDutyRecord(
    val id: Long = 0,
    val crewId: String,
    val crewName: String,
    val designation: String,
    val trainNo: String,
    val section: String,
    val signOnTime: String,
    val signOnDate: String,
    val dutyHours: Double = 0.0,
    val status: String = "ON_DUTY", // ON_DUTY, RELIEVED, LONG_HOUR
    val reliefStation: String = "",
    val remarks: String = "",
    val locoNo: String = "",
    val currentPosition: String = "",
    val gdrStatus: String = "",
    val expectedDeparture: String = "",
    val reliefStatus: String = "",
    val reliefTime: String = "",
    val firestoreId: String = ""
)

data class JeepAvailabilityItem(
    val vehicleNo: String,
    val driverName: String,
    val driverMobile: String,
    val status: String = "AVAILABLE", // AVAILABLE, ON_TRIP, MAINTENANCE
    val lastReturnedTime: String = "",
    val assignedCrewCount: Int = 0
)

data class JeepMovementRecord(
    val id: Long = 0,
    val vehicleNo: String,
    val driverName: String,
    val fromLocation: String,
    val toLocation: String,
    val departureTime: String,
    val expectedReturnTime: String = "",
    val actualReturnTime: String? = null,
    val purpose: String = "",
    val crewNames: String = "",
    val status: String = "ACTIVE"
)

data class RosterTlcRecord(
    val id: Long = 0,
    val shift: String, // 06-14, 14-22, 22-06
    val date: String,
    val role: String,
    val staffId: String,
    val staffName: String,
    val designation: String,
    val mobile: String,
    val tlcName: String = "",
    val tlcMobile: String = "",
    val remarks: String = ""
)

data class StoreIssueRecord(
    val id: Long = 0,
    val equipmentName: String,
    val serialNumber: String,
    val crewId: String,
    val crewName: String,
    val designation: String,
    val trainNo: String,
    val issueTime: String,
    val returnTime: String? = null,
    val issuedBy: String = "Store In-Charge",
    val status: String = "ISSUED" // ISSUED, RETURNED, PENDING_APPROVAL
)

data class StoreShiftRecord(
    val id: Long = 0,
    val shift: String,
    val date: String,
    val adminName: String,
    val totalIssued: Int = 0,
    val totalReturned: Int = 0,
    val remarks: String = ""
)
