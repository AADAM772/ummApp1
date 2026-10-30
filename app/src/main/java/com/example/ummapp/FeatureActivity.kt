package com.example.ummapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.ummapp.data.CampusHub
import com.example.ummapp.data.Feature
import com.example.ummapp.data.MockData
import com.example.ummapp.data.RequestStatus
import com.example.ummapp.data.RequestType
import com.example.ummapp.data.Session
import com.example.ummapp.data.User
import com.example.ummapp.data.fullTime
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Generic screen that lists the rows for one dashboard feature. */
class FeatureActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FEATURE = "feature"
    }

    private lateinit var user: User
    private lateinit var feature: Feature
    private lateinit var rowsContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = Session.currentUser(this) ?: run {
            finish()
            return
        }
        feature = Feature.valueOf(intent.getStringExtra(EXTRA_FEATURE) ?: return finish())

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContentView(R.layout.activity_feature)
        findViewById<View>(R.id.main).applySystemBarPadding()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "${feature.icon}  ${feature.title}"
        toolbar.subtitle = user.role.title
        toolbar.setNavigationOnClickListener { finish() }

        rowsContainer = findViewById(R.id.rowsContainer)
        emptyText = findViewById(R.id.emptyText)

        setUpActionButton(findViewById(R.id.actionButton))

        val hint = when {
            feature == Feature.MESSAGES -> R.string.hint_messages
            feature == Feature.MY_REQUESTS -> R.string.hint_my_requests
            feature == Feature.DIRECTORY -> R.string.hint_directory
            feature == Feature.EMERGENCY_ALERTS -> R.string.hint_alerts
            feature == Feature.MARKS_ENTRY -> R.string.hint_marks
            feature == Feature.FEES -> R.string.hint_fees
            feature.isInbox -> R.string.hint_inbox
            CampusHub.canEdit(feature, user.role) -> R.string.hint_board
            else -> null
        }
        findViewById<TextView>(R.id.hintText).apply {
            if (hint != null) setText(hint) else isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun setUpActionButton(button: MaterialButton) {
        when (feature) {
            Feature.MESSAGES -> {
                button.setText(R.string.action_new_message)
                button.setOnClickListener { composeMessage(null) }
            }
            Feature.MY_REQUESTS -> {
                button.setText(R.string.action_new_request)
                button.setOnClickListener { newRequest() }
            }
            Feature.MARKS_ENTRY -> {
                button.setText(R.string.action_submit_results)
                button.setOnClickListener {
                    showFormDialog(getString(R.string.action_submit_results), null, R.string.hint_course, R.string.hint_results_summary) { _, course, summary ->
                        CampusHub.submitRequest(user.email, RequestType.RESULTS, course, summary)
                        Toast.makeText(this, R.string.results_submitted, Toast.LENGTH_LONG).show()
                    }
                }
            }
            Feature.POST_ANNOUNCEMENT -> {
                button.setText(R.string.action_new_announcement)
                button.setOnClickListener {
                    showFormDialog(getString(R.string.action_new_announcement), null, R.string.hint_title, R.string.hint_message) { _, title, message ->
                        CampusHub.postAnnouncement(user, title, message)
                    }
                }
            }
            Feature.FEES -> {
                button.setText(R.string.action_pay_fees)
                button.setOnClickListener { payFees() }
            }
            else -> if (CampusHub.canEdit(feature, user.role)) {
                button.setText(R.string.action_add_item)
                button.setOnClickListener {
                    showFormDialog(getString(R.string.action_add_item), null, R.string.hint_title, R.string.hint_details) { _, title, details ->
                        CampusHub.addBoardItem(feature, title, details)
                    }
                }
            } else {
                button.isVisible = false
            }
        }
    }

    private fun payFees() {
        showFormDialog(getString(R.string.action_pay_fees), null, R.string.hint_amount, R.string.hint_phone) { _, amount, phone ->
            val value = amount.replace(",", "").toIntOrNull()
            if (value == null || value <= 0) {
                Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show()
            } else {
                CampusHub.payFees(user, value, phone)
                Toast.makeText(this, getString(R.string.payment_done, CampusHub.kes(value)), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun render() {
        rowsContainer.removeAllViews()
        val rows = MockData.rows(feature, user)
        emptyText.isVisible = rows.isEmpty()
        rows.forEach { row ->
            val id = row.id
            addRowView(rowsContainer, row, if (id == null) null else { { onRowClick(id) } })
        }
    }

    private fun onRowClick(id: Int) {
        when {
            feature == Feature.DIRECTORY -> composeMessage(MockData.users[id])
            feature == Feature.MESSAGES -> openMessage(id)
            feature == Feature.EMERGENCY_ALERTS -> {
                CampusHub.respondToEmergency(user, id)
                render()
            }
            feature == Feature.MY_REQUESTS || feature.isInbox -> openRequest(id)
            CampusHub.canEdit(feature, user.role) -> confirmRemove(id)
        }
    }

    private fun confirmRemove(id: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.remove_item_title)
            .setMessage(R.string.remove_item_message)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                CampusHub.removeBoardItem(feature, id)
                render()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openMessage(id: Int) {
        val message = CampusHub.openMessage(user, id) ?: return
        val incoming = message.toEmail == user.email
        val other = MockData.findUser(if (incoming) message.fromEmail else message.toEmail) ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(if (incoming) "From ${other.name}" else "To ${other.name}")
            .setMessage("${other.role.title}\n\n${message.text}\n\n🕒 ${fullTime(message.time)}")
            .setPositiveButton(R.string.action_reply) { _, _ -> composeMessage(other) }
            .setNegativeButton(R.string.action_close, null)
            .show()
        render()
    }

    private fun openRequest(id: Int) {
        val request = CampusHub.findRequest(id) ?: return
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(request.title)
            .setMessage(CampusHub.describe(request))

        if (request.status == RequestStatus.PENDING && request.assignedTo == user.role) {
            builder.setPositiveButton(request.type.acceptAction) { _, _ ->
                CampusHub.accept(request, user)
                render()
            }
            builder.setNegativeButton(R.string.action_reject) { _, _ ->
                CampusHub.reject(request, user)
                render()
            }
        } else {
            builder.setNegativeButton(R.string.action_close, null)
        }

        // The sender can message the office handling it, and the office can message the sender.
        val contact = if (request.fromEmail == user.email) CampusHub.userFor(request.assignedTo)
        else MockData.findUser(request.fromEmail)
        if (contact != null && contact != user) {
            builder.setNeutralButton(R.string.action_message) { _, _ -> composeMessage(contact) }
        }
        builder.show()
    }

    private fun newRequest() {
        val types = CampusHub.requestTypesFor(user.role)
        val labels = types.map { "${it.label} (to ${it.chain.first().title})" }
        showFormDialog(getString(R.string.action_new_request), labels, R.string.hint_title, R.string.hint_details) { index, title, details ->
            CampusHub.submitRequest(user.email, types[index], title, details)
        }
    }

    /** Opens a message form. When [to] is null the user picks a recipient. */
    private fun composeMessage(to: User?) {
        val recipients = if (to == null) MockData.users.filter { it != user } else listOf(to)
        val options = if (to == null) recipients.map { "${it.name}, ${it.role.title}" } else null
        val title = if (to == null) getString(R.string.action_new_message) else getString(R.string.message_to, to.name)
        showFormDialog(title, options, null, R.string.hint_message) { index, _, text ->
            CampusHub.sendMessage(user.email, recipients[index].email, text)
            Toast.makeText(this, R.string.message_sent, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a form with an optional picker and up to two text fields.
     * [onSubmit] receives the picked index (0 when there is no picker) and the field values.
     */
    private fun showFormDialog(
        title: String,
        options: List<String>?,
        hint1: Int?,
        hint2: Int,
        onSubmit: (Int, String, String) -> Unit,
    ) {
        val form = LayoutInflater.from(this).inflate(R.layout.dialog_form, null)
        val pickerLayout = form.findViewById<TextInputLayout>(R.id.pickerLayout)
        val picker = form.findViewById<MaterialAutoCompleteTextView>(R.id.picker)
        val field1Layout = form.findViewById<TextInputLayout>(R.id.field1Layout)
        val field1 = form.findViewById<TextInputEditText>(R.id.field1)
        val field2 = form.findViewById<TextInputEditText>(R.id.field2)

        var picked = if (options == null) 0 else -1
        if (options == null) {
            pickerLayout.isVisible = false
        } else {
            pickerLayout.setHint(R.string.hint_choose)
            picker.setSimpleItems(options.toTypedArray())
            picker.setOnItemClickListener { _, _, position, _ -> picked = position }
        }
        if (hint1 == null) field1Layout.isVisible = false else field1Layout.setHint(hint1)
        form.findViewById<TextInputLayout>(R.id.field2Layout).setHint(hint2)

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(form)
            .setPositiveButton(R.string.action_submit) { _, _ ->
                val first = field1.text.toString().trim()
                val second = field2.text.toString().trim()
                if (picked < 0 || (hint1 != null && first.isEmpty()) || second.isEmpty()) {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                } else {
                    onSubmit(picked, first, second)
                    render()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
