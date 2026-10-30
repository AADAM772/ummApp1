package com.example.ummapp.data

import java.util.Locale

/**
 * Connects every role in the university. Messages, requests, emergency alerts,
 * announcements, boards and payments created by one user show up for the users
 * they are meant for.
 *
 * Everything is held in memory as a stand-in for a backend, so it resets when
 * the app process ends. Log out and log in as another account to see the other side.
 */
object CampusHub {

    private const val SEMESTER_FEES = 65_000

    private var nextId = 1
    private val announcements = mutableListOf<Announcement>()
    private val messages = mutableListOf<Message>()
    private val requests = mutableListOf<Request>()
    private val alerts = mutableListOf<EmergencyAlert>()
    private val payments = mutableListOf<Payment>()
    private val releasedResults = mutableListOf<String>()

    /** Lists that one office maintains and everyone else with the screen can read. */
    private val boardEditors = mapOf(
        Feature.EVENTS to setOf(Role.DEAN_OF_STUDENTS, Role.REGISTRAR),
        Feature.CLUBS to setOf(Role.DEAN_OF_STUDENTS),
        Feature.MENU to setOf(Role.CAFETERIA),
        Feature.BUS_SCHEDULE to setOf(Role.TRANSPORT),
        Feature.HEALTH_ALERTS to setOf(Role.NURSE),
        Feature.EXAM_TIMETABLE to setOf(Role.REGISTRAR),
        Feature.ACADEMIC_CALENDAR to setOf(Role.REGISTRAR),
        Feature.COURSE_ALLOCATION to setOf(Role.HOD),
        Feature.MY_COURSES to setOf(Role.LECTURER),
        Feature.VISITOR_LOG to setOf(Role.SECURITY),
        Feature.INCIDENT_LOG to setOf(Role.SECURITY),
    )
    private val boards = mutableMapOf<Feature, MutableList<BoardItem>>()

    init {
        seed()
    }

    private fun now() = System.currentTimeMillis()

    /** A time [minutes] before now, used to give the seeded data realistic timestamps. */
    private fun ago(minutes: Long) = now() - minutes * 60_000

    private fun nameOf(email: String) = MockData.findUser(email)?.name ?: email

    fun kes(amount: Int) = String.format(Locale.US, "KES %,d", amount)

    /** The person who currently staffs [role]. Each role has one demo account. */
    fun userFor(role: Role): User? = MockData.users.find { it.role == role }

    /** Rows for screens backed by shared data, or null when the screen only shows static content. */
    fun liveRows(feature: Feature, user: User): List<Row>? = when {
        feature == Feature.MY_ID -> idRows(user)
        feature == Feature.ANNOUNCEMENTS -> announcementRows(null)
        feature == Feature.POST_ANNOUNCEMENT -> announcementRows(user)
        feature == Feature.MESSAGES -> messageRows(user)
        feature == Feature.MY_REQUESTS -> requests.filter { it.fromEmail == user.email }.map { it.toRow(user) }
        feature == Feature.DIRECTORY -> directoryRows(user)
        feature == Feature.EMERGENCY_ALERTS -> alertRows()
        feature == Feature.RESULTS -> releasedResults.map { Row(it, "Semester 2, released by the Registrar", "New", Tone.GOOD) } +
            MockData.staticRows(feature)
        feature == Feature.FEES -> feeRows(user)
        feature == Feature.FEE_COLLECTION -> paymentRows() + MockData.staticRows(feature)
        feature.isInbox -> inboxRows(user, feature)
        feature in boardEditors -> board(feature).map {
            Row(it.title, it.details, it.tag, it.tone, if (canEdit(feature, user.role)) it.id else null, it.time)
        }
        else -> null
    }

    // Profile

    private fun idRows(user: User): List<Row> {
        val number = String.format(Locale.US, "UMM-%04d", MockData.users.indexOf(user) + 1)
        return listOf(
            Row(user.name, user.role.title),
            Row("ID number", number, "Active", Tone.GOOD),
            Row("Department", user.department),
            Row("Email", user.email),
            Row("Valid until", "31 Aug 2027"),
        )
    }

    private fun directoryRows(user: User) = MockData.users.mapIndexedNotNull { index, other ->
        if (other == user) null else Row(other.name, "${other.role.title}, ${other.department}\n${other.email}", id = index)
    }

    // Announcements

    fun postAnnouncement(user: User, title: String, message: String) {
        announcements.add(0, Announcement(title, message, user.email, now()))
    }

    private fun announcementRows(author: User?): List<Row> = announcements
        .filter { author == null || it.authorEmail == author.email }
        .map { Row(it.title, "${it.message}\nPosted by ${nameOf(it.authorEmail)}", time = it.time) }

    // Messages

    fun sendMessage(fromEmail: String, toEmail: String, text: String, time: Long = now()) {
        messages.add(0, Message(nextId++, fromEmail, toEmail, text, time))
    }

