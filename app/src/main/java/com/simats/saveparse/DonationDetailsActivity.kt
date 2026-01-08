package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class DonationDetailsActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentScroll: ScrollView
    private lateinit var ivAnimalPhoto: ImageView
    private lateinit var tvCaseId: TextView
    private lateinit var tvAnimalInfo: TextView
    private lateinit var tvCenterName: TextView
    private lateinit var tvCenterPhone: TextView
    private lateinit var tvAmount: TextView
    private lateinit var btnDonate: Button

    private var donationId: Int = -1
    private var userId: Int = -1
    private var currentDonation: DonationDetails? = null

    companion object {
        private const val TAG = "DonationDetails"
        private const val RAZORPAY_KEY_ID = "rzp_test_DrASf34mihEAtB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_details)

        // Initialize Razorpay
        Checkout.preload(applicationContext)

        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        initViews()

        donationId = intent.getIntExtra("donation_id", -1)
        btnBack.setOnClickListener { finish() }

        if (donationId != -1) {
            fetchDonationDetails(donationId)
        } else {
            Toast.makeText(this, "Invalid donation", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        contentScroll = findViewById(R.id.contentScroll)
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto)
        tvCaseId = findViewById(R.id.tvCaseId)
        tvAnimalInfo = findViewById(R.id.tvAnimalInfo)
        tvCenterName = findViewById(R.id.tvCenterName)
        tvCenterPhone = findViewById(R.id.tvCenterPhone)
        tvAmount = findViewById(R.id.tvAmount)
        btnDonate = findViewById(R.id.btnDonate)
    }

    private fun fetchDonationDetails(donationId: Int) {
        progressBar.visibility = View.VISIBLE
        contentScroll.visibility = View.GONE

        ApiClient.api.getDonationDetails(donationId).enqueue(object : Callback<DonationDetailsResponse> {
            override fun onResponse(call: Call<DonationDetailsResponse>, response: Response<DonationDetailsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.donation?.let {
                        currentDonation = it
                        displayDonationDetails(it)
                    } ?: run {
                        Toast.makeText(this@DonationDetailsActivity, "Donation not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@DonationDetailsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DonationDetailsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@DonationDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayDonationDetails(donation: DonationDetails) {
        contentScroll.visibility = View.VISIBLE
        tvCaseId.text = "Case #${donation.caseId}"
        tvAnimalInfo.text = "${donation.animalType ?: "Animal"} - ${donation.animalCondition ?: ""}"
        tvCenterName.text = donation.centerName ?: "Unknown Center"
        tvCenterPhone.text = "Phone: ${donation.centerPhone ?: "N/A"}"

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvAmount.text = formatter.format(donation.amount)

        val imageUrl = when {
            !donation.casePhoto.isNullOrEmpty() -> "${ApiClient.retrofit.baseUrl()}uploads/${donation.casePhoto}"
            !donation.imageOfAnimal.isNullOrEmpty() -> "${ApiClient.retrofit.baseUrl()}uploads/${donation.imageOfAnimal}"
            else -> ""
        }

        if (imageUrl.isNotEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_alert).error(R.drawable.ic_alert).centerCrop().into(ivAnimalPhoto)
        }

        btnDonate.setOnClickListener {
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startRazorpayPayment()
        }
    }

    private fun startRazorpayPayment() {
        val donation = currentDonation ?: return
        
        val checkout = Checkout()
        checkout.setKeyID(RAZORPAY_KEY_ID)

        try {
            val amountInPaise = (donation.amount * 100).toInt()
            
            val options = JSONObject().apply {
                put("name", "SavePaws")
                put("description", "Donation for ${donation.centerName ?: "Animal Rescue"}")
                put("currency", "INR")
                put("amount", amountInPaise)
                
                // Theme
                put("theme", JSONObject().apply {
                    put("color", "#4CAF50")
                })
                
                // Prefill
                val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
                put("prefill", JSONObject().apply {
                    put("email", sharedPref.getString("user_email", "") ?: "")
                    put("contact", sharedPref.getString("user_phone", "") ?: "")
                })
            }

            Log.d(TAG, "Starting Razorpay payment: $options")
            checkout.open(this@DonationDetailsActivity, options)
            
        } catch (e: Exception) {
            Log.e(TAG, "Razorpay error: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ============ RAZORPAY CALLBACK - SUCCESS ============
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.d(TAG, "✅ Payment SUCCESS: $razorpayPaymentId")
        
        runOnUiThread {
            if (razorpayPaymentId != null) {
                savePaymentToDatabase(razorpayPaymentId)
            } else {
                showPaymentSuccessDialog("payment_${System.currentTimeMillis()}")
            }
        }
    }

    // ============ RAZORPAY CALLBACK - ERROR ============
    override fun onPaymentError(code: Int, response: String?) {
        Log.e(TAG, "❌ Payment ERROR: code=$code, response=$response")
        
        runOnUiThread {
            val message = when (code) {
                Checkout.NETWORK_ERROR -> "Network error"
                Checkout.INVALID_OPTIONS -> "Invalid options"
                Checkout.PAYMENT_CANCELED -> "Payment cancelled"
                else -> "Payment failed"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun savePaymentToDatabase(razorpayPaymentId: String) {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.processPayment(donationId, userId, razorpayPaymentId, "Razorpay")
            .enqueue(object : Callback<PaymentResponse> {
                override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                    progressBar.visibility = View.GONE
                    showPaymentSuccessDialog(razorpayPaymentId)
                }

                override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showPaymentSuccessDialog(razorpayPaymentId)
                }
            })
    }

    private fun showPaymentSuccessDialog(transactionId: String) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val amountText = formatter.format(currentDonation?.amount ?: 0.0)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Payment Successful!")
            .setMessage("Thank you for your donation!\n\nAmount: $amountText\nTransaction ID: $transactionId")
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
