package com.example

import com.example.data.Lobby

sealed class Screen {
    object Splash : Screen()
    object Welcome : Screen()
    object Landing : Screen()
    object Login : Screen()
    object MainMenu : Screen()
    object StaffDirectory : Screen()
    data class LobbyDetail(val lobby: Lobby) : Screen()
    object EquipmentRegisterHome : Screen()
    object FastIssue : Screen()
    object FastReturn : Screen()
    object SupervisorDashboard : Screen()
    object PeriodicalRest : Screen()
    object LongHour : Screen()
    object JeepSubMenu : Screen()
    object JeepAvailability : Screen()
    object JeepMovementEntry : Screen()
    object RosterTlcSubMenu : Screen()
    object ShiftWiseRoster : Screen()
    object RosterAdminPortal : Screen()
    object LrDeclaration : Screen()
    object RunningRoomBoyAttendance : Screen()
    object BoxBoyAttendance : Screen()
    object SanderBoyAttendance : Screen()
    object CliPosition : Screen()
    object JeepDriverAttendance : Screen()
}
