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

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvTotalCases: TextView
    private lateinit var tvTotalDonations: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvTotalCases = findViewById(R.id.tvTotalCases)
        tvTotalDonations = findViewById(R.id.tvTotalDonations)

        // Get user data from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val userName = sharedPref.getString("user_name", "User") ?: "User"
        val userEmail = sharedPref.getString("user_email", "") ?: ""
        val userPhone = sharedPref.getString("user_phone", "") ?: ""

        // Set user info
        findViewById<TextView>(R.id.tvUserName).text = userName
        findViewById<TextView>(R.id.tvUserEmail).text = userEmail
        findViewById<TextView>(R.id.tvUserPhone).text = "+91 $userPhone"

        // Set initials
        val initials = userName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        findViewById<TextView>(R.id.tvInitials).text = if (initials.isNotEmpty()) initials else "U"

        // Load stats from API
        if (userId != -1) {
            loadCasesCount(userId)
            loadDonationsTotal(userId)
        }

        // Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Case History
        findViewById<CardView>(R.id.cardCaseHistory).setOnClickListener {
            startActivity(Intent(this, UserCaseHistoryActivity::class.java))
        }

        // Donation History
        findViewById<CardView>(R.id.cardDonationHistory).setOnClickListener {
            startActivity(Intent(this, UserDonationHistoryActivity::class.java))
        }

        // Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, OngoingCasesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId != -1) {
            loadCasesCount(userId)
            loadDonationsTotal(userId)
        }
    }

    private fun loadCasesCount(userId: Int) {
        ApiClient.api.getUserCases(userId).enqueue(object : Callback<UserCasesResponse> {
            override fun onResponse(
                call: Call<UserCasesResponse>,
                response: Response<UserCasesResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val totalCases = response.body()?.totalCases ?: 0
                    tvTotalCases.text = totalCases.toString()
                }
            }

            override fun onFailure(call: Call<UserCasesResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Failed to load cases count", t)
            }
        })
    }

    private fun loadDonationsTotal(userId: Int) {
        ApiClient.api.getUserDonations(userId).enqueue(object : Callback<UserDonationsResponse> {
            override fun onResponse(
                call: Call<UserDonationsResponse>,
                response: Response<UserDonationsResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val totalAmount = response.body()?.totalAmount ?: 0.0
                    tvTotalDonations.text = "₹${totalAmount.toInt()}"
                }
            }

            override fun onFailure(call: Call<UserDonationsResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Failed to load donations", t)
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