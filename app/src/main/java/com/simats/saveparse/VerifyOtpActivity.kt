package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpActivity : AppCompatActivity() {

    private var email: String = ""
    private var isOtpVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)
        
        email = intent.getStringExtra("email") ?: ""
        
        val etOtp = findViewById<TextInputEditText>(R.id.etOtp)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val tilNewPass = findViewById<TextInputLayout>(R.id.tilNewPass)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        
        tvSubtitle.text = "Enter the code sent to $email"
        
        btnVerify.setOnClickListener {
            if (!isOtpVerified) {
                // Verify OTP Step
                val otp = etOtp.text.toString().trim()
                if (otp.length < 4) {
                    Toast.makeText(this, "Enter valid OTP", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                ApiClient.api.verifyOtp(email, otp).enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            // OTP Correct -> Show Password Field
                            isOtpVerified = true
                            etOtp.isEnabled = false // Lock OTP field
                            tilNewPass.visibility = View.VISIBLE
                            btnVerify.text = "Reset Password"
                            Toast.makeText(this@VerifyOtpActivity, "OTP Verified! Set new password.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@VerifyOtpActivity, response.body()?.message ?: "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        Toast.makeText(this@VerifyOtpActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
                
            } else {
                // Reset Password Step
                val newPass = etNewPassword.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 chars", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                ApiClient.api.resetPassword(email, newPass).enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@VerifyOtpActivity, "Password reset successfully!", Toast.LENGTH_LONG).show()
                            finish() // Go back to login or previous screen
                        } else {
                            Toast.makeText(this@VerifyOtpActivity, "Failed to reset password", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                         Toast.makeText(this@VerifyOtpActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
