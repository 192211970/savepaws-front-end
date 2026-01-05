package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class CenterDonationsReceivedActivity : AppCompatActivity() {

    private lateinit var rvDonations: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private var centerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_donations_received)

        rvDonations = findViewById(R.id.rvDonations)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        rvDonations.layoutManager = LinearLayoutManager(this)

        // Get center ID from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadDonationsReceived()
    }

    private fun loadDonationsReceived() {
        if (centerId == -1) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Center not found"
            return
        }

        progressBar.visibility = View.VISIBLE

        ApiClient.api.getCenterProfile(centerId).enqueue(object : Callback<CenterProfileResponse> {
            override fun onResponse(
                call: Call<CenterProfileResponse>,
                response: Response<CenterProfileResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val donations = response.body()?.received_donations ?: emptyList()

                    if (donations.isEmpty()) {
                        rvDonations.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rvDonations.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        rvDonations.adapter = DonationsAdapter(donations)
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Failed to load donations"
                }
            }

            override fun onFailure(call: Call<CenterProfileResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Network error"
                Log.e("CenterDonationsReceived", "Failed to load donations", t)
            }
        })
    }

    inner class DonationsAdapter(
        private val donations: List<CenterReceivedDonation>
    ) : RecyclerView.Adapter<DonationsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDonorName: TextView = view.findViewById(R.id.tvDonorName)
            val tvPaymentTime: TextView = view.findViewById(R.id.tvPaymentTime)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_donation, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val donation = donations[position]
            holder.tvDonorName.text = donation.donor_name ?: "Anonymous Donor"
            holder.tvPaymentTime.text = formatDate(donation.payment_time)
            holder.tvAmount.text = "₹${donation.amount?.toInt() ?: 0}"
        }

        override fun getItemCount() = donations.size

        private fun formatDate(dateTime: String?): String {
            if (dateTime == null) return ""
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateTime.take(10)
            }
        }
    }
}
