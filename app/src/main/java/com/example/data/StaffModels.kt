package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffContact(
    val name: String,
    val designation: String,
    val mobile: String,
    val cug: String = ""
)

@JsonClass(generateAdapter = true)
data class LobbyCategory(
    val category: String,
    val contacts: List<StaffContact>
)

@JsonClass(generateAdapter = true)
data class Lobby(
    val id: Int,
    val code: String,
    val name: String,
    val categories: List<LobbyCategory>
) {
    val totalContacts: Int
        get() = categories.sumOf { it.contacts.size }
}

@JsonClass(generateAdapter = true)
data class DirectoryResponse(
    val lobbies: List<Lobby> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CrewMember(
    val crewId: String = "",
    val name: String = "",
    val designation: String = "",
    val category: String = "",
    val cadre: String = "",
    val mobile: String = ""
)

@JsonClass(generateAdapter = true)
data class StationContact(
    val sNo: Int = 0,
    val code: String = "",
    val name: String = "",
    val cugMobile: String = "",
    val landline: String = "",
    val section: String = ""
)

@JsonClass(generateAdapter = true)
data class StationResponse(
    val stations: List<StationContact> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CliContact(
    val id: Int,
    val name: String,
    val restDay: String,
    val mobile: String
)

@JsonClass(generateAdapter = true)
data class SanderBoyContact(
    val id: Int,
    val shift: String,
    val name: String,
    val mobile: String
)

@JsonClass(generateAdapter = true)
data class TlcContact(
    val sNo: Int,
    val name: String,
    val mobile: String,
    val designation: String = "TLC - Traction Loco Controller",
    val division: String = "Bilaspur (BSP)"
)

@JsonClass(generateAdapter = true)
data class LobbyCccContact(
    val lobbyCode: String,
    val name: String,
    val designation: String,
    val mobile: String
)

@JsonClass(generateAdapter = true)
data class KharsiaStaffItem(
    val id: String,
    val name: String,
    val role: String,
    val category: String, // "LPG", "TM", "ALP", "CLI", "JEEP DRIVER", "SANDER BOY", "CCC"
    val subDetail: String = "",
    val mobile: String,
    val altMobile: String = "",
    val badge: String = ""
)

@JsonClass(generateAdapter = true)
data class OtherLobbyCrewItem(
    val lobbyCode: String,
    val lobbyName: String,
    val category: String,
    val name: String,
    val designation: String,
    val mobile: String
)
