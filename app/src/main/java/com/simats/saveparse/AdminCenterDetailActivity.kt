package com.simats.saveparse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class AdminCenterDetailActivity : AppCompatActivity() {

    private var centerId: Int = 0
    private var centerName: String = ""
    private var currentStatus: String = "Operating"

    private lateinit var tvCenterName: TextView
    private lateinit var tvCenterId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvCasesHandled: TextView
    private lateinit var tvDonationsCount: TextView
    private lateinit var tvAvgResponseTime: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var btnDeactivate: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_center_detail)

        centerId = intent.getIntExtra("center_id", 0)
        centerName = intent.getStringExtra("center_name") ?: "Center Details"

        initViews()
        setupClickListeners()
        loadCenterDetails()
    }

    private fun initViews() {
        tvCenterName = findViewById(R.id.tvCenterName)
        tvCenterId = findViewById(R.id.tvCenterId)
        tvStatus = findViewById(R.id.tvStatus)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvCasesHandled = findViewById(R.id.tvCasesHandled)
        tvDonationsCount = findViewById(R.id.tvDonationsCount)
        tvAvgResponseTime = findViewById(R.id.tvAvgResponseTime)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvAddress = findViewById(R.id.tvAddress)
        btnDeactivate = findViewById(R.id.btnDeactivate)
        progressBar = findViewById(R.id.progressBar)

        // Set header title
        findViewById<TextView>(R.id.tvHeaderTitle).text = centerName
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnDeactivate.setOnClickListener {
            showDeactivateConfirmDialog()
        }
    }

    private fun loadCenterDetails() {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.getAdminCenterDetails(centerId).enqueue(object : Callback<AdminCenterDetailResponse> {
            override fun onResponse(
                call: Call<AdminCenterDetailResponse>,
                response: Response<AdminCenterDetailResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!
                    displayCenterDetails(data)
                } else {
                    Toast.makeText(this@AdminCenterDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                    Log.e("AdminCenterDetail", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminCenterDetailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminCenterDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminCenterDetail", "Network error: ${t.message}")
            }
        })
    }

    private fun displayCenterDetails(data: AdminCenterDetailResponse) {
        val center = data.center ?: return
        val stats = data.stats

        tvCenterName.text = center.center_name ?: "Unknown"
        tvCenterId.text = "Center ID: #${center.center_id}"

        // Status
        currentStatus = center.center_status ?: "Operating"
        updateStatusUI(currentStatus)

        // Member since
        tvMemberSince.text = "Member since: ${formatDate(center.member_since)}"

        // Stats
        tvCasesHandled.text = (stats?.cases_handled ?: 0).toString()
        tvDonationsCount.text = (stats?.donation_count ?: 0).toString()
        
        // Avg response time
        val avgTime = center.avg_response_time ?: 0
        tvAvgResponseTime.text = if (avgTime >= 60) {
            "${avgTime / 60} hrs ${avgTime % 60} mins"
        } else {
            "$avgTime mins"
        }

        // Contact info
        tvPhone.text = center.phone ?: "N/A"
        tvEmail.text = center.email ?: "N/A"
        tvAddress.text = center.address ?: "N/A"
    }

    private fun updateStatusUI(status: String) {
        if (status == "Operating") {
            tvStatus.text = "Operating"
            tvStatus.setBackgroundResource(R.drawable.bg_status_green)
            tvStatus.setTextColor(getColor(R.color.green_dark))
            btnDeactivate.text = "Deactivate Center"
            btnDeactivate.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        } else {
            tvStatus.text = "Deactivated"
            tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
            tvStatus.setTextColor(getColor(R.color.orange_dark))
            btnDeactivate.text = "Activate Center"
            btnDeactivate.setBackgroundColor(getColor(R.color.green_dark))
        }
    }

    private fun showDeactivateConfirmDialog() {
        val action = if (currentStatus == "Operating") "deactivate" else "activate"
        val newStatus = if (currentStatus == "Operating") "Deactivated" else "Operating"

        AlertDialog.Builder(this)
            .setTitle("${action.replaceFirstChar { it.uppercase() }} Center")
            .setMessage("Are you sure you want to $action this rescue center?")
            .setPositiveButton("Yes") { _, _ ->
                updateCenterStatus(newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCenterStatus(newStatus: String) {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.updateCenterStatus(centerId, newStatus).enqueue(object : Callback<AdminCenterStatusResponse> {
            override fun onResponse(
                call: Call<AdminCenterStatusResponse>,
                response: Response<AdminCenterStatusResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    currentStatus = newStatus
                    updateStatusUI(newStatus)
                    Toast.makeText(
                        this@AdminCenterDetailActivity,
                        "Center ${if (newStatus == "Operating") "activated" else "deactivated"} successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val msg = response.body()?.message ?: "Failed to update status"
                    Toast.makeText(this@AdminCenterDetailActivity, msg, Toast.LENGTH_SHORT).show()
                    Log.e("AdminCenterDetail", "Update failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminCenterStatusResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminCenterDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminCenterDetail", "Network error: ${t.message}")
            }
        })
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr.take(10)
        }
    }
}
