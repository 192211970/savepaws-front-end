package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDonationHistoryActivity : AppCompatActivity() {

    private lateinit var rvDonations: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvTotalAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_donation_history)

        rvDonations = findViewById(R.id.rvDonations)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)

        rvDonations.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fetchUserDonations()
    }

    private fun fetchUserDonations() {
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        rvDonations.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getUserDonations(userId).enqueue(object : Callback<UserDonationsResponse> {
            override fun onResponse(
                call: Call<UserDonationsResponse>,
                response: Response<UserDonationsResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val donations = response.body()?.donations ?: emptyList()
                    val totalAmount = response.body()?.totalAmount ?: 0.0

                    // Update total amount display
                    tvTotalAmount.text = "₹${totalAmount.toInt()}"

                    // Save total donated to SharedPreferences for profile stats
                    sharedPref.edit().putFloat("total_donated", totalAmount.toFloat()).apply()

                    if (donations.isNotEmpty()) {
                        rvDonations.visibility = View.VISIBLE
                        rvDonations.adapter = UserDonationAdapter(donations)
                    } else {
                        emptyState.visibility = View.VISIBLE
                    }
                } else {
                    emptyState.visibility = View.VISIBLE
                    Toast.makeText(
                        this@UserDonationHistoryActivity,
                        "Failed to load donations",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserDonationsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(
                    this@UserDonationHistoryActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
