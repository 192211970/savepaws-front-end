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
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminPendingCasesActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: androidx.viewpager2.widget.ViewPager2

    private var criticalCases: List<AdminPendingCase> = emptyList()
    private var standardCases: List<AdminPendingCase> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pending_cases)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadPendingCases()
    }

    private fun loadPendingCases() {
        ApiClient.api.getAdminPendingCases().enqueue(object : Callback<AdminPendingCasesResponse> {
            override fun onResponse(
                call: Call<AdminPendingCasesResponse>,
                response: Response<AdminPendingCasesResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    criticalCases = response.body()?.critical_cases ?: emptyList()
                    standardCases = response.body()?.standard_cases ?: emptyList()
                    setupViewPager()
                } else {
                    Log.e("AdminPendingCases", "Failed to load: ${response.code()}")
                    setupViewPager() // Show empty state
                }
            }

            override fun onFailure(call: Call<AdminPendingCasesResponse>, t: Throwable) {
                Log.e("AdminPendingCases", "Network error: ${t.message}")
                setupViewPager() // Show empty state
            }
        })
    }

    private fun setupViewPager() {
        viewPager.adapter = PendingCasesPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Critical (${criticalCases.size})"
                1 -> "Standard (${standardCases.size})"
                else -> ""
            }
        }.attach()
    }

    // ViewPager Adapter
    inner class PendingCasesPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PendingCasesFragment.newInstance(criticalCases, "Critical")
                1 -> PendingCasesFragment.newInstance(standardCases, "Standard")
                else -> PendingCasesFragment.newInstance(emptyList(), "")
            }
        }
    }

    // Fragment for each tab
    class PendingCasesFragment : Fragment() {

        private var cases: List<AdminPendingCase> = emptyList()
        private var caseType: String = ""

        companion object {
            private var cachedCriticalCases: List<AdminPendingCase> = emptyList()
            private var cachedStandardCases: List<AdminPendingCase> = emptyList()

            fun newInstance(cases: List<AdminPendingCase>, type: String): PendingCasesFragment {
                if (type == "Critical") cachedCriticalCases = cases
                else cachedStandardCases = cases
                
                return PendingCasesFragment().apply {
                    this.caseType = type
                }
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_case_list, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            cases = if (caseType == "Critical") cachedCriticalCases else cachedStandardCases

            val rvCases = view.findViewById<RecyclerView>(R.id.rvCases)
            val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

            rvCases.layoutManager = LinearLayoutManager(context)

            if (cases.isEmpty()) {
                rvCases.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "No $caseType cases"
            } else {
                rvCases.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                rvCases.adapter = PendingCaseAdapter(cases) { case ->
                    // Navigate to case detail
                    val intent = Intent(context, AdminPendingCaseDetailActivity::class.java)
                    intent.putExtra("case_id", case.case_id)
                    intent.putExtra("type_of_animal", case.type_of_animal)
                    intent.putExtra("photo", case.photo)
                    intent.putExtra("created_time", case.created_time)
                    intent.putExtra("case_type", case.case_type)
                    intent.putExtra("remark", case.remark)
                    intent.putExtra("latitude", case.latitude)
                    intent.putExtra("longitude", case.longitude)
                    intent.putExtra("centers_escalated", case.centers_escalated?.joinToString(",") ?: "")
                    startActivity(intent)
                }
            }
        }
    }

    // Adapter for case list
    class PendingCaseAdapter(
        private val cases: List<AdminPendingCase>,
        private val onClick: (AdminPendingCase) -> Unit
    ) : RecyclerView.Adapter<PendingCaseAdapter.ViewHolder>() {

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
            holder.tvCaseType.text = case.case_type ?: "Standard"

            // Set case type badge color
            if (case.case_type == "Critical") {
                holder.tvCaseType.setBackgroundResource(R.drawable.bg_status_orange)
                holder.tvCaseType.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            } else {
                holder.tvCaseType.setBackgroundResource(R.drawable.bg_status_green)
                holder.tvCaseType.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }

            // Time ago
            val ageMinutes = case.case_age_minutes ?: 0
            holder.tvReportedTime.text = when {
                ageMinutes < 60 -> "Reported $ageMinutes mins ago"
                ageMinutes < 1440 -> "Reported ${ageMinutes / 60} hours ago"
                else -> "Reported ${ageMinutes / 1440} days ago"
            }

            // Load image
            if (!case.photo.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load("http://10.0.2.2/uploads/${case.photo}")
                    .placeholder(R.drawable.ic_paw)
                    .into(holder.ivPhoto)
            }

            holder.itemView.setOnClickListener { onClick(case) }
        }

        override fun getItemCount() = cases.size
    }
}
