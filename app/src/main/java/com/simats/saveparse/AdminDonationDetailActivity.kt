package com.simats.saveparse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminDonationDetailActivity : AppCompatActivity() {

    private var donationId: Int = 0
    private var status: String = "pending"

    private lateinit var ivAnimalPhoto: ImageView
    private lateinit var tvDonationId: TextView
    private lateinit var tvCenterId: TextView
    private lateinit var tvCaseId: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvCenterName: TextView
    private lateinit var cardRemark: CardView
    private lateinit var ivRemarkIcon: ImageView
    private lateinit var tvRemark: TextView
    private lateinit var cardPaymentInfo: CardView
    private lateinit var tvDonorName: TextView
    private lateinit var tvUserId: TextView
    private lateinit var tvPaymentDate: TextView
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnApprove: MaterialButton
    private lateinit var btnReject: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_donation_detail)

        donationId = intent.getIntExtra("donation_id", 0)
        status = intent.getStringExtra("status") ?: "pending"

        initViews()
        setupClickListeners()
        loadDonationDetails()
    }

    private fun initViews() {
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto)
        tvDonationId = findViewById(R.id.tvDonationId)
        tvCenterId = findViewById(R.id.tvCenterId)
        tvCaseId = findViewById(R.id.tvCaseId)
        tvAmount = findViewById(R.id.tvAmount)
        tvCenterName = findViewById(R.id.tvCenterName)
        cardRemark = findViewById(R.id.cardRemark)
        ivRemarkIcon = findViewById(R.id.ivRemarkIcon)
        tvRemark = findViewById(R.id.tvRemark)
        cardPaymentInfo = findViewById(R.id.cardPaymentInfo)
        tvDonorName = findViewById(R.id.tvDonorName)
        tvUserId = findViewById(R.id.tvUserId)
        tvPaymentDate = findViewById(R.id.tvPaymentDate)
        layoutActions = findViewById(R.id.layoutActions)
        btnApprove = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)
        progressBar = findViewById(R.id.progressBar)

        findViewById<TextView>(R.id.tvHeaderTitle).text = "Donation #$donationId"
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnApprove.setOnClickListener {
            showConfirmDialog("approve")
        }

        btnReject.setOnClickListener {
            showConfirmDialog("reject")
        }
    }

    private fun loadDonationDetails() {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.getAdminDonationDetails(donationId).enqueue(object : Callback<AdminDonationDetailResponse> {
            override fun onResponse(
                call: Call<AdminDonationDetailResponse>,
                response: Response<AdminDonationDetailResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val donation = response.body()?.donation
                    if (donation != null) {
                        displayDonationDetails(donation)
                    }
                } else {
                    Toast.makeText(this@AdminDonationDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                    Log.e("AdminDonationDetail", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminDonationDetailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminDonationDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminDonationDetail", "Network error: ${t.message}")
            }
        })
    }

    private fun displayDonationDetails(donation: AdminDonationDetail) {
        // Basic info
        tvDonationId.text = "#${donation.donation_id}"
        tvCenterId.text = "#${donation.center_id}"
        tvCaseId.text = "#${donation.case_id}"
        tvCenterName.text = donation.center_name ?: "N/A"

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvAmount.text = formatter.format(donation.amount ?: 0)

        // Load image
        val imageUrl = when {
            !donation.image_of_animal.isNullOrEmpty() -> ApiClient.IMAGE_BASE_URL + "uploads/donations/" + donation.image_of_animal
            !donation.case_photo.isNullOrEmpty() -> ApiClient.IMAGE_BASE_URL + "uploads/" + donation.case_photo
            else -> null
        }
        
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_paw)
                .into(ivAnimalPhoto)
        }

        // Show appropriate UI based on status
        when (status) {
            "pending" -> {
                layoutActions.visibility = View.VISIBLE
                cardRemark.visibility = View.GONE
                cardPaymentInfo.visibility = View.GONE
            }
            "approved" -> {
                layoutActions.visibility = View.GONE
                cardRemark.visibility = View.VISIBLE
                cardPaymentInfo.visibility = View.GONE
                tvRemark.text = "Waiting for donation payment"
                ivRemarkIcon.setImageResource(R.drawable.ic_alert)
                ivRemarkIcon.setColorFilter(getColor(android.R.color.holo_orange_light))
            }
            "rejected" -> {
                layoutActions.visibility = View.GONE
                cardRemark.visibility = View.VISIBLE
                cardPaymentInfo.visibility = View.GONE
                tvRemark.text = "This donation request was rejected"
                ivRemarkIcon.setImageResource(R.drawable.ic_alert)
                ivRemarkIcon.setColorFilter(getColor(android.R.color.holo_red_light))
            }
            "paid" -> {
                layoutActions.visibility = View.GONE
                cardRemark.visibility = View.GONE
                cardPaymentInfo.visibility = View.VISIBLE
                
                tvDonorName.text = donation.donor_name ?: "Anonymous"
                tvUserId.text = "#${donation.user_id ?: "N/A"}"
                tvPaymentDate.text = formatDate(donation.payment_time)
            }
        }
    }

    private fun showConfirmDialog(action: String) {
        val title = if (action == "approve") "Approve Donation" else "Reject Donation"
        val message = if (action == "approve") 
            "Are you sure you want to approve this donation request?" 
        else 
            "Are you sure you want to reject this donation request?"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                performAction(action)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performAction(action: String) {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.approveDonation(donationId, action).enqueue(object : Callback<AdminActionResponse> {
            override fun onResponse(
                call: Call<AdminActionResponse>,
                response: Response<AdminActionResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(
                        this@AdminDonationDetailActivity,
                        if (action == "approve") "Donation approved!" else "Donation rejected!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    val msg = response.body()?.message ?: "Action failed"
                    Toast.makeText(this@AdminDonationDetailActivity, msg, Toast.LENGTH_SHORT).show()
                    Log.e("AdminDonationDetail", "Action failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminActionResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminDonationDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminDonationDetail", "Network error: ${t.message}")
            }
        })
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr.take(10)
        }
    }
}
