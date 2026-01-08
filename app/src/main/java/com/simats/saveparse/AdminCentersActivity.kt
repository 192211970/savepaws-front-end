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

class AdminCentersActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar

    private var operatingCenters: List<AdminCenterListItem> = emptyList()
    private var deactivatedCenters: List<AdminCenterListItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_centers)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadCenters()
    }

    override fun onResume() {
        super.onResume()
        loadCenters()
    }

    private fun loadCenters() {
        progressBar.visibility = View.VISIBLE

        ApiClient.api.getAdminAllCenters().enqueue(object : Callback<AdminCentersListResponse> {
            override fun onResponse(
                call: Call<AdminCentersListResponse>,
                response: Response<AdminCentersListResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val allCenters = response.body()?.centers ?: emptyList()

                    // Separate into operating and deactivated based on center_status only
                    operatingCenters = allCenters.filter { 
                        it.center_status != "Deactivated"
                    }
                    deactivatedCenters = allCenters.filter { 
                        it.center_status == "Deactivated"
                    }

                    setupViewPager()
                } else {
                    Log.e("AdminCenters", "Failed to load centers: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AdminCentersListResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("AdminCenters", "Network error: ${t.message}")
            }
        })
    }

    private fun setupViewPager() {
        viewPager.adapter = CentersPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Operating (${operatingCenters.size})"
                1 -> "Deactivated (${deactivatedCenters.size})"
                else -> ""
            }
        }.attach()
    }

    inner class CentersPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CenterListFragment.newInstance(operatingCenters)
                1 -> CenterListFragment.newInstance(deactivatedCenters)
                else -> CenterListFragment.newInstance(emptyList())
            }
        }
    }

    class CenterListFragment : Fragment() {

        private var centers: List<AdminCenterListItem> = emptyList()

        companion object {
            private var centersList: List<AdminCenterListItem> = emptyList()

            fun newInstance(list: List<AdminCenterListItem>): CenterListFragment {
                centersList = list
                return CenterListFragment()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            centers = centersList
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_center_list, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val rvCenters = view.findViewById<RecyclerView>(R.id.rvCenters)
            val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

            rvCenters.layoutManager = LinearLayoutManager(requireContext())

            if (centers.isEmpty()) {
                rvCenters.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            } else {
                rvCenters.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                rvCenters.adapter = CentersAdapter(centers) { center ->
                    val intent = Intent(requireContext(), AdminCenterDetailActivity::class.java)
                    intent.putExtra("center_id", center.center_id)
                    intent.putExtra("center_name", center.center_name)
                    startActivity(intent)
                }
            }
        }

        inner class CentersAdapter(
            private val centers: List<AdminCenterListItem>,
            private val onClick: (AdminCenterListItem) -> Unit
        ) : RecyclerView.Adapter<CentersAdapter.ViewHolder>() {

            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val tvCenterId: TextView = view.findViewById(R.id.tvCenterId)
                val tvCenterName: TextView = view.findViewById(R.id.tvCenterName)
                val tvCasesHandled: TextView = view.findViewById(R.id.tvCasesHandled)
                val tvCenterStatus: TextView = view.findViewById(R.id.tvCenterStatus)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_admin_center, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val center = centers[position]

                holder.tvCenterId.text = "Center #${center.center_id}"
                holder.tvCenterName.text = center.center_name ?: "Unknown Center"
                holder.tvCasesHandled.text = "${center.cases_handled ?: 0} cases handled"

                // Set status badge
                if (center.is_active == "Yes" || center.center_status == "Operating") {
                    holder.tvCenterStatus.text = "Active"
                    holder.tvCenterStatus.setBackgroundResource(R.drawable.bg_status_green)
                    holder.tvCenterStatus.setTextColor(holder.itemView.context.getColor(R.color.green_dark))
                } else {
                    holder.tvCenterStatus.text = "Inactive"
                    holder.tvCenterStatus.setBackgroundResource(R.drawable.bg_status_orange)
                    holder.tvCenterStatus.setTextColor(holder.itemView.context.getColor(R.color.orange_dark))
                }

                holder.itemView.setOnClickListener { onClick(center) }
            }

            override fun getItemCount() = centers.size
        }
    }
}
