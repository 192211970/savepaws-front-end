package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalCases: TextView
    private lateinit var tvActiveCenters: TextView
    private lateinit var tvPendingCases: TextView
    private lateinit var tvPendingDonations: TextView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize views
        tvTotalCases = findViewById(R.id.tvTotalCases)
        tvActiveCenters = findViewById(R.id.tvActiveCenters)
        tvPendingCases = findViewById(R.id.tvPendingCases)
        tvPendingDonations = findViewById(R.id.tvPendingDonations)
        bottomNav = findViewById(R.id.bottomNav)

        // Quick action cards
        findViewById<CardView>(R.id.cardAllCases).setOnClickListener {
            startActivity(Intent(this, AdminCasesActivity::class.java))
        }

        findViewById<CardView>(R.id.cardAllCenters).setOnClickListener {
            startActivity(Intent(this, AdminCentersActivity::class.java))
        }

        findViewById<CardView>(R.id.cardDonationRequests).setOnClickListener {
            startActivity(Intent(this, AdminDonationsActivity::class.java))
        }

        // Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_admin_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> true
                R.id.nav_admin_cases -> {
                    startActivity(Intent(this, AdminCasesActivity::class.java))
                    true
                }
                R.id.nav_admin_centers -> {
                    startActivity(Intent(this, AdminCentersActivity::class.java))
                    true
                }
                R.id.nav_admin_donations -> {
                    startActivity(Intent(this, AdminDonationsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_admin_dashboard
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        ApiClient.api.getAdminDashboardStats().enqueue(object : Callback<AdminDashboardResponse> {
            override fun onResponse(
                call: Call<AdminDashboardResponse>,
                response: Response<AdminDashboardResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()?.stats
                    stats?.let {
                        tvTotalCases.text = it.total_cases.toString()
                        tvActiveCenters.text = it.active_centers.toString()
                        tvPendingCases.text = it.pending_cases.toString()
                        tvPendingDonations.text = it.pending_donations.toString()
                    }
                } else {
                    Log.e("AdminDashboard", "Failed to load stats: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminDashboardResponse>, t: Throwable) {
                Log.e("AdminDashboard", "Network error: ${t.message}", t)
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
