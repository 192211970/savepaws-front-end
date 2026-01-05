package com.simats.saveparse

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonationStatusActivity : AppCompatActivity() {

    private var centerId: Int = -1
    private lateinit var recyclerDonations: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_status)

        // Get center_id
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Initialize views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        recyclerDonations = findViewById(R.id.recyclerDonations)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        tvCount = findViewById(R.id.tvCount)

        recyclerDonations.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        loadDonations()
    }

    private fun loadDonations() {
        progressBar.visibility = View.VISIBLE
        recyclerDonations.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getDonationHistory(centerId)
            .enqueue(object : Callback<DonationHistoryResponse> {
                override fun onResponse(
                    call: Call<DonationHistoryResponse>,
                    response: Response<DonationHistoryResponse>
                ) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val donations = response.body()?.donations ?: emptyList()
                        tvCount.text = "${donations.size} requests"

                        if (donations.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                        } else {
                            recyclerDonations.visibility = View.VISIBLE
                            recyclerDonations.adapter = DonationStatusAdapter(donations)
                        }
                    } else {
                        Toast.makeText(
                            this@DonationStatusActivity,
                            "Failed to load donations",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DonationHistoryResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@DonationStatusActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
