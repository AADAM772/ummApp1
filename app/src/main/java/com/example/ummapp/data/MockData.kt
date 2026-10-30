package com.example.ummapp.data

/**
 * Hardcoded accounts and static screen content standing in for a backend.
 * Screens where users interact with each other are served by [CampusHub].
 */
object MockData {

    val users = listOf(
        User("vc@umm.edu", "vc123", "Prof. Amina Hassan", Role.VICE_CHANCELLOR, "Office of the Vice Chancellor"),
        User("dvc@umm.edu", "dvc123", "Prof. Daniel Otieno", Role.DEPUTY_VC, "Academic Affairs"),
        User("dean.science@umm.edu", "dean123", "Prof. Fatuma Ali", Role.DEAN, "Faculty of Science"),
        User("dos@umm.edu", "dos123", "Dr. Peter Mwangi", Role.DEAN_OF_STUDENTS, "Student Affairs"),
        User("registrar@umm.edu", "reg123", "Mrs. Halima Yusuf", Role.REGISTRAR, "Registrar's Office"),
        User("hod.cs@umm.edu", "hod123", "Dr. Ibrahim Noor", Role.HOD, "Computer Science"),
        User("lecturer.cs@umm.edu", "lec123", "Mr. Kevin Kamau", Role.LECTURER, "Computer Science"),
        User("student@umm.edu", "stu123", "Zainab Omar", Role.STUDENT, "BSc Computer Science, Year 2"),
        User("bursar@umm.edu", "fin123", "Mr. Samuel Kiptoo", Role.FINANCE_OFFICER, "Finance Office"),
        User("library@umm.edu", "lib123", "Ms. Grace Wanjiru", Role.LIBRARIAN, "Library"),
        User("ict@umm.edu", "ict123", "Mr. Abdi Mohamed", Role.ICT_OFFICER, "ICT Department"),
        User("hr@umm.edu", "hr123", "Ms. Mary Achieng", Role.HR_OFFICER, "Human Resources"),
        User("nurse@umm.edu", "nurse123", "Nurse Rehema Salim", Role.NURSE, "Health Centre"),
        User("security@umm.edu", "sec123", "Officer James Mutua", Role.SECURITY, "Security"),
        User("cleaner@umm.edu", "clean123", "Mr. Joseph Njoroge", Role.CLEANER, "Estates and Maintenance"),
        User("cafeteria@umm.edu", "cafe123", "Ms. Esther Akinyi", Role.CAFETERIA, "Cafeteria"),
        User("warden@umm.edu", "hostel123", "Mr. Ali Hussein", Role.HOSTEL_WARDEN, "Hostels"),
        User("transport@umm.edu", "bus123", "Mr. Paul Odhiambo", Role.TRANSPORT, "Transport"),
    )

    fun login(email: String, password: String): User? =
        users.find { it.email.equals(email.trim(), ignoreCase = true) && it.password == password }

    fun findUser(email: String): User? = users.find { it.email.equals(email, ignoreCase = true) }

    fun rows(feature: Feature, user: User): List<Row> = CampusHub.liveRows(feature, user) ?: staticRows(feature)

