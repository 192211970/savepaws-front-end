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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserCaseHistoryActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_case_history)

        rvCases = findViewById(R.id.rvCases)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)

        rvCases.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fetchUserCases()
    }

    private fun fetchUserCases() {
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        rvCases.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getUserCases(userId).enqueue(object : Callback<UserCasesResponse> {
            override fun onResponse(
                call: Call<UserCasesResponse>,
                response: Response<UserCasesResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.cases ?: emptyList()

                    // Save total cases to SharedPreferences for profile stats
                    sharedPref.edit().putInt("total_cases", cases.size).apply()

                    if (cases.isNotEmpty()) {
                        rvCases.visibility = View.VISIBLE
                        rvCases.adapter = UserCaseAdapter(cases) { case ->
                            // Navigate to case tracking with all required data
                            val intent = Intent(this@UserCaseHistoryActivity, CaseTrackActivity::class.java)
                            intent.putExtra("case_id", case.caseId)
                            intent.putExtra("photo", case.photo ?: "")
                            intent.putExtra("animal_type", case.animalType)
                            intent.putExtra("condition", case.condition)
                            intent.putExtra("status", case.status)
                            startActivity(intent)
                        }
                    } else {
                        emptyState.visibility = View.VISIBLE
                    }
                } else {
                    emptyState.visibility = View.VISIBLE
                    Toast.makeText(
                        this@UserCaseHistoryActivity,
                        "Failed to load cases",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserCasesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(
                    this@UserCaseHistoryActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