    /** Returns the message and marks it read if [user] is the recipient. */
    fun openMessage(user: User, id: Int): Message? =
        messages.find { it.id == id }?.also { if (it.toEmail == user.email) it.read = true }

    private fun messageRows(user: User): List<Row> = messages
        .filter { it.toEmail == user.email || it.fromEmail == user.email }
        .map {
            val incoming = it.toEmail == user.email
            Row(
                title = if (incoming) "From ${nameOf(it.fromEmail)}" else "To ${nameOf(it.toEmail)}",
                subtitle = it.text,
                tag = if (incoming && !it.read) "New" else "",
                tone = Tone.WARN,
                id = it.id,
                time = it.time,
            )
        }

    // Requests

    fun requestTypesFor(role: Role) = RequestType.entries.filter { role in it.senders }

    fun findRequest(id: Int) = requests.find { it.id == id }

    fun submitRequest(fromEmail: String, type: RequestType, title: String, details: String, time: Long = now()) {
        val request = Request(nextId++, type, fromEmail, title, details, time)
        request.history += "Sent by ${nameOf(fromEmail)} to ${request.assignedTo.title}, ${fullTime(time)}"
        requests.add(0, request)
    }

    /** Approves the request and passes it to the next office in its chain, or completes it. */
    fun accept(request: Request, user: User) {
        val chain = request.type.chain
        val next = chain.getOrNull(chain.indexOf(request.assignedTo) + 1)
        val update = if (next != null) {
            request.assignedTo = next
            request.involved += next
            "Approved by ${user.name} and sent to ${next.title}"
        } else {
            request.status = RequestStatus.ACCEPTED
            onCompleted(request, user)
            "${request.type.acceptedLabel} by ${user.name}"
        }
        request.history += "$update, ${fullTime(now())}"
        notifySender(request, user, update)
    }

    fun reject(request: Request, user: User) {
        request.status = RequestStatus.REJECTED
        val update = "Rejected by ${user.name}"
        request.history += "$update, ${fullTime(now())}"
        notifySender(request, user, update)
    }

    private fun notifySender(request: Request, user: User, update: String) {
        sendMessage(user.email, request.fromEmail, "Update on your ${request.type.label.lowercase()} \"${request.title}\": $update.")
    }

    private fun onCompleted(request: Request, user: User) {
        if (request.type == RequestType.RESULTS) {
            releasedResults.add(0, request.title)
            postAnnouncement(user, "Results released: ${request.title}", "Students can now view these results under My Results.")
        }
    }

    fun describe(request: Request): String = buildString {
        append(request.type.label).append(" from ").append(nameOf(request.fromEmail)).append("\n\n")
        append(request.details).append("\n\nProgress:\n")
        request.history.forEach { append("• ").append(it).append("\n") }
    }

    private fun Request.toRow(viewer: User): Row {
        val (tag, tone) = when (status) {
            RequestStatus.ACCEPTED -> type.acceptedLabel to Tone.GOOD
            RequestStatus.REJECTED -> "Rejected" to Tone.BAD
            RequestStatus.PENDING ->
                if (assignedTo == viewer.role) "Action needed" to Tone.WARN
                else "With ${assignedTo.title}" to Tone.NEUTRAL
        }
        val from = if (fromEmail == viewer.email) type.label else "${type.label} from ${nameOf(fromEmail)}"
        return Row(title, "$from\n$details", tag, tone, id, time)
    }

    private fun inboxRows(user: User, feature: Feature): List<Row> = requests
        .filter { it.type.inbox == feature && user.role in it.involved }
        .sortedBy { !(it.status == RequestStatus.PENDING && it.assignedTo == user.role) }
        .map { it.toRow(user) }

    // Boards

    fun canEdit(feature: Feature, role: Role) = role in boardEditors[feature].orEmpty()

    private fun board(feature: Feature) = boards.getOrPut(feature) {
        MockData.staticRows(feature).map { BoardItem(nextId++, it.title, it.subtitle, it.tag, it.tone) }.toMutableList()
    }

    fun addBoardItem(feature: Feature, title: String, details: String) {
        board(feature).add(0, BoardItem(nextId++, title, details, time = now()))
    }

    fun removeBoardItem(feature: Feature, id: Int) {
        board(feature).removeAll { it.id == id }
    }

    // Fees

    private fun paidBy(email: String) = payments.filter { it.email == email }.sumOf { it.amount }

    /** Records a mock M-Pesa payment and sends the student a receipt from the Finance Office. */
    fun payFees(user: User, amount: Int, phone: String) {
        payments.add(0, Payment(nextId++, user.email, amount, phone, now()))
        val balance = SEMESTER_FEES - paidBy(user.email)
        val bursar = userFor(Role.FINANCE_OFFICER) ?: return
        sendMessage(bursar.email, user.email, "Payment of ${kes(amount)} received from $phone. Your balance is now ${kes(balance)}. Thank you.")
    }

