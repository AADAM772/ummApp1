package com.example.ummapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.ummapp.data.CampusHub
import com.example.ummapp.data.Feature
import com.example.ummapp.data.Role
import com.example.ummapp.data.Session
import com.example.ummapp.data.User
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Role-based dashboard shown after login. */
class MainActivity : AppCompatActivity() {

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = Session.currentUser(this) ?: run {
            goToLogin()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main).applySystemBarPadding()

        findViewById<TextView>(R.id.userName).text = user.name
        findViewById<TextView>(R.id.userRole).text = user.role.title
        findViewById<TextView>(R.id.userDepartment).text = user.department
        findViewById<View>(R.id.logoutButton).setOnClickListener {
            Session.logout(this)
            goToLogin()
        }

    }

    // Rebuilt on every return to the dashboard so badge counts stay current.
    override fun onResume() {
        super.onResume()
        refreshCards()
    }

    private fun refreshCards() {
        addFeatureCards(findViewById(R.id.roleGrid), user.role.features)
        addFeatureCards(findViewById(R.id.sharedGrid), Role.SHARED_FEATURES - user.role.features.toSet())
    }

    private fun addFeatureCards(grid: GridLayout, features: List<Feature>) {
        grid.removeAllViews()
        val margin = resources.getDimensionPixelSize(R.dimen.card_spacing) / 2
        features.forEach { feature ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_feature, grid, false) as MaterialCardView
            card.findViewById<TextView>(R.id.featureIcon).text = feature.icon
            card.findViewById<TextView>(R.id.featureTitle).text = feature.title
            val count = CampusHub.badgeCount(feature, user)
            card.findViewById<TextView>(R.id.featureBadge).apply {
                text = count.toString()
                isVisible = count > 0
            }
            if (feature == Feature.EMERGENCY) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red_light))
            }
            card.setOnClickListener { openFeature(feature) }
            card.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
            ).apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                setMargins(margin, margin, margin, margin)
            }
            grid.addView(card)
        }
    }

    private fun openFeature(feature: Feature) {
        if (feature == Feature.EMERGENCY) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.emergency_title)
                .setMessage(R.string.emergency_message)
                .setPositiveButton(R.string.emergency_send) { _, _ ->
                    CampusHub.raiseEmergency(user)
                    Toast.makeText(this, R.string.emergency_sent, Toast.LENGTH_LONG).show()
                    refreshCards()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        startActivity(Intent(this, FeatureActivity::class.java).putExtra(FeatureActivity.EXTRA_FEATURE, feature.name))
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
