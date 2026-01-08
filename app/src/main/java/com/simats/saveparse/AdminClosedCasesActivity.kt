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

class AdminClosedCasesActivity : AppCompatActivity() {

    private lateinit var rvCases: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_closed_cases)

        rvCases = findViewById(R.id.rvCases)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        rvCases.layoutManager = LinearLayoutManager(this)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadClosedCases()
    }

    private fun loadClosedCases() {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.getAdminClosedCases().enqueue(object : Callback<AdminClosedCasesResponse> {
            override fun onResponse(
                call: Call<AdminClosedCasesResponse>,
                response: Response<AdminClosedCasesResponse>
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
                        rvCases.adapter = ClosedCaseAdapter(cases) { case ->
                            // Navigate to closed case detail
                            val intent = Intent(this@AdminClosedCasesActivity, AdminClosedCaseDetailActivity::class.java)
                            intent.putExtra("case_id", case.case_id)
                            intent.putExtra("center_id", case.center_id)
                            intent.putExtra("center_name", case.center_name)
                            intent.putExtra("case_took_up_time", case.case_took_up_time)
                            intent.putExtra("rescued_photo", case.rescued_photo)
                            intent.putExtra("original_photo", case.original_photo)
                            intent.putExtra("type_of_animal", case.type_of_animal)
                            startActivity(intent)
                        }
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                    Log.e("AdminClosedCases", "Failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminClosedCasesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                Log.e("AdminClosedCases", "Error: ${t.message}")
            }
        })
    }

    inner class ClosedCaseAdapter(
        private val cases: List<AdminClosedCase>,
        private val onClick: (AdminClosedCase) -> Unit
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
            holder.tvCaseType.text = "Closed"
            holder.tvCaseType.setBackgroundResource(R.drawable.bg_status_green)
            holder.tvCaseType.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))

            // Show center and time
            holder.tvReportedTime.text = "By ${case.center_name ?: "Center"} • ${formatDate(case.case_took_up_time)}"

            // Load rescued photo if available, otherwise original
            // Note: rescued_photo already contains "uploads/" prefix from database
            val photoUrl = if (!case.rescued_photo.isNullOrEmpty()) {
                if (case.rescued_photo.startsWith("uploads/")) {
                    ApiClient.IMAGE_BASE_URL + case.rescued_photo
                } else {
                    ApiClient.IMAGE_BASE_URL + "uploads/" + case.rescued_photo
                }
            } else if (!case.original_photo.isNullOrEmpty()) {
                ApiClient.IMAGE_BASE_URL + "uploads/" + case.original_photo
            } else null

            if (photoUrl != null) {
                Glide.with(holder.itemView.context)
                    .load(photoUrl)
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
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateTime.take(10)
            }
        }
    }
}
