package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CenterCasesHandledActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private var centerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_cases_handled)

        rvCases = findViewById(R.id.rvCases)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        rvCases.layoutManager = LinearLayoutManager(this)

        // Get center ID from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadCasesHandled()
    }

    private fun loadCasesHandled() {
        if (centerId == -1) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Center not found"
            return
        }

        progressBar.visibility = View.VISIBLE

        ApiClient.api.getCenterProfile(centerId).enqueue(object : Callback<CenterProfileResponse> {
            override fun onResponse(
                call: Call<CenterProfileResponse>,
                response: Response<CenterProfileResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.handled_cases ?: emptyList()

                    if (cases.isEmpty()) {
                        rvCases.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rvCases.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        rvCases.adapter = CasesAdapter(cases)
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Failed to load cases"
                }
            }

            override fun onFailure(call: Call<CenterProfileResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Network error"
                Log.e("CenterCasesHandled", "Failed to load cases", t)
            }
        })
    }

    inner class CasesAdapter(
        private val cases: List<CenterHandledCase>
    ) : RecyclerView.Adapter<CasesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
            val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
            val tvCompletionDate: TextView = view.findViewById(R.id.tvCompletionDate)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_handled_case, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val caseItem = cases[position]
            holder.tvAnimalType.text = caseItem.type_of_animal ?: "Animal"
            holder.tvCaseId.text = "Case #${caseItem.case_id}"
            holder.tvCompletionDate.text = "Completed: ${formatDate(caseItem.case_took_up_time)}"
            holder.tvStatus.text = caseItem.rescue_status ?: "Closed"
        }

        override fun getItemCount() = cases.size

        private fun formatDate(dateTime: String?): String {
            if (dateTime == null) return "N/A"
            return try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateTime.take(10)
            }
        }
    }
}
