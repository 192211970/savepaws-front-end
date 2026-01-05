package com.simats.saveparse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CaseDetailActivity : AppCompatActivity() {

    private var caseId: Int = -1
    private var centerId: Int = -1
    private var caseType: String = ""
    private var remark: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_detail)

        // Get data from intent
        caseId = intent.getIntExtra("case_id", -1)
        caseType = intent.getStringExtra("case_type") ?: "Standard"
        remark = intent.getStringExtra("remark") ?: "None"
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        val reportedTime = intent.getStringExtra("reported_time") ?: ""
        val animalType = intent.getStringExtra("animal_type") ?: ""
        val condition = intent.getStringExtra("condition") ?: ""
        val photo = intent.getStringExtra("photo") ?: ""

        // Get center_id from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Initialize views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val ivAnimalPhoto = findViewById<ImageView>(R.id.ivAnimalPhoto)
        val tvCaseId = findViewById<TextView>(R.id.tvCaseId)
        val tvCaseType = findViewById<TextView>(R.id.tvCaseType)
        val tvReportedTime = findViewById<TextView>(R.id.tvReportedTime)
        val tvAnimalType = findViewById<TextView>(R.id.tvAnimalType)
        val tvCondition = findViewById<TextView>(R.id.tvCondition)
        val tvCoordinates = findViewById<TextView>(R.id.tvCoordinates)
        val btnOpenMaps = findViewById<Button>(R.id.btnOpenMaps)
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnReject = findViewById<Button>(R.id.btnReject)

        // Load animal photo
        if (photo.isNotEmpty()) {
            val imageUrl = ApiClient.IMAGE_BASE_URL + "uploads/" + photo
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.bg_rounded_image)
                .into(ivAnimalPhoto)
        }

        // Set data
        tvCaseId.text = "Case #$caseId"
        tvCaseType.text = caseType
        tvCaseType.setBackgroundResource(
            if (caseType == "Critical") R.drawable.bg_badge_critical
            else R.drawable.bg_badge_standard
        )
        tvReportedTime.text = reportedTime
        tvAnimalType.text = animalType
        tvCondition.text = condition
        tvCoordinates.text = String.format("%.6f, %.6f", latitude, longitude)

        btnBack.setOnClickListener { finish() }

        // Open in Google Maps - View location only (not navigation)
        btnOpenMaps.setOnClickListener {
            // geo: URI shows location as a pin on map
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Animal Location)")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback to browser
                val browserUri = Uri.parse("https://www.google.com/maps?q=$latitude,$longitude")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }

        // Accept button
        btnAccept.setOnClickListener {
            acceptCase()
        }

        // Reject button
        btnReject.setOnClickListener {
            handleReject()
        }
    }

    private fun acceptCase() {
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        btnAccept.isEnabled = false
        btnAccept.text = "Processing..."

        ApiClient.api.respondToCase(centerId, caseId, "accepted", null)
            .enqueue(object : Callback<CaseActionResponse> {
                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@CaseDetailActivity,
                            "Case accepted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        btnAccept.isEnabled = true
                        btnAccept.text = "Accept"
                        Toast.makeText(
                            this@CaseDetailActivity,
                            response.body()?.message ?: "Failed to accept",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    btnAccept.isEnabled = true
                    btnAccept.text = "Accept"
                    Toast.makeText(
                        this@CaseDetailActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun handleReject() {
        // Check if case can be rejected
        // Critical cases and Sent_again cases cannot be rejected
        if (caseType == "Critical" || remark == "Sent_again") {
            Toast.makeText(
                this,
                "This case cannot be rejected",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Show dialog for rejection reason
        val editText = EditText(this)
        editText.hint = "Enter reason for rejection"
        editText.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle("Reject Case")
            .setMessage("Please provide a reason for rejecting this case:")
            .setView(editText)
            .setPositiveButton("Reject") { _, _ ->
                val reason = editText.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Reason is required", Toast.LENGTH_SHORT).show()
                } else {
                    rejectCase(reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectCase(reason: String) {
        val btnReject = findViewById<Button>(R.id.btnReject)
        btnReject.isEnabled = false
        btnReject.text = "Processing..."

        ApiClient.api.respondToCase(centerId, caseId, "rejected", reason)
            .enqueue(object : Callback<CaseActionResponse> {
                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@CaseDetailActivity,
                            "Case rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        btnReject.isEnabled = true
                        btnReject.text = "Reject"
                        Toast.makeText(
                            this@CaseDetailActivity,
                            response.body()?.message ?: "Failed to reject",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    btnReject.isEnabled = true
                    btnReject.text = "Reject"
                    Toast.makeText(
                        this@CaseDetailActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
