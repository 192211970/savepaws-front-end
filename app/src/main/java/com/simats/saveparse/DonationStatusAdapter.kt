package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class DonationStatusAdapter(
    private val donations: List<CenterDonationItem>
) : RecyclerView.Adapter<DonationStatusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
        val tvApprovalStatus: TextView = view.findViewById(R.id.tvApprovalStatus)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvRequestedTime: TextView = view.findViewById(R.id.tvRequestedTime)
        val layoutDonationStatus: LinearLayout = view.findViewById(R.id.layoutDonationStatus)
        val tvDonationAmount: TextView = view.findViewById(R.id.tvDonationAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donation = donations[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        holder.tvCaseId.text = "Case #${donation.case_id}"
        holder.tvAmount.text = formatter.format(donation.amount)
        holder.tvRequestedTime.text = "Requested: ${donation.requested_time ?: ""}"

        // Approval status badge
        holder.tvApprovalStatus.text = donation.approval_status ?: "Pending"
        holder.tvApprovalStatus.setBackgroundResource(
            when (donation.approval_status) {
                "Approved" -> R.drawable.bg_badge_standard
                "Rejected" -> R.drawable.bg_badge_critical
                else -> R.drawable.bg_badge_warning
            }
        )

        // Show donation received if paid
        if (donation.donation_status == "Paid" && donation.amount != null) {
            holder.layoutDonationStatus.visibility = View.VISIBLE
            holder.tvDonationAmount.text = formatter.format(donation.amount)
        } else {
            holder.layoutDonationStatus.visibility = View.GONE
        }
    }

    override fun getItemCount() = donations.size
}
