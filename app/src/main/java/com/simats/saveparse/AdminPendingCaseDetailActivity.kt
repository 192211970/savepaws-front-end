package com.simats.saveparse

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class AdminPendingCaseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pending_case_detail)

        // Get data from intent
        val caseId = intent.getIntExtra("case_id", 0)
        val typeOfAnimal = intent.getStringExtra("type_of_animal") ?: "Animal"
        val photo = intent.getStringExtra("photo")
        val createdTime = intent.getStringExtra("created_time")
        val caseType = intent.getStringExtra("case_type") ?: "Standard"
        val remark = intent.getStringExtra("remark") ?: "None"
        val latitude = intent.getStringExtra("latitude") ?: ""
        val longitude = intent.getStringExtra("longitude") ?: ""
        val centersEscalated = intent.getStringExtra("centers_escalated") ?: ""

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set data
        findViewById<TextView>(R.id.tvCaseId).text = "Case #$caseId"
        findViewById<TextView>(R.id.tvAnimalType).text = typeOfAnimal
        findViewById<TextView>(R.id.tvRemark).text = remark.replace("_", " ")
        findViewById<TextView>(R.id.tvLocation).text = if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
            "$latitude, $longitude"
        } else {
            "N/A"
        }

        // Case type badge
        val tvCaseType = findViewById<TextView>(R.id.tvCaseType)
        tvCaseType.text = caseType
        if (caseType == "Critical") {
            tvCaseType.setBackgroundResource(R.drawable.bg_status_orange)
            tvCaseType.setTextColor(getColor(android.R.color.holo_orange_dark))
        } else {
            tvCaseType.setBackgroundResource(R.drawable.bg_status_green)
            tvCaseType.setTextColor(getColor(android.R.color.holo_green_dark))
        }

        // Format time
        findViewById<TextView>(R.id.tvReportedTime).text = formatDateTime(createdTime)

        // Centers escalated
        val centers = if (centersEscalated.isNotEmpty()) {
            "Center ${centersEscalated.replace(",", ", Center ")}"
        } else {
            "None"
        }
        findViewById<TextView>(R.id.tvCentersEscalated).text = centers

        // Load image
        val ivPhoto = findViewById<ImageView>(R.id.ivAnimalPhoto)
        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load("http://10.0.2.2/uploads/$photo")
                .placeholder(R.drawable.ic_paw)
                .into(ivPhoto)
        }
    }

    private fun formatDateTime(dateTime: String?): String {
        if (dateTime == null) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateTime
        }
    }
}
