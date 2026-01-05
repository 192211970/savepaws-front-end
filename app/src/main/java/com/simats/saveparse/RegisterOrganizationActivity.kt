package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterOrganizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_org)

        val etOrgName = findViewById<EditText>(R.id.etOrganizationName)
        val etPersonName = findViewById<EditText>(R.id.etPersonName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegisterOrg)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnBack.setOnClickListener { finish() }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {
            val name = etOrgName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerOrganization(name, phone, email, password)
        }
    }

    private fun registerOrganization(
        name: String,
        phone: String,
        email: String,
        password: String
    ) {
        ApiClient.api.registerOrg(
            name = name,
            phone = phone,
            email = email,
            password = password,
            userType = "organization"
        ).enqueue(object : Callback<RegisterResponse> {

            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        this@RegisterOrganizationActivity,
                        response.body()!!.message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (response.body()!!.status == "success") {
                        startActivity(
                            Intent(
                                this@RegisterOrganizationActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterOrganizationActivity,
                        "Server error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(
                    this@RegisterOrganizationActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
