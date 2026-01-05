package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class DonationAdapter(
    private val donations: List<DonationItem>,
    private val onDonationClick: (DonationItem) -> Unit
) : RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    class DonationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAnimalPhoto: ImageView = view.findViewById(R.id.ivAnimalPhoto)
        val tvCenterName: TextView = view.findViewById(R.id.tvCenterName)
        val tvCaseInfo: TextView = view.findViewById(R.id.tvCaseInfo)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donations[position]
        val context = holder.itemView.context

        // Set center name
        holder.tvCenterName.text = donation.centerName ?: "Unknown Center"

        // Set case info
        val animalType = donation.animalType ?: "Animal"
        holder.tvCaseInfo.text = "Case #${donation.caseId} - $animalType"

        // Set amount formatted
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        holder.tvAmount.text = formatter.format(donation.amount)

        // Load image using Glide - prioritize case_photo
        val imageUrl = when {
            !donation.casePhoto.isNullOrEmpty() -> {
                "${ApiClient.retrofit.baseUrl()}uploads/${donation.casePhoto}"
            }
            !donation.imageOfAnimal.isNullOrEmpty() -> {
                "${ApiClient.retrofit.baseUrl()}uploads/${donation.imageOfAnimal}"
            }
            else -> ""
        }
        
        if (imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_alert)
                .error(R.drawable.ic_alert)
                .centerCrop()
                .into(holder.ivAnimalPhoto)
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onDonationClick(donation)
        }
    }

    override fun getItemCount(): Int = donations.size
}
