package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AcceptedCasesActivity : AppCompatActivity() {

    private var centerId: Int = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accepted_cases)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerAcceptedCases)
        emptyState = findViewById(R.id.emptyState)
        progressBar = findViewById(R.id.progressBar)
        tvCount = findViewById(R.id.tvCount)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get center_id from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadAcceptedCases()
    }

    private fun loadAcceptedCases() {
        if (centerId == -1) {
            Toast.makeText(this, "Center not found", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        ApiClient.api.getCenterAcceptedCases(centerId)
            .enqueue(object : Callback<AcceptedCasesResponse> {

                override fun onResponse(
                    call: Call<AcceptedCasesResponse>,
                    response: Response<AcceptedCasesResponse>
                ) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        tvCount.text = "${data.total_accepted} cases"

                        if (data.cases.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyState.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE

                            val adapter = AcceptedCaseAdapter(data.cases) { case ->
                                // Navigate to update rescue activity
                                val intent = Intent(
                                    this@AcceptedCasesActivity,
                                    UpdateRescueActivity::class.java
                                )
                                intent.putExtra("case_id", case.case_id)
                                intent.putExtra("reached", case.reached_location == "Yes")
                                intent.putExtra("spotted", case.spot_animal == "Yes")
                                intent.putExtra("rescued", case.rescue_status == "Closed")
                                intent.putExtra("photo", case.photo)
                                intent.putExtra("animal_type", case.type_of_animal)
                                intent.putExtra("latitude", case.latitude?.toDoubleOrNull() ?: 0.0)
                                intent.putExtra("longitude", case.longitude?.toDoubleOrNull() ?: 0.0)
                                startActivity(intent)
                            }
                            recyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(
                            this@AcceptedCasesActivity,
                            "Failed to load cases",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AcceptedCasesResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AcceptedCasesActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
