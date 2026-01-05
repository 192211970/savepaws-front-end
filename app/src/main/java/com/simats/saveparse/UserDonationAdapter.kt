package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class UserDonationAdapter(
    private val donations: List<UserDonation>
) : RecyclerView.Adapter<UserDonationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCenterName: TextView = view.findViewById(R.id.tvCenterName)
        val tvPaymentMethod: TextView = view.findViewById(R.id.tvPaymentMethod)
        val tvPaymentTime: TextView = view.findViewById(R.id.tvPaymentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_donation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donation = donations[position]

        holder.tvAnimalType.text = "${donation.animalType} Rescue"
        holder.tvAmount.text = "₹${donation.amount.toInt()}"
        holder.tvCenterName.text = "To: ${donation.centerName}"
        holder.tvPaymentMethod.text = donation.paymentMethod ?: "N/A"
        holder.tvPaymentTime.text = formatDate(donation.paymentTime)
    }

    override fun getItemCount() = donations.size

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"
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

