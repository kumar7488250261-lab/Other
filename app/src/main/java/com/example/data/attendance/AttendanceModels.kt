package com.example.data.attendance

import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Standard 5-column Attendance Record for:
 * 1. Kharsia Running Room Boy Attendance
 * 2. Box Boy Attendance
 * 3. Sander Filling Boy Attendance
 *
 * Columns strictly required:
 * - Date
 * - Shift (00-08, 08-16, 16-00)
 * - Name
 * - Mobile No
 * - BA (breathalyzer) No
 */
@JsonClass(generateAdapter = true)
data class CommonBoyAttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd
    val shift: String, // "00-08", "08-16", "16-00"
    val name: String,
    val mobileNo: String,
    val baNo: String, // Breathalyzer Test No
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * CLI Position Record
 * Date, Status (Available / Not Available / Rest Day / On Leave),
 * CLI Name, Available Time, Departure Time, Arrival Time, Section/Train, Remarks
 */
@JsonClass(generateAdapter = true)
data class CliPositionRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd
    val cliName: String,
    val cugMobile: String = "",
    val scheduledRestDay: String = "",
    val status: String, // "Available", "Not Available", "Rest Day", "On Leave"
    val availableTime: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val sectionOrTrain: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Jeep Driver Attendance Record
 * Date, Driver Name, Driver Mobile, Taken Charge Jeep No (89, 89(II), 79, 31, 22, 91),
 * Shift Time (20-08, 08-20, 05-17, 17-05), BA No
 */
@JsonClass(generateAdapter = true)
data class JeepDriverAttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd
    val driverName: String,
    val mobileNo: String,
    val jeepNo: String, // "89", "89(II)", "79", "31", "22", "91"
    val shiftTime: String, // "20-08", "08-20", "05-17", "17-05"
    val baNo: String,
    val timestamp: Long = System.currentTimeMillis()
)
