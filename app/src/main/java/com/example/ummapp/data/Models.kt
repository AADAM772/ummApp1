package com.example.ummapp.data

enum class Feature(val title: String, val icon: String) {
    // Shared by everyone
    MY_ID("My ID", "🪪"),
    ANNOUNCEMENTS("Announcements", "📢"),
    MESSAGES("Messages", "💬"),
    MY_REQUESTS("My Requests", "📤"),
    EVENTS("Events", "🎉"),
    DIRECTORY("Directory", "📇"),
    CAMPUS_MAP("Campus Map", "🗺️"),
    MENU("Cafeteria Menu", "🍽️"),
    BUS_SCHEDULE("Bus Schedule", "🚌"),
    EMERGENCY("Emergency", "🚨"),

    // Leadership
    UNIVERSITY_OVERVIEW("University Overview", "📊"),
    APPROVALS("Pending Approvals", "✅"),
    POST_ANNOUNCEMENT("Post Announcement", "✏️"),
    FACULTY_DEPARTMENTS("Departments", "🏛️"),
    RESULTS_APPROVAL("Results Approval", "📝"),
    WELFARE_CASES("Welfare Cases", "🤝"),
    CLUBS("Clubs and Societies", "⚽"),

    // Registrar
    ADMISSIONS("Admissions", "🎓"),
    ACADEMIC_CALENDAR("Academic Calendar", "📅"),
    EXAM_TIMETABLE("Exam Timetable", "🗓️"),
    TRANSCRIPTS("Transcript Requests", "📄"),

    // Academic department
    COURSE_ALLOCATION("Course Allocation", "📚"),
    DEPARTMENT_STAFF("Department Staff", "👥"),
    TIMETABLE("Timetable", "⏰"),
    MY_COURSES("My Courses", "📘"),
    ATTENDANCE("Attendance", "🙋"),
    MARKS_ENTRY("Marks Entry", "🧮"),
    OFFICE_HOURS("Office Hours", "🕑"),
    RESULTS("My Results", "🏅"),
    FEES("Fees", "💳"),
    LIBRARY("Library", "📖"),

    // Administrative offices
    FEE_COLLECTION("Fee Collection", "💰"),
    PAYMENT_REQUESTS("Payment Requests", "🧾"),
    BOOK_LOANS("Book Loans", "📕"),
    SUPPORT_TICKETS("Support Tickets", "💻"),
    LEAVE_REQUESTS("Leave Requests", "🌴"),
    STAFF_RECORDS("Staff Records", "🗂️"),

    // Support staff
    CLINIC_APPOINTMENTS("Clinic Appointments", "🩺"),
    HEALTH_ALERTS("Health Alerts", "💊"),
    EMERGENCY_ALERTS("Emergency Alerts", "🚑"),
    VISITOR_LOG("Visitor Log", "🚪"),
    INCIDENT_LOG("Incident Log", "📋"),
    MY_TASKS("My Tasks", "🧹"),
    WORK_SCHEDULE("Work Schedule", "📆"),
    ROOM_ALLOCATION("Room Allocation", "🛏️"),
    HOSTEL_COMPLAINTS("Hostel Complaints", "📨");

    /** True when this screen lists requests sent to the user's office. */
    val isInbox: Boolean get() = RequestType.entries.any { it.inbox == this }
}

enum class Role(val title: String, val features: List<Feature>) {
    VICE_CHANCELLOR("Vice Chancellor", listOf(Feature.UNIVERSITY_OVERVIEW, Feature.APPROVALS, Feature.POST_ANNOUNCEMENT)),
    DEPUTY_VC("Deputy Vice Chancellor", listOf(Feature.UNIVERSITY_OVERVIEW, Feature.APPROVALS, Feature.POST_ANNOUNCEMENT)),
    DEAN("Dean of Faculty", listOf(Feature.FACULTY_DEPARTMENTS, Feature.RESULTS_APPROVAL, Feature.POST_ANNOUNCEMENT)),
    DEAN_OF_STUDENTS("Dean of Students", listOf(Feature.WELFARE_CASES, Feature.CLUBS, Feature.POST_ANNOUNCEMENT)),
    REGISTRAR("Registrar", listOf(Feature.RESULTS_APPROVAL, Feature.TRANSCRIPTS, Feature.ADMISSIONS, Feature.ACADEMIC_CALENDAR, Feature.EXAM_TIMETABLE, Feature.POST_ANNOUNCEMENT)),
    HOD("Head of Department", listOf(Feature.RESULTS_APPROVAL, Feature.COURSE_ALLOCATION, Feature.DEPARTMENT_STAFF, Feature.TIMETABLE, Feature.POST_ANNOUNCEMENT)),
    LECTURER("Lecturer", listOf(Feature.MARKS_ENTRY, Feature.OFFICE_HOURS, Feature.MY_COURSES, Feature.TIMETABLE, Feature.ATTENDANCE)),
    STUDENT("Student", listOf(Feature.TIMETABLE, Feature.MY_COURSES, Feature.RESULTS, Feature.FEES, Feature.EXAM_TIMETABLE, Feature.LIBRARY)),
    FINANCE_OFFICER("Finance Officer (Bursar)", listOf(Feature.PAYMENT_REQUESTS, Feature.FEE_COLLECTION)),
    LIBRARIAN("Librarian", listOf(Feature.BOOK_LOANS)),
    ICT_OFFICER("ICT Officer", listOf(Feature.SUPPORT_TICKETS)),
    HR_OFFICER("HR Officer", listOf(Feature.LEAVE_REQUESTS, Feature.STAFF_RECORDS)),
    NURSE("Nurse", listOf(Feature.EMERGENCY_ALERTS, Feature.CLINIC_APPOINTMENTS, Feature.HEALTH_ALERTS)),
    SECURITY("Security Officer", listOf(Feature.EMERGENCY_ALERTS, Feature.VISITOR_LOG, Feature.INCIDENT_LOG)),
    CLEANER("Cleaning and Maintenance", listOf(Feature.MY_TASKS, Feature.WORK_SCHEDULE)),
    CAFETERIA("Cafeteria Staff", listOf(Feature.MENU)),
    HOSTEL_WARDEN("Hostel Warden", listOf(Feature.HOSTEL_COMPLAINTS, Feature.ROOM_ALLOCATION)),
    TRANSPORT("Transport Officer", listOf(Feature.BUS_SCHEDULE));

