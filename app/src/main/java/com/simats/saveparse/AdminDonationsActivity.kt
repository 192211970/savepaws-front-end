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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class AdminDonationsActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar

    private var pendingDonations: List<AdminDonationListItem> = emptyList()
    private var approvedDonations: List<AdminDonationListItem> = emptyList()
    private var rejectedDonations: List<AdminDonationListItem> = emptyList()
    private var paidDonations: List<AdminDonationListItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_donations)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadAllDonations()
    }

    override fun onResume() {
        super.onResume()
        loadAllDonations()
    }

    private fun loadAllDonations() {
        progressBar.visibility = View.VISIBLE
        var completedCalls = 0
        val totalCalls = 4

        fun checkComplete() {
            completedCalls++
            if (completedCalls >= totalCalls) {
                progressBar.visibility = View.GONE
                setupViewPager()
            }
        }

        // Load Pending
        ApiClient.api.getAdminDonationsList("pending").enqueue(object : Callback<AdminDonationsListResponse> {
            override fun onResponse(call: Call<AdminDonationsListResponse>, response: Response<AdminDonationsListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingDonations = response.body()?.donations ?: emptyList()
                }
                checkComplete()
            }
            override fun onFailure(call: Call<AdminDonationsListResponse>, t: Throwable) {
                Log.e("AdminDonations", "Error loading pending: ${t.message}")
                checkComplete()
            }
        })

        // Load Approved
        ApiClient.api.getAdminDonationsList("approved").enqueue(object : Callback<AdminDonationsListResponse> {
            override fun onResponse(call: Call<AdminDonationsListResponse>, response: Response<AdminDonationsListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    approvedDonations = response.body()?.donations ?: emptyList()
                }
                checkComplete()
            }
            override fun onFailure(call: Call<AdminDonationsListResponse>, t: Throwable) {
                Log.e("AdminDonations", "Error loading approved: ${t.message}")
                checkComplete()
            }
        })

        // Load Rejected
        ApiClient.api.getAdminDonationsList("rejected").enqueue(object : Callback<AdminDonationsListResponse> {
            override fun onResponse(call: Call<AdminDonationsListResponse>, response: Response<AdminDonationsListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    rejectedDonations = response.body()?.donations ?: emptyList()
                }
                checkComplete()
            }
            override fun onFailure(call: Call<AdminDonationsListResponse>, t: Throwable) {
                Log.e("AdminDonations", "Error loading rejected: ${t.message}")
                checkComplete()
            }
        })

        // Load Paid
        ApiClient.api.getAdminDonationsList("paid").enqueue(object : Callback<AdminDonationsListResponse> {
            override fun onResponse(call: Call<AdminDonationsListResponse>, response: Response<AdminDonationsListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    paidDonations = response.body()?.donations ?: emptyList()
                }
                checkComplete()
            }
            override fun onFailure(call: Call<AdminDonationsListResponse>, t: Throwable) {
                Log.e("AdminDonations", "Error loading paid: ${t.message}")
                checkComplete()
            }
        })
    }

    private fun setupViewPager() {
        viewPager.adapter = DonationsPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending (${pendingDonations.size})"
                1 -> "Approved (${approvedDonations.size})"
                2 -> "Rejected (${rejectedDonations.size})"
                3 -> "Paid (${paidDonations.size})"
                else -> ""
            }
        }.attach()
    }

    inner class DonationsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 4

        override fun createFragment(position: Int): Fragment {
            val status = when (position) {
                0 -> "pending"
                1 -> "approved"
                2 -> "rejected"
                3 -> "paid"
                else -> "pending"
            }
            val donations = when (position) {
                0 -> pendingDonations
                1 -> approvedDonations
                2 -> rejectedDonations
                3 -> paidDonations
                else -> emptyList()
            }
            return DonationListFragment.newInstance(donations, status)
        }
    }

    class DonationListFragment : Fragment() {

        private var donations: List<AdminDonationListItem> = emptyList()
        private var status: String = "pending"

        companion object {
            private var donationsList: List<AdminDonationListItem> = emptyList()
            private var statusType: String = "pending"

            fun newInstance(list: List<AdminDonationListItem>, status: String): DonationListFragment {
                donationsList = list
                statusType = status
                return DonationListFragment()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            donations = donationsList
            status = statusType
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_donation_list, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val rvDonations = view.findViewById<RecyclerView>(R.id.rvDonations)
            val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

            rvDonations.layoutManager = LinearLayoutManager(requireContext())

            if (donations.isEmpty()) {
                rvDonations.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            } else {
                rvDonations.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                rvDonations.adapter = DonationsAdapter(donations) { donation ->
                    val intent = Intent(requireContext(), AdminDonationDetailActivity::class.java)
                    intent.putExtra("donation_id", donation.donation_id)
                    intent.putExtra("status", status)
                    startActivity(intent)
                }
            }
        }

        inner class DonationsAdapter(
            private val donations: List<AdminDonationListItem>,
            private val onClick: (AdminDonationListItem) -> Unit
        ) : RecyclerView.Adapter<DonationsAdapter.ViewHolder>() {

            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val tvDonationId: TextView = view.findViewById(R.id.tvDonationId)
                val tvCenterId: TextView = view.findViewById(R.id.tvCenterId)
                val tvAmount: TextView = view.findViewById(R.id.tvAmount)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_admin_donation, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val donation = donations[position]

                holder.tvDonationId.text = "Donation #${donation.donation_id}"
                holder.tvCenterId.text = "Center #${donation.center_id}"
                
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                holder.tvAmount.text = formatter.format(donation.amount ?: 0)

                holder.itemView.setOnClickListener { onClick(donation) }
            }

            override fun getItemCount() = donations.size
        }
    }
}
