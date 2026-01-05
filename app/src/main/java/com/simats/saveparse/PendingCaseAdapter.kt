package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PendingCaseAdapter(
    private val cases: List<PendingCase>,
    private val onItemClick: (PendingCase) -> Unit
) : RecyclerView.Adapter<PendingCaseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
        val tvReportedTime: TextView = view.findViewById(R.id.tvReportedTime)
        val tvCaseType: TextView = view.findViewById(R.id.tvCaseType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_case, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val case = cases[position]

        holder.tvCaseId.text = "Case #${case.case_id}"
        
        // Format time ago
        val minutes = case.case_age_minutes
        holder.tvReportedTime.text = when {
            minutes < 60 -> "Reported $minutes min ago"
            minutes < 1440 -> "Reported ${minutes / 60}h ago"
            else -> "Reported ${minutes / 1440}d ago"
        }

        // Case type badge
        holder.tvCaseType.text = case.case_type
        holder.tvCaseType.setBackgroundResource(
            if (case.case_type == "Critical") R.drawable.bg_badge_critical
            else R.drawable.bg_badge_standard
        )

        // Item click
        holder.itemView.setOnClickListener {
            onItemClick(case)
        }
    }

    override fun getItemCount() = cases.size
}
