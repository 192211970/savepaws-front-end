package com.simats.saveparse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OngoingCasesActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_cases)

        // Initialize views
        rvCases = findViewById(R.id.rvCases)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        btnBack = findViewById(R.id.btnBack)
        bottomNav = findViewById(R.id.bottomNav)

        // Setup RecyclerView
        rvCases.layoutManager = LinearLayoutManager(this)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Setup Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_track // Highlight Track tab
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_track -> {
                    // Already on Track page
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Get user ID from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId != -1) {
            fetchOngoingCases(userId)
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchOngoingCases(userId: Int) {
        progressBar.visibility = View.VISIBLE
        rvCases.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getOngoingCases(userId).enqueue(object : Callback<OngoingCasesResponse> {
            override fun onResponse(
                call: Call<OngoingCasesResponse>,
                response: Response<OngoingCasesResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.cases ?: emptyList()
                    
                    if (cases.isNotEmpty()) {
                        rvCases.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                        
                        val adapter = OngoingCasesAdapter(cases) { case ->
                            // Navigate to CaseTrackActivity
                            val intent = Intent(this@OngoingCasesActivity, CaseTrackActivity::class.java)
                            intent.putExtra("case_id", case.caseId)
                            intent.putExtra("photo", case.photo)
                            intent.putExtra("animal_type", case.typeOfAnimal)
                            intent.putExtra("condition", case.animalCondition)
                            intent.putExtra("status", case.caseStatus)
                            startActivity(intent)
                        }
                        rvCases.adapter = adapter
                    } else {
                        rvCases.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@OngoingCasesActivity,
                        "Failed to load cases",
                        Toast.LENGTH_SHORT
                    ).show()
                    emptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<OngoingCasesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(
                    this@OngoingCasesActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