    private fun feeRows(user: User): List<Row> {
        val paid = paidBy(user.email)
        val balance = SEMESTER_FEES - paid
        return listOf(
            Row("Semester fees", kes(SEMESTER_FEES)),
            Row("Paid", kes(paid)),
            if (balance > 0) Row("Balance", "Due 15 Nov", kes(balance), Tone.WARN)
            else Row("Balance", "Fully paid", kes(0), Tone.GOOD),
        ) + payments.filter { it.email == user.email }.map {
            Row("Payment", "M-Pesa ${it.phone}", kes(it.amount), Tone.GOOD, time = it.time)
        }
    }

    private fun paymentRows(): List<Row> = payments.map {
        Row("Payment from ${nameOf(it.email)}", "M-Pesa ${it.phone}", kes(it.amount), Tone.GOOD, time = it.time)
    }

    // Emergencies

    fun raiseEmergency(user: User) {
        alerts.add(0, EmergencyAlert(nextId++, user.email, "Near ${user.department}", now()))
    }

    fun respondToEmergency(user: User, id: Int) {
        val alert = alerts.find { it.id == id } ?: return
        if (alert.respondedBy != null) return
        alert.respondedBy = user.email
        sendMessage(user.email, alert.reportedBy, "${user.role.title} has received your emergency alert and is on the way.")
    }

    private fun alertRows(): List<Row> = alerts.map {
        val reporter = MockData.findUser(it.reportedBy)
        Row(
            "Alert from ${reporter?.name ?: it.reportedBy}",
            "${reporter?.role?.title}\n${it.location}",
            it.respondedBy?.let { email -> "Responded: ${nameOf(email)}" } ?: "Active",
            if (it.respondedBy == null) Tone.BAD else Tone.GOOD,
            it.id,
            it.time,
        )
    }

    // Dashboard badges

    fun badgeCount(feature: Feature, user: User): Int = when {
        feature == Feature.MESSAGES -> messages.count { it.toEmail == user.email && !it.read }
        feature == Feature.EMERGENCY_ALERTS -> alerts.count { it.respondedBy == null }
        feature.isInbox -> requests.count {
            it.type.inbox == feature && it.assignedTo == user.role && it.status == RequestStatus.PENDING
        }
        else -> 0
    }

    private fun seed() {
        val hour = 60L
        val day = 24 * hour

        // Newest first, matching how new items are added.
        announcements += Announcement("Exam timetable released", "The end of semester exam timetable is now available under Exam Timetable.", "registrar@umm.edu", ago(3 * hour))
        announcements += Announcement("Hackathon this Friday", "Computer Science students are invited to the annual hackathon in Lab 2.", "hod.cs@umm.edu", ago(day + 2 * hour))
        announcements += Announcement("Graduation date confirmed", "The graduation ceremony will take place on 12 December.", "vc@umm.edu", ago(5 * day))

        // Oldest first, because each request is added to the top of the list.
        submitRequest("dean.science@umm.edu", RequestType.PROPOSAL, "New Data Science programme", "BSc Data Science starting September next year. Curriculum approved by the faculty board.", ago(4 * day))
        submitRequest("hod.cs@umm.edu", RequestType.PAYMENT, "Lab equipment", "20 new keyboards and mice for Lab 2. KES 340,000.", ago(3 * day))
        submitRequest("lecturer.cs@umm.edu", RequestType.LEAVE, "Annual leave", "2 Dec to 6 Dec. Classes covered by Ms. Joy Muthoni.", ago(2 * day))
        submitRequest("student@umm.edu", RequestType.HOSTEL, "Noise after 10pm", "Hostel C, Room 4 plays loud music every night.", ago(day + 5 * hour))
        submitRequest("student@umm.edu", RequestType.REPAIR, "Hostel B, Room 12", "Leaking tap in the bathroom.", ago(day))
        submitRequest("student@umm.edu", RequestType.OFFICE_HOURS, "Help with CSC 201 Trees", "Tuesday 2:00pm if available.", ago(6 * hour))
        submitRequest("lecturer.cs@umm.edu", RequestType.RESULTS, "CSC 201 Data Structures", "60 students, mean grade B. Marks sheet attached.", ago(4 * hour))
        submitRequest("student@umm.edu", RequestType.IT_SUPPORT, "Cannot log in to Wi-Fi", "My student account is rejected on UMM-Students.", ago(2 * hour))
        submitRequest("lecturer.cs@umm.edu", RequestType.REPAIR, "Lecture Hall 3", "Projector is not working.", ago(45))

        payments += Payment(nextId++, "student@umm.edu", 45_000, "0712 345 678", ago(58 * day))

        sendMessage("dos@umm.edu", "student@umm.edu", "Welcome back. The Student Affairs office is open 8am to 5pm.", ago(3 * day))
        sendMessage("hod.cs@umm.edu", "lecturer.cs@umm.edu", "Please submit the CSC 201 results by Friday.", ago(20))
    }
}
