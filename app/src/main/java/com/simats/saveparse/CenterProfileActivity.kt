package com.simats.saveparse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CenterProfileActivity : AppCompatActivity() {

    private lateinit var tvCenterName: TextView
    private lateinit var tvCenterPhone: TextView
    private lateinit var tvInitials: TextView
    private lateinit var tvCasesHandled: TextView
    private lateinit var tvDonationsReceived: TextView
    private lateinit var bottomNav: BottomNavigationView

    private var centerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_profile)

        // Initialize views
        tvCenterName = findViewById(R.id.tvCenterName)
        tvCenterPhone = findViewById(R.id.tvCenterPhone)
        tvInitials = findViewById(R.id.tvInitials)
        tvCasesHandled = findViewById(R.id.tvCasesHandled)
        tvDonationsReceived = findViewById(R.id.tvDonationsReceived)
        bottomNav = findViewById(R.id.bottomNav)

        // Get center data from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)
        val centerName = sharedPref.getString("center_name", "Rescue Center") ?: "Rescue Center"
        val centerPhone = sharedPref.getString("center_phone", "") ?: ""

        // Set center info
        tvCenterName.text = centerName
        tvCenterPhone.text = if (centerPhone.isNotEmpty()) "+91 $centerPhone" else ""

        // Set initials
        val initials = centerName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        tvInitials.text = if (initials.isNotEmpty()) initials else "RC"

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Cases Handled navigation
        findViewById<CardView>(R.id.cardCasesHandled).setOnClickListener {
            startActivity(Intent(this, CenterCasesHandledActivity::class.java))
        }

        // Donation History navigation
        findViewById<CardView>(R.id.cardDonationHistory).setOnClickListener {
            startActivity(Intent(this, CenterDonationsReceivedActivity::class.java))
        }

        // Logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        // Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_center_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, CenterDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cases -> {
                    startActivity(Intent(this, AcceptedCasesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_center_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_center_profile
        loadProfileStats()
    }

    private fun loadProfileStats() {
        if (centerId == -1) {
            return
        }

        Log.d("CenterProfileActivity", "Loading profile stats for center_id: $centerId")

        ApiClient.api.getCenterProfile(centerId).enqueue(object : Callback<CenterProfileResponse> {
            override fun onResponse(
                call: Call<CenterProfileResponse>,
                response: Response<CenterProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val profileData = response.body()!!

                    // Update center info from API
                    profileData.center?.let { center ->
                        tvCenterName.text = center.center_name ?: "Rescue Center"
                        tvCenterPhone.text = center.phone?.let { "+91 $it" } ?: ""

                        // Update initials
                        val name = center.center_name ?: "RC"
                        val initials = name.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                        tvInitials.text = if (initials.isNotEmpty()) initials else "RC"
                        
                        // Save phone to SharedPreferences for future use
                        center.phone?.let { phone ->
                            val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("center_phone", phone).apply()
                        }
                    }

                    // Update stats
                    profileData.stats?.let { stats ->
                        tvCasesHandled.text = stats.total_cases_handled.toString()
                        tvDonationsReceived.text = "₹${stats.total_amount_received.toInt()}"
                    }

                } else {
                    Log.e("CenterProfileActivity", "API failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CenterProfileResponse>, t: Throwable) {
                Log.e("CenterProfileActivity", "Network failure: ${t.message}", t)
            }
        })
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Clear SharedPreferences
                val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // Navigate to login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
