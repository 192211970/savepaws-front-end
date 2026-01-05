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

class PendingCasesActivity : AppCompatActivity() {

    private var centerId: Int = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_cases)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerPendingCases)
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
        loadPendingCases()
    }

    private fun loadPendingCases() {
        if (centerId == -1) {
            Toast.makeText(this, "Center not found", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        ApiClient.api.getCenterPendingCases(centerId)
            .enqueue(object : Callback<PendingCasesResponse> {

                override fun onResponse(
                    call: Call<PendingCasesResponse>,
                    response: Response<PendingCasesResponse>
                ) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        tvCount.text = "${data.total_pending} cases"

                        if (data.cases.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyState.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE

                            val adapter = PendingCaseAdapter(data.cases) { case ->
                                // Navigate to case detail
                                val intent = Intent(
                                    this@PendingCasesActivity,
                                    CaseDetailActivity::class.java
                                )
                                intent.putExtra("case_id", case.case_id)
                                intent.putExtra("case_type", case.case_type)
                                intent.putExtra("remark", case.remark)
                                intent.putExtra("latitude", case.latitude.toDoubleOrNull() ?: 0.0)
                                intent.putExtra("longitude", case.longitude.toDoubleOrNull() ?: 0.0)
                                intent.putExtra("reported_time", case.created_time)
                                intent.putExtra("animal_type", case.type_of_animal)
                                intent.putExtra("condition", case.animal_condition)
                                intent.putExtra("photo", case.photo)
                                startActivity(intent)
                            }
                            recyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(
                            this@PendingCasesActivity,
                            "Failed to load cases",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PendingCasesResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@PendingCasesActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
