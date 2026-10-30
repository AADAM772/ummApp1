package com.example.ummapp

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.ummapp.data.MockData
import com.example.ummapp.data.Row
import com.example.ummapp.data.Session
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Session.currentUser(this) != null) {
            openDashboard()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        findViewById<android.view.View>(R.id.main).applySystemBarPadding()

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        errorText = findViewById(R.id.errorText)

        findViewById<android.view.View>(R.id.loginButton).setOnClickListener { attemptLogin() }
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) attemptLogin()
            false
        }

        // Demo only: list the test accounts so they can be filled in with one tap.
        val demoAccounts = findViewById<LinearLayout>(R.id.demoAccounts)
        MockData.users.forEach { user ->
            addRowView(demoAccounts, Row(user.role.title, "${user.email}\nPassword: ${user.password}")) {
                emailInput.setText(user.email)
                passwordInput.setText(user.password)
                errorText.isVisible = false
            }
        }
    }

    private fun attemptLogin() {
        val user = MockData.login(emailInput.text.toString(), passwordInput.text.toString())
        if (user == null) {
            errorText.isVisible = true
            return
        }
        Session.login(this, user)
        openDashboard()
    }

    private fun openDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