    /** Fixed content, also used as the starting items for lists that offices can edit. */
    fun staticRows(feature: Feature): List<Row> = when (feature) {
        Feature.RESULTS -> listOf(
            Row("CSC 101 Intro to Programming", "Semester 1", "A", Tone.GOOD),
            Row("MAT 101 Calculus I", "Semester 1", "B", Tone.GOOD),
            Row("CSC 102 Computer Systems", "Semester 1", "B+", Tone.GOOD),
        )
        Feature.EVENTS -> listOf(
            Row("Computer Science Hackathon", "Fri 1 Nov, 9:00am, Lab 2"),
            Row("Inter-faculty Football", "Sat 2 Nov, 2:00pm, Main Field"),
            Row("Career Fair", "Wed 6 Nov, 10:00am, Main Hall"),
            Row("Graduation Ceremony", "Thu 12 Dec, 9:00am, Graduation Square"),
        )
        Feature.CAMPUS_MAP -> listOf(
            Row("Main Administration Block", "VC, Registrar and Finance offices"),
            Row("Science Complex", "Computer Science labs, Lecture Halls 1 to 6"),
            Row("Library", "Open 8:00am to 10:00pm"),
            Row("Health Centre", "Next to Hostel A, open 24 hours"),
            Row("Security Office", "At the Main Gate"),
            Row("Cafeteria", "Behind the Library"),
            Row("Hostels", "Blocks A to D, east campus"),
        )
        Feature.MENU -> listOf(
            Row("Breakfast", "Tea, mandazi, eggs", "KES 120"),
            Row("Lunch", "Pilau with beef stew", "KES 250"),
            Row("Lunch (vegetarian)", "Rice, beans and greens", "KES 180"),
            Row("Dinner", "Ugali, sukuma wiki and fish", "KES 220"),
        )
        Feature.BUS_SCHEDULE -> listOf(
            Row("Town to Campus", "6:30am, 7:30am, 12:30pm"),
            Row("Campus to Town", "1:00pm, 5:30pm, 7:00pm"),
            Row("Weekend shuttle", "Saturday 10:00am and 4:00pm", "Changed", Tone.WARN),
        )
        Feature.UNIVERSITY_OVERVIEW -> listOf(
            Row("Students enrolled", "4,820 across 5 faculties"),
            Row("Teaching staff", "312 lecturers"),
            Row("Support staff", "186 staff"),
            Row("Fees collected this semester", "KES 182M of KES 240M", "76%", Tone.WARN),
            Row("Average class attendance", "Last 30 days", "84%", Tone.GOOD),
        )
        Feature.FACULTY_DEPARTMENTS -> listOf(
            Row("Computer Science", "HOD: Dr. Ibrahim Noor, 620 students"),
            Row("Mathematics", "HOD: Dr. Lucy Wairimu, 410 students"),
            Row("Physics", "HOD: Dr. Hassan Abdi, 280 students"),
            Row("Biology", "HOD: Dr. Ann Chebet, 350 students"),
        )
        Feature.CLUBS -> listOf(
            Row("Computer Science Club", "86 members, meets Tuesdays"),
            Row("Debate Society", "54 members, meets Thursdays"),
            Row("Football Club", "40 members, trains daily"),
        )
        Feature.ADMISSIONS -> listOf(
            Row("January intake applications", "1,240 received", "Open", Tone.WARN),
            Row("Admission letters sent", "980 of 1,050 admitted", "93%", Tone.GOOD),
        )
        Feature.ACADEMIC_CALENDAR -> listOf(
            Row("Teaching ends", "15 Nov"),
            Row("Exams", "18 Nov to 29 Nov"),
            Row("Results release", "16 Dec"),
            Row("January semester opens", "6 Jan"),
        )
        Feature.EXAM_TIMETABLE -> listOf(
            Row("CSC 201 Data Structures", "Mon 18 Nov, 9:00am, Exam Hall A"),
            Row("CSC 205 Databases", "Wed 20 Nov, 2:00pm, Exam Hall B"),
            Row("MAT 202 Linear Algebra", "Fri 22 Nov, 9:00am, Exam Hall A"),
        )
        Feature.COURSE_ALLOCATION -> listOf(
            Row("CSC 201 Data Structures", "Mr. Kevin Kamau"),
            Row("CSC 205 Databases", "Ms. Joy Muthoni"),
            Row("CSC 210 Networks", "Unassigned", "Needs lecturer", Tone.WARN),
        )
        Feature.DEPARTMENT_STAFF -> listOf(
            Row("Mr. Kevin Kamau", "Lecturer, 3 courses"),
            Row("Ms. Joy Muthoni", "Lecturer, 2 courses"),
            Row("Mr. Felix Oduor", "Lab Technician"),
        )
        Feature.TIMETABLE -> listOf(
            Row("Monday", "CSC 201 Data Structures, 8:00am, Hall 3"),
            Row("Tuesday", "CSC 205 Databases, 10:00am, Lab 1"),
            Row("Wednesday", "MAT 202 Linear Algebra, 2:00pm, Hall 5"),
            Row("Thursday", "CSC 210 Networks, 9:00am, Lab 2"),
        )
        Feature.MY_COURSES -> listOf(
            Row("CSC 201 Data Structures", "Notes: Week 8 Trees uploaded", "New", Tone.WARN),
            Row("CSC 205 Databases", "Assignment 2 due 4 Nov"),
            Row("CSC 210 Networks", "No new updates"),
        )
        Feature.ATTENDANCE -> listOf(
            Row("CSC 201, Mon 28 Oct", "52 of 60 present", "87%", Tone.GOOD),
            Row("CSC 201, Mon 21 Oct", "45 of 60 present", "75%", Tone.WARN),
        )
        Feature.MARKS_ENTRY -> listOf(
            Row("CSC 201 CAT 1", "60 of 60 marks entered", "Complete", Tone.GOOD),
            Row("CSC 205 CAT 1", "48 of 55 marks entered", "In progress", Tone.WARN),
        )
        Feature.LIBRARY -> listOf(
            Row("Introduction to Algorithms", "Due 5 Nov", "Borrowed"),
            Row("Database System Concepts", "Ready for pickup", "Reserved", Tone.GOOD),
        )
        Feature.FEE_COLLECTION -> listOf(
            Row("Faculty of Science", "KES 58M of KES 72M", "81%", Tone.GOOD),
            Row("Faculty of Business", "KES 44M of KES 63M", "70%", Tone.WARN),
            Row("Students with balances", "1,120 students", "Follow up", Tone.WARN),
        )
        Feature.BOOK_LOANS -> listOf(
            Row("Introduction to Algorithms", "Zainab Omar, due 5 Nov"),
            Row("Linear Algebra Done Right", "Brian Kiprono, due 28 Oct", "Overdue", Tone.BAD),
        )
        Feature.STAFF_RECORDS -> listOf(
            Row("Teaching staff", "312 records"),
            Row("Support staff", "186 records"),
            Row("Contracts expiring this year", "14 staff", "Review", Tone.WARN),
        )
        Feature.HEALTH_ALERTS -> listOf(
            Row("Flu season", "Free flu shots at the Health Centre this week"),
            Row("Stay hydrated", "Water points are available at every block"),
        )
        Feature.VISITOR_LOG -> listOf(
            Row("John Kariuki", "Visiting Registrar's Office, in 9:10am", "On campus", Tone.WARN),
            Row("Delivery: Office supplies", "Finance Office, in 8:30am, out 9:00am", "Left", Tone.GOOD),
        )
        Feature.INCIDENT_LOG -> listOf(
            Row("Lost phone", "Library, 28 Oct", "Resolved", Tone.GOOD),
            Row("Broken gate barrier", "Main Gate, 29 Oct", "Open", Tone.WARN),
        )
        Feature.WORK_SCHEDULE -> listOf(
            Row("6:00am to 8:00am", "Lecture Halls 1 to 6"),
            Row("8:00am to 11:00am", "Administration Block"),
            Row("2:00pm to 4:00pm", "Library and Cafeteria"),
        )
        Feature.ROOM_ALLOCATION -> listOf(
            Row("Hostel A", "120 of 120 rooms filled", "Full"),
            Row("Hostel B", "98 of 110 rooms filled", "12 free", Tone.GOOD),
            Row("Hostel C", "80 of 100 rooms filled", "20 free", Tone.GOOD),
        )
        else -> emptyList()
    }
}
