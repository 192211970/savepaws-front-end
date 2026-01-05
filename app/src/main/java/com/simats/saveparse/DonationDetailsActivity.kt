package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class DonationDetailsActivity : AppCompatActivity() {

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
    private var currentDonation: DonationDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_details)

        initViews()

        // Get donation_id from intent
        donationId = intent.getIntExtra("donation_id", -1)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Fetch donation details
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
            override fun onResponse(
                call: Call<DonationDetailsResponse>,
                response: Response<DonationDetailsResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val donation = response.body()?.donation
                    if (donation != null) {
                        currentDonation = donation
                        displayDonationDetails(donation)
                    } else {
                        Toast.makeText(
                            this@DonationDetailsActivity,
                            "Donation details not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@DonationDetailsActivity,
                        "Failed to load donation details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DonationDetailsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@DonationDetailsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayDonationDetails(donation: DonationDetails) {
        contentScroll.visibility = View.VISIBLE

        // Set case info
        tvCaseId.text = "Case #${donation.caseId}"
        val animalType = donation.animalType ?: "Animal"
        val condition = donation.animalCondition ?: ""
        tvAnimalInfo.text = "$animalType - $condition"

        // Set center info
        tvCenterName.text = donation.centerName ?: "Unknown Center"
        tvCenterPhone.text = "Phone: ${donation.centerPhone ?: "N/A"}"

        // Set amount
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvAmount.text = formatter.format(donation.amount)

        // Load animal image
        val imageUrl = when {
            !donation.casePhoto.isNullOrEmpty() -> {
                "${ApiClient.retrofit.baseUrl()}uploads/${donation.casePhoto}"
            }
            !donation.imageOfAnimal.isNullOrEmpty() -> {
                "${ApiClient.retrofit.baseUrl()}uploads/${donation.imageOfAnimal}"
            }
            else -> ""
        }

        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_alert)
                .error(R.drawable.ic_alert)
                .centerCrop()
                .into(ivAnimalPhoto)
        }

        // Donate button click - show payment method selection
        btnDonate.setOnClickListener {
            showPaymentMethodSheet()
        }
    }

    // ============ BOTTOM SHEET: Payment Method Selection ============
    private fun showPaymentMethodSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_payment_method, null)
        bottomSheet.setContentView(view)

        val cardCreditCard = view.findViewById<CardView>(R.id.cardCreditCard)
        val cardDebitCard = view.findViewById<CardView>(R.id.cardDebitCard)
        val cardUPI = view.findViewById<CardView>(R.id.cardUPI)

        cardCreditCard.setOnClickListener {
            bottomSheet.dismiss()
            showCardDetailsSheet("Credit Card")
        }

        cardDebitCard.setOnClickListener {
            bottomSheet.dismiss()
            showCardDetailsSheet("Debit Card")
        }

        cardUPI.setOnClickListener {
            bottomSheet.dismiss()
            showUPISheet()
        }

        bottomSheet.show()
    }

    // ============ BOTTOM SHEET: Card Details ============
    private fun showCardDetailsSheet(cardType: String) {
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_card_details, null)
        bottomSheet.setContentView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackCard)
        val tvTitle = view.findViewById<TextView>(R.id.tvCardTitle)
        val etCardNumber = view.findViewById<EditText>(R.id.etCardNumber)
        val etExpiry = view.findViewById<EditText>(R.id.etExpiry)
        val etCvv = view.findViewById<EditText>(R.id.etCvv)
        val etCardHolder = view.findViewById<EditText>(R.id.etCardHolder)
        val btnPay = view.findViewById<Button>(R.id.btnPayCard)

        tvTitle.text = "Enter $cardType Details"

        btnBack.setOnClickListener {
            bottomSheet.dismiss()
            showPaymentMethodSheet()
        }

        btnPay.setOnClickListener {
            val cardNumber = etCardNumber.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCvv.text.toString().trim()
            val holder = etCardHolder.text.toString().trim()

            // Validate
            if (cardNumber.length < 16) {
                Toast.makeText(this, "Enter valid 16-digit card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (expiry.length < 4) {
                Toast.makeText(this, "Enter valid expiry date (MM/YY)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cvv.length < 3) {
                Toast.makeText(this, "Enter valid CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (holder.isEmpty()) {
                Toast.makeText(this, "Enter card holder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bottomSheet.dismiss()
            processPayment(cardType)
        }

        bottomSheet.show()
    }

    // ============ BOTTOM SHEET: UPI Selection ============
    private fun showUPISheet() {
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_upi, null)
        bottomSheet.setContentView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackUpi)
        val cardGooglePay = view.findViewById<CardView>(R.id.cardGooglePay)
        val cardPhonePe = view.findViewById<CardView>(R.id.cardPhonePe)
        val cardOtherUpi = view.findViewById<CardView>(R.id.cardOtherUpi)

        btnBack.setOnClickListener {
            bottomSheet.dismiss()
            showPaymentMethodSheet()
        }

        cardGooglePay.setOnClickListener {
            bottomSheet.dismiss()
            processPayment("UPI - Google Pay")
        }

        cardPhonePe.setOnClickListener {
            bottomSheet.dismiss()
            processPayment("UPI - PhonePe")
        }

        cardOtherUpi.setOnClickListener {
            bottomSheet.dismiss()
            processPayment("UPI - Other")
        }

        bottomSheet.show()
    }

    // ============ Process Payment via API ============
    private fun processPayment(paymentMethod: String) {
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate transaction ID
        val transactionId = "TXN${System.currentTimeMillis()}"

        // Show loading
        progressBar.visibility = View.VISIBLE

        ApiClient.api.processPayment(
            donationId = donationId,
            userId = userId,
            transactionId = transactionId,
            paymentMethod = paymentMethod
        ).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    // Show success dialog
                    showPaymentSuccessDialog(transactionId, paymentMethod)
                } else {
                    val message = response.body()?.message ?: "Payment failed"
                    Toast.makeText(this@DonationDetailsActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@DonationDetailsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // ============ Payment Success Dialog ============
    private fun showPaymentSuccessDialog(transactionId: String, paymentMethod: String) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val amountText = formatter.format(currentDonation?.amount ?: 0.0)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Payment Successful!")
            .setMessage(
                "Thank you for your generous donation!\n\n" +
                "Amount: $amountText\n" +
                "Method: $paymentMethod\n" +
                "Transaction ID: $transactionId\n\n" +
                "Your contribution will help save an animal's life."
            )
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
