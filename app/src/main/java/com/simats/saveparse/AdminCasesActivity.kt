package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminCasesActivity : AppCompatActivity() {

    private lateinit var tvPendingCount: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var tvClosedCount: TextView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_cases)

        // Initialize views
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)
        tvClosedCount = findViewById(R.id.tvClosedCount)
        bottomNav = findViewById(R.id.bottomNav)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Navigation cards
        findViewById<CardView>(R.id.cardPendingCases).setOnClickListener {
            startActivity(Intent(this, AdminPendingCasesActivity::class.java))
        }

        findViewById<CardView>(R.id.cardInProgressCases).setOnClickListener {
            startActivity(Intent(this, AdminInProgressCasesActivity::class.java))
        }

        findViewById<CardView>(R.id.cardClosedCases).setOnClickListener {
            startActivity(Intent(this, AdminClosedCasesActivity::class.java))
        }

        // Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_admin_cases
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_admin_cases -> true
                R.id.nav_admin_centers -> {
                    startActivity(Intent(this, AdminCentersActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_admin_donations -> {
                    startActivity(Intent(this, AdminDonationsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_admin_cases
        loadCaseCounts()
    }

    private fun loadCaseCounts() {
        // Load dashboard stats to get counts
        ApiClient.api.getAdminDashboardStats().enqueue(object : Callback<AdminDashboardResponse> {
            override fun onResponse(
                call: Call<AdminDashboardResponse>,
                response: Response<AdminDashboardResponse>
            ) {
                Log.d("AdminCases", "Response: ${response.body()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()?.stats
                    Log.d("AdminCases", "Stats: pending=${stats?.pending_cases}, in_progress=${stats?.in_progress_cases}, closed=${stats?.closed_cases}")
                    stats?.let {
                        tvPendingCount.text = it.pending_cases.toString()
                        tvInProgressCount.text = it.in_progress_cases.toString()
                        tvClosedCount.text = it.closed_cases.toString()
                    }
                } else {
                    Log.e("AdminCases", "Failed to load stats: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AdminDashboardResponse>, t: Throwable) {
                Log.e("AdminCasesActivity", "Failed to load stats: ${t.message}")
            }
        })
    }
}
