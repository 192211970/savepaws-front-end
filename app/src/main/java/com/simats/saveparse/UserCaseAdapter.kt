package com.simats.saveparse

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class UserCaseAdapter(
    private val cases: List<UserCase>,
    private val onItemClick: (UserCase) -> Unit
) : RecyclerView.Adapter<UserCaseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAnimal: ImageView = view.findViewById(R.id.imgAnimal)
        val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
        val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
        val tvCondition: TextView = view.findViewById(R.id.tvCondition)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvCenterName: TextView = view.findViewById(R.id.tvCenterName)
        val tvReportedTime: TextView = view.findViewById(R.id.tvReportedTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_case, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val case = cases[position]

        holder.tvAnimalType.text = case.animalType
        holder.tvCaseId.text = "Case #${case.caseId}"
        holder.tvCondition.text = case.condition

        // Set condition color
        holder.tvCondition.setTextColor(
            when (case.condition.lowercase()) {
                "critical" -> Color.parseColor("#F44336")
                "injured" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#4CAF50")
            }
        )

        // Set status badge
        holder.tvStatus.text = case.status
        holder.tvStatus.setBackgroundResource(
            when (case.status.lowercase()) {
                "reported" -> R.drawable.bg_status_pending
                "accepted" -> R.drawable.bg_status_accepted
                "closed" -> R.drawable.bg_status_closed
                else -> R.drawable.bg_status_badge
            }
        )

        // Center name - show "Waiting for response" if not accepted yet
        holder.tvCenterName.visibility = View.VISIBLE
        if (!case.centerName.isNullOrEmpty()) {
            holder.tvCenterName.text = case.centerName
            holder.tvCenterName.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvCenterName.text = "Waiting for response..."
            holder.tvCenterName.setTextColor(Color.parseColor("#FF9800"))
        }

        // Format date
        holder.tvReportedTime.text = "Reported: ${formatDate(case.reportedTime)}"

        // Load image
        val imageUrl = "https://pmgjc2px-80.inc1.devtunnels.ms/savepaws/uploads/${case.photo}"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_paw)
            .error(R.drawable.ic_paw)
            .centerCrop()
            .into(holder.imgAnimal)

        holder.itemView.setOnClickListener { onItemClick(case) }
    }

    override fun getItemCount() = cases.size

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
