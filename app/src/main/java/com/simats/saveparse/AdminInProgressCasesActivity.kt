package com.simats.saveparse

import android.content.Intent
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

class AdminInProgressCasesActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_inprogress_cases)

        rvCases = findViewById(R.id.rvCases)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        rvCases.layoutManager = LinearLayoutManager(this)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadInProgressCases()
    }

    private fun loadInProgressCases() {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.getAdminInProgressCases().enqueue(object : Callback<AdminInProgressCasesResponse> {
            override fun onResponse(
                call: Call<AdminInProgressCasesResponse>,
                response: Response<AdminInProgressCasesResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.cases ?: emptyList()

                    if (cases.isEmpty()) {
                        rvCases.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rvCases.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        rvCases.adapter = InProgressCaseAdapter(cases) { case ->
                            // Navigate to case tracking
                            val intent = Intent(this@AdminInProgressCasesActivity, CaseTrackActivity::class.java)
                            intent.putExtra("case_id", case.case_id)
                            startActivity(intent)
                        }
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                    Log.e("AdminInProgress", "Failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminInProgressCasesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                Log.e("AdminInProgress", "Error: ${t.message}")
            }
        })
    }

    inner class InProgressCaseAdapter(
        private val cases: List<AdminInProgressCase>,
        private val onClick: (AdminInProgressCase) -> Unit
    ) : RecyclerView.Adapter<InProgressCaseAdapter.ViewHolder>() {

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
            holder.tvCaseType.text = case.rescue_status ?: "In Progress"
            holder.tvCaseType.setBackgroundResource(R.drawable.bg_status_blue)
            holder.tvCaseType.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))

            // Show center and time
            holder.tvReportedTime.text = "By ${case.center_name ?: "Center"} • ${formatDate(case.case_took_up_time)}"

            // Load image
            if (!case.photo.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load("http://10.0.2.2/uploads/${case.photo}")
                    .placeholder(R.drawable.ic_paw)
                    .into(holder.ivPhoto)
            }

            holder.itemView.setOnClickListener { onClick(case) }
        }

        override fun getItemCount() = cases.size

        private fun formatDate(dateTime: String?): String {
            if (dateTime == null) return ""
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateTime.take(10)
            }
        }
    }
}
