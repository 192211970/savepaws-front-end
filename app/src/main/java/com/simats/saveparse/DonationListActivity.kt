package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonationListActivity : AppCompatActivity() {

    private lateinit var rvDonations: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_list)

        // Initialize views
        rvDonations = findViewById(R.id.rvDonations)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        btnBack = findViewById(R.id.btnBack)
        bottomNav = findViewById(R.id.bottomNav)

        // Setup RecyclerView
        rvDonations.layoutManager = LinearLayoutManager(this)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Setup Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, OngoingCasesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Fetch donations on first load
        fetchApprovedDonations()
    }

    // Refresh list when returning from DonationDetailsActivity (after payment)
    override fun onResume() {
        super.onResume()
        // Always refresh when coming back to this screen
        fetchApprovedDonations()
    }

    private fun fetchApprovedDonations() {
        progressBar.visibility = View.VISIBLE
        rvDonations.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getApprovedDonations().enqueue(object : Callback<DonationListResponse> {
            override fun onResponse(
                call: Call<DonationListResponse>,
                response: Response<DonationListResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val donations = response.body()?.donations ?: emptyList()

                    if (donations.isNotEmpty()) {
                        rvDonations.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE

                        val adapter = DonationAdapter(donations) { donation ->
                            // Navigate to DonationDetailsActivity
                            val intent = Intent(this@DonationListActivity, DonationDetailsActivity::class.java)
                            intent.putExtra("donation_id", donation.donationId)
                            startActivity(intent)
                        }
                        rvDonations.adapter = adapter
                    } else {
                        rvDonations.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@DonationListActivity,
                        "Failed to load donations",
                        Toast.LENGTH_SHORT
                    ).show()
                    emptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<DonationListResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(
                    this@DonationListActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
