package com.simats.saveparse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AcceptedCaseAdapter(
    private val cases: List<AcceptedCase>,
    private val onUpdate: (AcceptedCase) -> Unit
) : RecyclerView.Adapter<AcceptedCaseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAnimalPhoto: ImageView = view.findViewById(R.id.ivAnimalPhoto)
        val tvAnimalType: TextView = view.findViewById(R.id.tvAnimalType)
        val tvCaseId: TextView = view.findViewById(R.id.tvCaseId)
        val tvCondition: TextView = view.findViewById(R.id.tvCondition)
        val tvAcceptedTime: TextView = view.findViewById(R.id.tvAcceptedTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivReached: ImageView = view.findViewById(R.id.ivReached)
        val ivSpotted: ImageView = view.findViewById(R.id.ivSpotted)
        val ivRescued: ImageView = view.findViewById(R.id.ivRescued)
        val lineReached: View = view.findViewById(R.id.lineReached)
        val lineSpotted: View = view.findViewById(R.id.lineSpotted)
        val btnUpdate: Button = view.findViewById(R.id.btnUpdate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accepted_case, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val case = cases[position]
        val context = holder.itemView.context
        val greenColor = ContextCompat.getColor(context, R.color.green_primary)
        val grayColor = ContextCompat.getColor(context, android.R.color.darker_gray)

        // Load image
        if (!case.photo.isNullOrEmpty()) {
            val imageUrl = ApiClient.IMAGE_BASE_URL + "uploads/" + case.photo
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_rounded_image)
                .into(holder.ivAnimalPhoto)
        }

        holder.tvAnimalType.text = case.type_of_animal ?: "Animal"
        holder.tvCaseId.text = "Case #${case.case_id}"
        holder.tvCondition.text = case.animal_condition ?: ""
        holder.tvAcceptedTime.text = "Accepted: ${case.case_took_up_time ?: ""}"

        // Status badge
        val status = case.rescue_status ?: "Inprogress"
        holder.tvStatus.text = status
        holder.tvStatus.setBackgroundResource(
            if (status == "Closed") R.drawable.bg_badge_standard
            else R.drawable.bg_badge_critical
        )

        // Progress indicators
        val reached = case.reached_location == "Yes"
        val spotted = case.spot_animal == "Yes"
        val rescued = status == "Closed"  // Case is rescued when closed

        holder.ivReached.setColorFilter(if (reached) greenColor else grayColor)
        holder.lineReached.setBackgroundColor(if (reached) greenColor else grayColor)

        holder.ivSpotted.setColorFilter(if (spotted) greenColor else grayColor)
        holder.lineSpotted.setBackgroundColor(if (spotted) greenColor else grayColor)

        holder.ivRescued.setColorFilter(if (rescued) greenColor else grayColor)

        // Update button
        if (status == "Closed") {
            holder.btnUpdate.text = "Closed"
            holder.btnUpdate.isEnabled = false
            holder.btnUpdate.setBackgroundColor(grayColor)
        } else {
            holder.btnUpdate.text = "Update Progress"
            holder.btnUpdate.isEnabled = true
            holder.btnUpdate.setOnClickListener { onUpdate(case) }
        }
    }

    override fun getItemCount() = cases.size
}
