package com.simats.saveparse

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class AdminClosedCaseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_closed_case_detail)

        // Get data from intent
        val caseId = intent.getIntExtra("case_id", 0)
        val centerId = intent.getIntExtra("center_id", 0)
        val centerName = intent.getStringExtra("center_name") ?: "Unknown Center"
        val closedTime = intent.getStringExtra("case_took_up_time")
        val rescuedPhoto = intent.getStringExtra("rescued_photo")
        val originalPhoto = intent.getStringExtra("original_photo")
        val typeOfAnimal = intent.getStringExtra("type_of_animal") ?: "Animal"

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set data
        findViewById<TextView>(R.id.tvCaseId).text = "Case #$caseId"
        findViewById<TextView>(R.id.tvAnimalType).text = typeOfAnimal
        findViewById<TextView>(R.id.tvCenterName).text = centerName
        findViewById<TextView>(R.id.tvCenterId).text = "#$centerId"
        findViewById<TextView>(R.id.tvClosedTime).text = formatDateTime(closedTime)

        // Load rescued photo
        val ivRescuedPhoto = findViewById<ImageView>(R.id.ivRescuedPhoto)
        // Note: rescued_photo may already contain "uploads/" prefix from database
        val photoUrl = when {
            !rescuedPhoto.isNullOrEmpty() -> {
                if (rescuedPhoto.startsWith("uploads/")) {
                    ApiClient.IMAGE_BASE_URL + rescuedPhoto
                } else {
                    ApiClient.IMAGE_BASE_URL + "uploads/" + rescuedPhoto
                }
            }
            !originalPhoto.isNullOrEmpty() -> ApiClient.IMAGE_BASE_URL + "uploads/" + originalPhoto
            else -> null
        }

        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_paw)
                .into(ivRescuedPhoto)
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
