package com.example.data.jeep

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class JeepDriver(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mobile: String,
    val altMobile: String = "",
    val aadhaar: String = "",
    val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class JeepVehicle(
    val vehicleNo: String, // e.g. "89", "89(II)", "79", "31", "22", "91"
    val defaultDriverName: String = "",
    val defaultDriverMobile: String = "",
    val status: String = "AVAILABLE", // AVAILABLE, ON_TRIP, BREAKDOWN
    val lobbyArrivalTime: String = "08:00", // Time arrived at Kharsia Lobby (for FIFO ordering)
    val currentDestination: String = "",
    val lastDispatchedTime: String = "",
    val breakdownStartTime: String = "",
    val breakdownEndTime: String = "",
    val breakdownReason: String = ""
)

@JsonClass(generateAdapter = true)
data class JeepDiDuty(
    val shift: String = "14:00 - 22:00", // 06:00 - 14:00, 14:00 - 22:00, 22:00 - 06:00
    val diCrewId: String = "KHS1001",
    val diName: String = "ROHIT KU KURRE",
    val diDesignation: String = "LPG - Loco Pilot (Goods)",
    val dutyDate: String = ""
)

@JsonClass(generateAdapter = true)
data class StationCodeItem(
    val code: String,
    val name: String,
    val category: String = "MAINLINE" // MAINLINE, SIDING, BRANCH
)

@JsonClass(generateAdapter = true)
data class JeepTripRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleNo: String,
    val driverName: String,
    val driverMobile: String,
    val driverAadhaar: String = "",
    val jeepDiName: String = "",
    val shift: String = "",
    val fromStationCode: String = "KHS",
    val fromStationName: String = "Kharsia Lobby",
    val departureTime: String,
    val departureDate: String,
    val toStationCode: String,
    val toStationName: String,
    // Outgoing Crew details
    val outgoingCrewId: String = "",
    val outgoingCrewName: String = "",
    val outgoingCrewDesig: String = "",
    val outgoingOtherCrew: String = "",
    val isUpdateLater: Boolean = false,
    val status: String = "ON_TRIP", // ON_TRIP, COMPLETED, BREAKDOWN
    // Arrival & Returning details (updated upon arrival back to Kharsia)
    val arrivalAtKharsiaTime: String = "",
    val returningCrewId: String = "",
    val returningCrewName: String = "",
    val returningCrewDesig: String = "",
    val returningOtherCrew: String = "",
    val returningFromStationCode: String = "",
    val returningDepartureTime: String = "",
    val remarks: String = ""
)

@JsonClass(generateAdapter = true)
data class JeepBreakdownRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleNo: String,
    val driverName: String,
    val breakdownStartTime: String,
    val breakdownStartDate: String,
    val breakdownEndTime: String = "",
    val isRestored: Boolean = false,
    val reason: String = "",
    val reportedBy: String = ""
)
