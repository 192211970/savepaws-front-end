package com.simats.saveparse

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.simats.saveparse.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btnLogin)
        val signup = findViewById<Button>(R.id.btnSignUp)

        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signup.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }
    }
}
