package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.AuthManager
import com.example.data.StaffRepository
import com.example.data.attendance.AttendanceRepository
import com.example.data.equipment.InChargeAuthManager
import com.example.data.jeep.JeepRepository
import com.example.data.lr.LrDeclarationRepository
import com.example.ui.screens.*
import com.example.ui.screens.attendance.BoxBoyScreen
import com.example.ui.screens.attendance.CliPositionScreen
import com.example.ui.screens.attendance.JeepDriverAttendanceScreen
import com.example.ui.screens.attendance.RunningRoomBoyScreen
import com.example.ui.screens.attendance.SanderBoyScreen
import com.example.ui.screens.equipment.StoreRegisterScreen
import com.example.ui.screens.jeep.JeepSubMenuScreen
import com.example.ui.screens.longhour.LongHourUpdateScreen
import com.example.ui.screens.lr.LrDeclarationScreen
import com.example.ui.screens.pr.PeriodicalRestScreen
import com.example.ui.screens.roster.RosterAdminPortalScreen
import com.example.ui.screens.roster.RosterTlcSubMenuScreen
import com.example.ui.screens.roster.ShiftWiseRosterScreen
import com.example.ui.theme.KharsiaLobbyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KharsiaLobbyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val authManager = remember { AuthManager(context) }
                    val inChargeAuthManager = remember { InChargeAuthManager(context) }
                    val staffRepository = remember { StaffRepository(context) }
                    val jeepRepository = remember { JeepRepository(context) }
                    val lrRepository = remember { LrDeclarationRepository(context) }
                    val attendanceRepository = remember { AttendanceRepository(context) }

                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                    var currentUserId by remember { mutableStateOf(authManager.currentUserId ?: "GUEST") }

                    when (val screen = currentScreen) {
                        is Screen.Splash -> {
                            SplashScreen(
                                onTimeout = {
                                    currentScreen = if (authManager.isLoggedIn) Screen.MainMenu else Screen.Landing
                                }
                            )
                        }
                        is Screen.Welcome -> {
                            WelcomeScreen(
                                onContinue = {
                                    currentScreen = Screen.Landing
                                }
                            )
                        }
                        is Screen.Landing -> {
                            LobbyLandingScreen(
                                onNavigateToLogin = {
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                        is Screen.Login -> {
                            BackHandler {
                                currentScreen = Screen.Landing
                            }
                            LoginScreen(
                                authManager = authManager,
                                onLoginSuccess = { userId ->
                                    currentUserId = userId
                                    currentScreen = Screen.MainMenu
                                },
                                onBack = {
                                    currentScreen = Screen.Landing
                                }
                            )
                        }
                        is Screen.MainMenu -> {
                            MainMenuScreen(
                                currentUserId = currentUserId,
                                onNavigateToStaffDirectory = {
                                    currentScreen = Screen.StaffDirectory
                                },
                                onNavigateToEquipmentRegister = {
                                    currentScreen = Screen.EquipmentRegisterHome
                                },
                                onNavigateToPeriodicalRest = {
                                    currentScreen = Screen.PeriodicalRest
                                },
                                onNavigateToLongHour = {
                                    currentScreen = Screen.LongHour
                                },
                                onNavigateToJeepSubMenu = {
                                    currentScreen = Screen.JeepSubMenu
                                },
                                onNavigateToRosterTlc = {
                                    currentScreen = Screen.RosterTlcSubMenu
                                },
                                onNavigateToLrDeclaration = {
                                    currentScreen = Screen.LrDeclaration
                                },
                                onNavigateToRunningRoomBoy = {
                                    currentScreen = Screen.RunningRoomBoyAttendance
                                },
                                onNavigateToBoxBoy = {
                                    currentScreen = Screen.BoxBoyAttendance
                                },
                                onNavigateToSanderBoy = {
                                    currentScreen = Screen.SanderBoyAttendance
                                },
                                onNavigateToCliPosition = {
                                    currentScreen = Screen.CliPosition
                                },
                                onNavigateToJeepDriverAttendance = {
                                    currentScreen = Screen.JeepDriverAttendance
                                },
                                onLogout = {
                                    authManager.logout()
                                    currentUserId = "GUEST"
                                    currentScreen = Screen.Landing
                                }
                            )
                        }
                        is Screen.StaffDirectory -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            StaffDirectoryScreen(
                                staffRepository = staffRepository,
                                onLobbyClick = { lobby ->
                                    currentScreen = Screen.LobbyDetail(lobby)
                                },
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.LobbyDetail -> {
                            BackHandler {
                                currentScreen = Screen.StaffDirectory
                            }
                            LobbyDetailScreen(
                                lobby = screen.lobby,
                                onBack = {
                                    currentScreen = Screen.StaffDirectory
                                }
                            )
                        }
                        is Screen.EquipmentRegisterHome, is Screen.FastIssue, is Screen.FastReturn, is Screen.SupervisorDashboard -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            StoreRegisterScreen(
                                inChargeAuthManager = inChargeAuthManager,
                                staffRepository = staffRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.PeriodicalRest -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            PeriodicalRestScreen(
                                inChargeAuthManager = inChargeAuthManager,
                                staffRepository = staffRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.LongHour -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            LongHourUpdateScreen(
                                inChargeAuthManager = inChargeAuthManager,
                                staffRepository = staffRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.JeepSubMenu, is Screen.JeepAvailability, is Screen.JeepMovementEntry -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            val initialTab = when (screen) {
                                is Screen.JeepAvailability -> 0
                                is Screen.JeepMovementEntry -> 1
                                else -> 0
                            }
                            JeepSubMenuScreen(
                                jeepRepository = jeepRepository,
                                staffRepository = staffRepository,
                                initialTab = initialTab,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.RosterTlcSubMenu -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            RosterTlcSubMenuScreen(
                                inChargeAuthManager = inChargeAuthManager,
                                onNavigateToShiftRoster = {
                                    currentScreen = Screen.ShiftWiseRoster
                                },
                                onNavigateToAdminPortal = {
                                    currentScreen = Screen.RosterAdminPortal
                                },
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.ShiftWiseRoster -> {
                            BackHandler {
                                currentScreen = Screen.RosterTlcSubMenu
                            }
                            ShiftWiseRosterScreen(
                                onBack = {
                                    currentScreen = Screen.RosterTlcSubMenu
                                }
                            )
                        }
                        is Screen.RosterAdminPortal -> {
                            BackHandler {
                                currentScreen = Screen.RosterTlcSubMenu
                            }
                            RosterAdminPortalScreen(
                                staffRepository = staffRepository,
                                onBack = {
                                    currentScreen = Screen.RosterTlcSubMenu
                                }
                            )
                        }
                        is Screen.LrDeclaration -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            LrDeclarationScreen(
                                lrRepository = lrRepository,
                                staffRepository = staffRepository,
                                currentUserId = currentUserId,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.RunningRoomBoyAttendance -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            RunningRoomBoyScreen(
                                attendanceRepository = attendanceRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.BoxBoyAttendance -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            BoxBoyScreen(
                                attendanceRepository = attendanceRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.SanderBoyAttendance -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            SanderBoyScreen(
                                attendanceRepository = attendanceRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.CliPosition -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            CliPositionScreen(
                                attendanceRepository = attendanceRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                        is Screen.JeepDriverAttendance -> {
                            BackHandler {
                                currentScreen = Screen.MainMenu
                            }
                            JeepDriverAttendanceScreen(
                                attendanceRepository = attendanceRepository,
                                onBack = {
                                    currentScreen = Screen.MainMenu
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