    companion object {
        val SHARED_FEATURES = listOf(
            Feature.MY_ID, Feature.ANNOUNCEMENTS, Feature.MESSAGES, Feature.MY_REQUESTS, Feature.EVENTS, Feature.DIRECTORY,
            Feature.CAMPUS_MAP, Feature.MENU, Feature.BUS_SCHEDULE, Feature.EMERGENCY,
        )
        val EVERYONE: Set<Role> get() = entries.toSet()
        val STAFF: Set<Role> get() = entries.toSet() - STUDENT
    }
}

/**
 * Every kind of request one part of the university can send to another.
 * [chain] lists the offices that must approve it, in order. The request is
 * complete once the last office in the chain approves it.
 */
enum class RequestType(
    val label: String,
    val chain: List<Role>,
    val inbox: Feature,
    val acceptAction: String,
    val acceptedLabel: String,
    val senders: Set<Role>,
) {
    REPAIR("Repair or cleaning", listOf(Role.CLEANER), Feature.MY_TASKS, "Mark done", "Done", Role.EVERYONE),
    IT_SUPPORT("IT support", listOf(Role.ICT_OFFICER), Feature.SUPPORT_TICKETS, "Mark resolved", "Resolved", Role.EVERYONE),
    CLINIC("Clinic appointment", listOf(Role.NURSE), Feature.CLINIC_APPOINTMENTS, "Confirm", "Confirmed", Role.EVERYONE),
    LEAVE("Leave request", listOf(Role.HR_OFFICER), Feature.LEAVE_REQUESTS, "Approve", "Approved", Role.STAFF),
    PAYMENT("Payment request", listOf(Role.FINANCE_OFFICER), Feature.PAYMENT_REQUESTS, "Approve", "Approved", Role.STAFF),
    PROPOSAL(
        "Proposal for VC approval", listOf(Role.DEPUTY_VC, Role.VICE_CHANCELLOR), Feature.APPROVALS, "Approve", "Approved",
        setOf(Role.DEAN, Role.DEAN_OF_STUDENTS, Role.REGISTRAR, Role.HOD, Role.FINANCE_OFFICER, Role.HR_OFFICER, Role.LIBRARIAN, Role.ICT_OFFICER),
    ),
    RESULTS("Exam results", listOf(Role.HOD, Role.DEAN, Role.REGISTRAR), Feature.RESULTS_APPROVAL, "Approve", "Released", setOf(Role.LECTURER)),
    HOSTEL("Hostel complaint", listOf(Role.HOSTEL_WARDEN), Feature.HOSTEL_COMPLAINTS, "Mark resolved", "Resolved", setOf(Role.STUDENT)),
    TRANSCRIPT("Transcript request", listOf(Role.REGISTRAR), Feature.TRANSCRIPTS, "Issue", "Issued", setOf(Role.STUDENT)),
    WELFARE("Welfare support", listOf(Role.DEAN_OF_STUDENTS), Feature.WELFARE_CASES, "Mark resolved", "Resolved", setOf(Role.STUDENT)),
    OFFICE_HOURS("Office hours booking", listOf(Role.LECTURER), Feature.OFFICE_HOURS, "Confirm", "Confirmed", setOf(Role.STUDENT)),
}

enum class RequestStatus { PENDING, ACCEPTED, REJECTED }

class Request(
    val id: Int,
    val type: RequestType,
    val fromEmail: String,
    val title: String,
    val details: String,
    val time: Long,
) {
    var assignedTo: Role = type.chain.first()
    var status = RequestStatus.PENDING
    val history = mutableListOf<String>()

    /** Offices that have seen this request, so it stays in their list after they act on it. */
    val involved = mutableSetOf(assignedTo)
}

class Message(
    val id: Int,
    val fromEmail: String,
    val toEmail: String,
    val text: String,
    val time: Long,
    var read: Boolean = false,
)

class EmergencyAlert(
    val id: Int,
    val reportedBy: String,
    val location: String,
    val time: Long,
    var respondedBy: String? = null,
)

/** An entry on a list that one office maintains and others read, such as the menu. */
class BoardItem(
    val id: Int,
    val title: String,
    val details: String,
    val tag: String = "",
    val tone: Tone = Tone.NEUTRAL,
    val time: Long? = null,
)

class Payment(
    val id: Int,
    val email: String,
    val amount: Int,
    val phone: String,
    val time: Long,
)

data class User(
    val email: String,
    val password: String,
    val name: String,
    val role: Role,
    val department: String,
)

enum class Tone { NEUTRAL, GOOD, WARN, BAD }

/** One line item shown on a feature screen. Rows with an [id] can be tapped. [time] shows as a timestamp. */
data class Row(
    val title: String,
    val subtitle: String = "",
    val tag: String = "",
    val tone: Tone = Tone.NEUTRAL,
    val id: Int? = null,
    val time: Long? = null,
)

data class Announcement(
    val title: String,
    val message: String,
    val authorEmail: String,
    val time: Long,
)
