package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class OngoingCasesAdapter(
    private val cases: List<OngoingCase>,
    private val onCaseClick: (OngoingCase) -> Unit
) : RecyclerView.Adapter<OngoingCasesAdapter.CaseViewHolder>() {

    class CaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAnimalPhoto: ImageView = view.findViewById(R.id.ivAnimalPhoto)
        val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
        val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
        val tvCondition: TextView = view.findViewById(R.id.tvCondition)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_case, parent, false)
        return CaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val case = cases[position]
        val context = holder.itemView.context

        // Set animal type
        holder.tvAnimalType.text = case.typeOfAnimal

        // Set case ID
        holder.tvCaseId.text = "Case #${case.caseId}"

        // Set condition
        holder.tvCondition.text = case.animalCondition

        // Set date - format the timestamp
        holder.tvDate.text = formatDate(case.createdTime)

        // Set status with appropriate color
        holder.tvStatus.text = case.caseStatus
        val statusColor = when (case.caseStatus) {
            "Reported" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            "Accepted" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
            "Closed" -> ContextCompat.getColor(context, android.R.color.darker_gray)
            else -> ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        }
        holder.tvStatus.background.setTint(statusColor)

        // Load image using Glide
        val imageUrl = "${ApiClient.retrofit.baseUrl()}uploads/${case.photo}"
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_alert)
            .error(R.drawable.ic_alert)
            .centerCrop()
            .into(holder.ivAnimalPhoto)

        // Click listener
        holder.itemView.setOnClickListener {
            onCaseClick(case)
        }
    }

    override fun getItemCount(): Int = cases.size

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}
