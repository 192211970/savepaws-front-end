package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnBack.setOnClickListener { finish() }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {

        ApiClient.api.login(email, password)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val loginResponse = response.body()!!

                        if (loginResponse.status == "success") {

                            val user = loginResponse.user!!
                            val userType = user.user_type

                            // Save user data to SharedPreferences
                            val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("user_id", user.id)
                                putString("user_name", user.name)
                                putString("user_email", user.email)
                                putString("user_phone", user.phone)
                                putString("user_type", userType)
                                apply()
                            }

                            navigateBasedOnRole(userType)

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Server error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun navigateBasedOnRole(userType: String) {
        when (userType.lowercase()) {
            "user" -> {
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish()
            }
            "rescuer" -> {
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish()
            }
            "admin" -> {
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish()
            }
            "organization" -> {
                // Check if center details are filled
                checkCenterDetailsAndNavigate()
            }
            else -> {
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCenterDetailsAndNavigate() {
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val cachedCenterId = sharedPref.getInt("center_id", -1)
        val cachedCenterName = sharedPref.getString("center_name", null)

        if (userId == -1) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.checkCenterDetails(userId)
            .enqueue(object : Callback<CheckCenterResponse> {

                override fun onResponse(
                    call: Call<CheckCenterResponse>,
                    response: Response<CheckCenterResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!

                        if (result.has_center_details) {
                            // Center details already filled, save center info and go to dashboard
                            val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("center_id", result.center_id ?: -1)
                                putString("center_name", result.center_name ?: "")
                                apply()
                            }

                            // Navigate to center dashboard
                            startActivity(Intent(this@LoginActivity, CenterDashboardActivity::class.java))
                            finish()
                        } else {
                            // Center details not filled, go to CenterDetailsActivity
                            Toast.makeText(
                                this@LoginActivity,
                                "Please complete your center registration",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@LoginActivity, CenterDetailsActivity::class.java))
                            finish()
                        }
                    } else {
                        // API error - check if we have cached center data
                        handleNetworkErrorWithCache()
                    }
                }

                override fun onFailure(call: Call<CheckCenterResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // On network error - check if we have cached center data
                    handleNetworkErrorWithCache()
                }
                
                private fun handleNetworkErrorWithCache() {
                    // If we have cached center data, use it instead of redirecting to details page
                    if (cachedCenterId != -1 && !cachedCenterName.isNullOrEmpty()) {
                        // Use cached data and go to dashboard
                        startActivity(Intent(this@LoginActivity, CenterDashboardActivity::class.java))
                        finish()
                    } else {
                        // No cached data, must fill center details
                        Toast.makeText(
                            this@LoginActivity,
                            "Please complete your center registration",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@LoginActivity, CenterDetailsActivity::class.java))
                        finish()
                    }
                }
            })
    }
}

