package com.simats.saveparse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CenterDetailsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var userId: Int = -1

    private lateinit var etCenterName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var tvLocationStatus: TextView
    private lateinit var tvCoordinates: TextView
    private lateinit var btnSubmit: Button
    private lateinit var btnRefreshLocation: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_details)

        // Initialize views
        etCenterName = findViewById(R.id.etCenterName)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Load user data from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)
        val userEmail = sharedPref.getString("user_email", "") ?: ""
        val userPhone = sharedPref.getString("user_phone", "") ?: ""

        // Pre-fill email (read-only) and phone
        etEmail.setText(userEmail)
        etPhone.setText(userPhone)

        // Get current location
        getCurrentLocation()

        // Back button
        btnBack.setOnClickListener {
            // Don't allow going back without completing registration
            Toast.makeText(this, "Please complete center registration", Toast.LENGTH_SHORT).show()
        }

        // Refresh location button
        btnRefreshLocation.setOnClickListener {
            getCurrentLocation()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitCenterDetails()
        }
    }

    private fun getCurrentLocation() {
        tvLocationStatus.text = "Detecting location..."
        tvCoordinates.text = "Lat: --, Lng: --"

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                tvLocationStatus.text = "Location detected ✓"
                tvCoordinates.text = "Lat: %.6f, Lng: %.6f".format(currentLatitude, currentLongitude)
            } else {
                tvLocationStatus.text = "Location unavailable"
                tvCoordinates.text = "Please enable GPS and try again"
            }
        }.addOnFailureListener {
            tvLocationStatus.text = "Location error"
            tvCoordinates.text = "Could not get location"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitCenterDetails() {
        val centerName = etCenterName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // Validation
        if (centerName.isEmpty()) {
            etCenterName.error = "Center name is required"
            etCenterName.requestFocus()
            return
        }

        if (address.isEmpty()) {
            etAddress.error = "Address is required"
            etAddress.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            etPhone.requestFocus()
            return
        }

        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            Toast.makeText(this, "Please wait for location detection", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
            return
        }

        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Disable button to prevent double submission
        btnSubmit.isEnabled = false
        btnSubmit.text = "Registering..."

        ApiClient.api.registerCenter(
            userId = userId,
            centerName = centerName,
            address = address,
            latitude = currentLatitude,
            longitude = currentLongitude,
            phone = phone,
            email = email
        ).enqueue(object : Callback<CenterDetailsResponse> {

            override fun onResponse(
                call: Call<CenterDetailsResponse>,
                response: Response<CenterDetailsResponse>
            ) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Complete Registration"

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status == "success") {
                        // Save center_id to SharedPreferences
                        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("center_id", result.center_id ?: -1)
                            putString("center_name", centerName)
                            apply()
                        }

                        Toast.makeText(
                            this@CenterDetailsActivity,
                            "Center registered successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to center dashboard
                        startActivity(Intent(this@CenterDetailsActivity, CenterDashboardActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(
                            this@CenterDetailsActivity,
                            result.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CenterDetailsActivity,
                        "Server error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<CenterDetailsResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Complete Registration"
                Toast.makeText(
                    this@CenterDetailsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Prevent back press without completing registration
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Toast.makeText(this, "Please complete center registration first", Toast.LENGTH_SHORT).show()
    }
}
