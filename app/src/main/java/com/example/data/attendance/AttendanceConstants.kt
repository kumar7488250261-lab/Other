package com.example.data.attendance

data class StaffDirectoryItem(
    val name: String,
    val mobile: String,
    val defaultShift: String = "",
    val extraInfo: String = ""
)

object AttendanceConstants {
    val SHIFTS_COMMON = listOf("00-08", "08-16", "16-00")
    val JEEP_NUMBERS = listOf("89", "89(II)", "79", "31", "22", "91")
    val JEEP_SHIFT_TIMES = listOf("20-08", "08-20", "05-17", "17-05")
    val CLI_STATUS_LIST = listOf("Available", "Not Available", "Rest Day", "On Leave")

    // 1. Sander Boys (from PDF)
    val SANDER_BOYS = listOf(
        StaffDirectoryItem("Dharam", "9131815151", "08-16"),
        StaffDirectoryItem("Kishun", "6266056927", "08-16"),
        StaffDirectoryItem("Bharat", "8319632548", "16-00"),
        StaffDirectoryItem("Umang", "7470744231", "16-00"),
        StaffDirectoryItem("Ishan", "8871045991", "00-08"),
        StaffDirectoryItem("Emon", "9201300822", "00-08")
    )

    // 2. Box Boys (from PDF)
    val BOX_BOYS = listOf(
        StaffDirectoryItem("Rakesh Bhardwaj", "9301939231", "08-16"),
        StaffDirectoryItem("Bharat Bhushan Yadav", "8319632548", "00-08"),
        StaffDirectoryItem("Narendra", "6262735467", "16-00"),
        StaffDirectoryItem("Pappu / Bharat", "8319632548", "00-08")
    )

    // 3. Running Room Boys (from PDF)
    val RUNNING_ROOM_BOYS = listOf(
        StaffDirectoryItem("Parmeshwar", "6268971289", "", "Active"),
        StaffDirectoryItem("Sanju", "7067243113", ""),
        StaffDirectoryItem("Ajay", "8889528490", ""),
        StaffDirectoryItem("Umesh", "7225913727", ""),
        StaffDirectoryItem("Ajay / Kr Singh", "8889528490", ""),
        StaffDirectoryItem("Kishun", "8719916498", ""),
        StaffDirectoryItem("Nanku", "6266550085", ""),
        StaffDirectoryItem("Bitu Chandra", "6288489149", "", "GGDA / RR")
    )

    // 4. Kharsia CLI Directory (from PDF)
    val CLI_CADRE = listOf(
        StaffDirectoryItem("CLI SHRI VIJAY KUMAR", "9752442527", "", "MONDAY"),
        StaffDirectoryItem("CLI SHRI M. ANAND RAO", "9752441206", "", "TUESDAY"),
        StaffDirectoryItem("CLI SHRI K. N. VERMA", "9752442765", "", "TUESDAY"),
        StaffDirectoryItem("CLI SHRI D. K. YADAV", "9752441423", "", "WEDNESDAY"),
        StaffDirectoryItem("CLI SHRI NEEMESH TURKANE", "9752442747", "", "WEDNESDAY"),
        StaffDirectoryItem("CLI SHRI SATYADEV", "7225020871", "", "THURSDAY"),
        StaffDirectoryItem("CLI SHRI TRIBHUWAN", "9752441912", "", "THURSDAY"),
        StaffDirectoryItem("CLI SHRI S. K. BAGHMAR", "9752442338", "", "FRIDAY"),
        StaffDirectoryItem("CLI SHRI KULDEEP", "9752442139", "", "SATURDAY"),
        StaffDirectoryItem("CLI SHRI M. LAXMAN RAO", "9752491759", "", "SATURDAY"),
        StaffDirectoryItem("CLI SHRI L. K. SHARAFF", "9752442147", "", "SUNDAY"),
        StaffDirectoryItem("CLI SHRI LAXMIKANT SAHU", "9752442786", "", "SUNDAY")
    )

    // 5. Jeep Drivers Directory (from PDF)
    val JEEP_DRIVERS = listOf(
        StaffDirectoryItem("Rajendra Patel", "6265395782"),
        StaffDirectoryItem("Deepak Sahu", "8817361305"),
        StaffDirectoryItem("Sagar", "9770612511"),
        StaffDirectoryItem("Kundan", "7000592530"),
        StaffDirectoryItem("Yuval (Golu)", "8839384199"),
        StaffDirectoryItem("Bhag Singh", "9081175369"),
        StaffDirectoryItem("Assem / Sushil Kumar", "8450865891"),
        StaffDirectoryItem("Anurag", "6261445934"),
        StaffDirectoryItem("Pintu Chouhan", "7970256421"),
        StaffDirectoryItem("Nihal", "9201415930"),
        StaffDirectoryItem("Jayant", "7415365109"),
        StaffDirectoryItem("Yogesh", "9575904597"),
        StaffDirectoryItem("Dilip Kumar", "8817842929"),
        StaffDirectoryItem("Dayanand", "8839976221"),
        StaffDirectoryItem("Shiv Nishad", "8358082886"),
        StaffDirectoryItem("Ravi Rathia (1)", "7000468629"),
        StaffDirectoryItem("Pappu Chouhan", "7999810631"),
        StaffDirectoryItem("Goldu / Golu", "7724050115"),
        StaffDirectoryItem("Arjun", "9340504813"),
        StaffDirectoryItem("Pankaj", "6204069455"),
        StaffDirectoryItem("Rajesh", "7898468217"),
        StaffDirectoryItem("Surya Prakash", "7898468217"),
        StaffDirectoryItem("Nilesh", "6264965208"),
        StaffDirectoryItem("Ganesh (GS)", "9201525083"),
        StaffDirectoryItem("Deepak Chouhan", "9111058832"),
        StaffDirectoryItem("Manish Rathiya", "8305660080"),
        StaffDirectoryItem("Ravi Rathia (2)", "7724050010"),
        StaffDirectoryItem("Mukesh", "8629994690"),
        StaffDirectoryItem("Prakash", "9243737179"),
        StaffDirectoryItem("Sanjay", "7999787360"),
        StaffDirectoryItem("Jitendra Chouhan", "9343935824"),
        StaffDirectoryItem("Ayshu", "9202797597"),
        StaffDirectoryItem("Sahu Kumar", "7879858764"),
        StaffDirectoryItem("Sanu", "7878858764"),
        StaffDirectoryItem("Devansh", "6262815619")
    )
}
