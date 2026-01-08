package com.simats.saveparse

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class CenterClosedCasesActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvCaseCount: TextView
    private lateinit var progressBar: ProgressBar

    private var centerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_closed_cases)

        rvCases = findViewById(R.id.rvCases)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvCaseCount = findViewById(R.id.tvCaseCount)
        progressBar = findViewById(R.id.progressBar)

        rvCases.layoutManager = LinearLayoutManager(this)

        // Get center ID from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadClosedCases()
    }

    private fun loadClosedCases() {
        if (centerId == -1) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Center not found"
            return
        }

        progressBar.visibility = View.VISIBLE

        // Use existing getCenterProfile API which already returns handled_cases (closed cases)
        ApiClient.api.getCenterProfile(centerId).enqueue(object : Callback<CenterProfileResponse> {
            override fun onResponse(
                call: Call<CenterProfileResponse>,
                response: Response<CenterProfileResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.handled_cases ?: emptyList()

                    tvCaseCount.text = "${cases.size} cases"

                    if (cases.isEmpty()) {
                        rvCases.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rvCases.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        rvCases.adapter = ClosedCaseAdapter(cases)
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
                Log.e("CenterClosedCases", "Error: ${t.message}")
            }
        })
    }

    // Using CenterHandledCase from CenterModels.kt (already exists)
    inner class ClosedCaseAdapter(
        private val cases: List<CenterHandledCase>
    ) : RecyclerView.Adapter<ClosedCaseAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPhoto: ImageView = view.findViewById(R.id.ivAnimalPhoto)
            val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
            val tvCaseType: TextView = view.findViewById(R.id.tvCaseType)
            val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
            val tvReportedTime: TextView = view.findViewById(R.id.tvReportedTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_pending_case, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val case = cases[position]

            holder.tvCaseId.text = "Case #${case.case_id}"
            holder.tvAnimalType.text = case.type_of_animal ?: "Animal"
            holder.tvCaseType.text = case.rescue_status ?: "Closed"
            holder.tvCaseType.setBackgroundResource(R.drawable.bg_status_green)
            holder.tvCaseType.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))

            holder.tvReportedTime.text = "Closed: ${formatDate(case.case_took_up_time)}"

            // Load image
            if (!case.photo.isNullOrEmpty()) {
                val imageUrl = ApiClient.IMAGE_BASE_URL + "uploads/" + case.photo
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_paw)
                    .into(holder.ivPhoto)
            }
        }

        override fun getItemCount() = cases.size

        private fun formatDate(dateTime: String?): String {
            if (dateTime == null) return ""
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateTime.take(10)
            }
        }
    }
}
