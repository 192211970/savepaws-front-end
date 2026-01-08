package com.simats.saveparse

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CaseTrackActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var ivAnimalPhoto: ImageView
    private lateinit var tvAnimalType: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvCaseId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var timelineContainer: LinearLayout
    private lateinit var cardRescueCenter: CardView
    private lateinit var tvRescueCenterName: TextView
    private lateinit var btnCallCenter: TextView
    private lateinit var cardRescuePhoto: CardView
    private lateinit var ivRescuePhoto: ImageView

    private var rescueCenterPhone: String? = null
    private var currentCaseId: Int = -1
    
    // Auto-refresh handler for real-time updates
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 10000L // 10 seconds
    private var isRefreshing = false
    
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (currentCaseId != -1) {
                fetchCaseTrack(currentCaseId, showLoading = false)
            }
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_track)

        initViews()

        // Get case data from intent
        currentCaseId = intent.getIntExtra("case_id", -1)
        val photo = intent.getStringExtra("photo") ?: ""
        val animalType = intent.getStringExtra("animal_type") ?: ""
        val condition = intent.getStringExtra("condition") ?: ""
        val status = intent.getStringExtra("status") ?: ""

        // Set header info
        setupHeader(photo, animalType, condition, currentCaseId, status)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Call center button
        btnCallCenter.setOnClickListener {
            rescueCenterPhone?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                startActivity(intent)
            }
        }

        // Fetch case tracking details
        if (currentCaseId != -1) {
            fetchCaseTrack(currentCaseId, showLoading = true)
        } else {
            Toast.makeText(this, "Invalid case", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Start auto-refresh when activity is visible
        startAutoRefresh()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop auto-refresh when activity is not visible
        stopAutoRefresh()
    }
    
    private fun startAutoRefresh() {
        if (!isRefreshing) {
            isRefreshing = true
            refreshHandler.postDelayed(refreshRunnable, refreshInterval)
        }
    }
    
    private fun stopAutoRefresh() {
        isRefreshing = false
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto)
        tvAnimalType = findViewById(R.id.tvAnimalType)
        tvCondition = findViewById(R.id.tvCondition)
        tvCaseId = findViewById(R.id.tvCaseId)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        timelineContainer = findViewById(R.id.timelineContainer)
        cardRescueCenter = findViewById(R.id.cardRescueCenter)
        tvRescueCenterName = findViewById(R.id.tvRescueCenterName)
        btnCallCenter = findViewById(R.id.btnCallCenter)
        cardRescuePhoto = findViewById(R.id.cardRescuePhoto)
        ivRescuePhoto = findViewById(R.id.ivRescuePhoto)
    }

    private fun setupHeader(photo: String, animalType: String, condition: String, caseId: Int, status: String) {
        tvAnimalType.text = animalType
        tvCondition.text = condition
        tvCaseId.text = "Case #$caseId"
        tvStatus.text = status

        // Status color
        val statusColor = when (status) {
            "Reported" -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            "Accepted" -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
            "Closed" -> ContextCompat.getColor(this, android.R.color.darker_gray)
            else -> ContextCompat.getColor(this, android.R.color.holo_blue_dark)
        }
        tvStatus.background.setTint(statusColor)

        // Load photo
        if (photo.isNotEmpty()) {
            val imageUrl = ApiClient.IMAGE_BASE_URL + "uploads/" + photo
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_alert)
                .error(R.drawable.ic_alert)
                .centerCrop()
                .into(ivAnimalPhoto)
        }
    }

    private fun fetchCaseTrack(caseId: Int, showLoading: Boolean = true) {
        if (showLoading) {
            progressBar.visibility = View.VISIBLE
            timelineContainer.visibility = View.GONE
        }

        ApiClient.api.getCaseTrack(caseId).enqueue(object : Callback<CaseTrackResponse> {
            override fun onResponse(
                call: Call<CaseTrackResponse>,
                response: Response<CaseTrackResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val trackData = response.body()!!
                    
                    // Update header with case info from API if available
                    trackData.caseInfo?.let { info ->
                        tvAnimalType.text = info.typeOfAnimal
                        tvCondition.text = info.animalCondition
                        tvCaseId.text = "Case #${info.caseId}"
                        tvStatus.text = info.caseStatus
                        
                        // Status color
                        val statusColor = when (info.caseStatus) {
                            "Reported" -> ContextCompat.getColor(this@CaseTrackActivity, android.R.color.holo_orange_dark)
                            "Accepted" -> ContextCompat.getColor(this@CaseTrackActivity, android.R.color.holo_green_dark)
                            "Closed" -> ContextCompat.getColor(this@CaseTrackActivity, android.R.color.darker_gray)
                            else -> ContextCompat.getColor(this@CaseTrackActivity, android.R.color.holo_blue_dark)
                        }
                        tvStatus.background.setTint(statusColor)
                        
                        // Load photo from API response
                        if (!info.photo.isNullOrEmpty()) {
                            val imageUrl = ApiClient.IMAGE_BASE_URL + "uploads/" + info.photo
                            Glide.with(this@CaseTrackActivity)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_alert)
                                .error(R.drawable.ic_alert)
                                .centerCrop()
                                .into(ivAnimalPhoto)
                        }
                    }
                    
                    // Build timeline
                    timelineContainer.visibility = View.VISIBLE
                    buildTimeline(trackData.timeline)

                    // Show rescue center info if available
                    trackData.rescueInfo?.let { info ->
                        if (!info.rescueCenter.isNullOrEmpty()) {
                            cardRescueCenter.visibility = View.VISIBLE
                            tvRescueCenterName.text = info.rescueCenter
                            rescueCenterPhone = info.rescueCenterPhone

                            // Show rescue photo if available
                            if (!info.rescuePhoto.isNullOrEmpty()) {
                                cardRescuePhoto.visibility = View.VISIBLE
                                val photoUrl = ApiClient.IMAGE_BASE_URL + info.rescuePhoto
                                Glide.with(this@CaseTrackActivity)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_alert)
                                    .into(ivRescuePhoto)
                            }
                        }
                    }
                } else if (showLoading) {
                    // Only show error toast on initial load, not on background refresh
                    val errorMsg = response.body()?.message ?: "Unknown error (code: ${response.code()})"
                    android.util.Log.e("CaseTrack", "Failed to load: $errorMsg, body: ${response.errorBody()?.string()}")
                    Toast.makeText(
                        this@CaseTrackActivity,
                        "Failed to load: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<CaseTrackResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                android.util.Log.e("CaseTrack", "Network error", t)
                if (showLoading) {
                    // Only show error toast on initial load, not on background refresh
                    Toast.makeText(
                        this@CaseTrackActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun buildTimeline(timeline: List<TimelineItem>) {
        timelineContainer.removeAllViews()

        for ((index, item) in timeline.withIndex()) {
            val timelineView = LayoutInflater.from(this)
                .inflate(R.layout.item_timeline, timelineContainer, false)

            val lineTop = timelineView.findViewById<View>(R.id.lineTop)
            val lineBottom = timelineView.findViewById<View>(R.id.lineBottom)
            val circleIndicator = timelineView.findViewById<View>(R.id.circleIndicator)
            val tvTitle = timelineView.findViewById<TextView>(R.id.tvTitle)
            val tvDescription = timelineView.findViewById<TextView>(R.id.tvDescription)
            val tvTimestamp = timelineView.findViewById<TextView>(R.id.tvTimestamp)

            // Set content
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvTimestamp.text = formatTimestamp(item.timestamp)
            tvTimestamp.visibility = if (item.timestamp.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Check if current step is completed (based only on status from server)
            val isCompleted = item.status == "completed"
            
            // Check if next step is completed
            val isNextCompleted = if (index < timeline.size - 1) {
                timeline[index + 1].status == "completed"
            } else {
                false
            }

            // Set indicator (circle) style based on status
            if (isCompleted) {
                circleIndicator.setBackgroundResource(R.drawable.timeline_circle_completed)
            } else {
                circleIndicator.setBackgroundResource(R.drawable.timeline_circle_pending)
            }

            // Line colors:
            // - lineTop is green if THIS step is completed
            // - lineBottom is green if the NEXT step is also completed
            val greenColor = Color.parseColor("#4CAF50")
            val greyColor = Color.parseColor("#E0E0E0")

            lineTop.setBackgroundColor(if (isCompleted) greenColor else greyColor)
            lineBottom.setBackgroundColor(if (isNextCompleted) greenColor else greyColor)

            // Hide top line for first item
            if (index == 0) {
                lineTop.visibility = View.INVISIBLE
            }

            // Hide bottom line for last item
            if (index == timeline.size - 1) {
                lineBottom.visibility = View.INVISIBLE
            }

            timelineContainer.addView(timelineView)
        }
    }

    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return ""
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            timestamp
        }
    }
}
