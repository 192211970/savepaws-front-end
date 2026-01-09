package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CenterDashboardActivity : AppCompatActivity() {

    private var centerId: Int = -1
    private var isActive: Boolean = true

    private lateinit var tvCenterName: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvAcceptedCount: TextView
    private lateinit var tvTotalHandled: TextView
    private lateinit var tvStatusValue: TextView
    private lateinit var switchStatus: SwitchCompat
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_dashboard)

        // Initialize views
        tvCenterName = findViewById(R.id.tvCenterName)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount)
        tvTotalHandled = findViewById(R.id.tvTotalHandled)
        tvStatusValue = findViewById(R.id.tvStatusValue)
        switchStatus = findViewById(R.id.switchStatus)

        val cardPendingCases = findViewById<CardView>(R.id.cardPendingCases)
        val cardAcceptedCases = findViewById<CardView>(R.id.cardAcceptedCases)
        val cardDonationRaise = findViewById<CardView>(R.id.cardDonationRaise)

        // Load center data from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)
        val centerName = sharedPref.getString("center_name", "Rescue Center") ?: "Rescue Center"

        tvCenterName.text = centerName

        // Status toggle
        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            updateCenterStatus(isChecked)
        }

        // Card click listeners
        cardPendingCases.setOnClickListener {
            startActivity(Intent(this, PendingCasesActivity::class.java))
        }

        cardAcceptedCases.setOnClickListener {
            startActivity(Intent(this, AcceptedCasesActivity::class.java))
        }

        cardDonationRaise.setOnClickListener {
            startActivity(Intent(this, DonationMenuActivity::class.java))
        }

        // Closed Cases navigation card
        findViewById<CardView>(R.id.cardClosedCases).setOnClickListener {
            startActivity(Intent(this, CenterClosedCasesActivity::class.java))
        }

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_cases -> {
                    startActivity(Intent(this, AcceptedCasesActivity::class.java))
                    true
                }
                R.id.nav_center_profile -> {
                    startActivity(Intent(this, CenterProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset bottom navigation highlight to dashboard
        bottomNav.selectedItemId = R.id.nav_dashboard
        loadDashboardData()
    }

    private fun loadDashboardData() {
        if (centerId == -1) {
            Toast.makeText(this, "Center not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Load pending cases count
        ApiClient.api.getCenterPendingCases(centerId)
            .enqueue(object : Callback<PendingCasesResponse> {
                override fun onResponse(
                    call: Call<PendingCasesResponse>,
                    response: Response<PendingCasesResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        tvPendingCount.text = data.total_pending.toString()
                    }
                }

                override fun onFailure(call: Call<PendingCasesResponse>, t: Throwable) {
                    // Silent fail for dashboard
                }
            })

        // Load accepted cases count (in progress only)
        ApiClient.api.getCenterAcceptedCases(centerId)
            .enqueue(object : Callback<AcceptedCasesResponse> {
                override fun onResponse(
                    call: Call<AcceptedCasesResponse>,
                    response: Response<AcceptedCasesResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        tvAcceptedCount.text = data.total_accepted.toString()
                    }
                }

                override fun onFailure(call: Call<AcceptedCasesResponse>, t: Throwable) {
                    // Silent fail for dashboard
                }
            })

        // Load closed/handled cases count from profile
        ApiClient.api.getCenterProfile(centerId)
            .enqueue(object : Callback<CenterProfileResponse> {
                override fun onResponse(
                    call: Call<CenterProfileResponse>,
                    response: Response<CenterProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val handledCount = response.body()?.handled_cases?.size ?: 0
                        tvTotalHandled.text = handledCount.toString()
                    }
                }

                override fun onFailure(call: Call<CenterProfileResponse>, t: Throwable) {
                    // Silent fail for dashboard
                }
            })
    }

    private fun updateCenterStatus(isOpen: Boolean) {
        val statusValue = if (isOpen) "Yes" else "No"
        tvStatusValue.text = if (isOpen) "Open" else "Closed"

        ApiClient.api.updateCenterActiveStatus(centerId, statusValue)
            .enqueue(object : Callback<CaseActionResponse> {
                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        if (result.status == "success") {
                            Toast.makeText(
                                this@CenterDashboardActivity,
                                "Status updated to ${if (isOpen) "Open" else "Closed"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    Toast.makeText(
                        this@CenterDashboardActivity,
                        "Failed to update status",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Revert switch
                    switchStatus.isChecked = !isOpen
                }
            })
    }
}
