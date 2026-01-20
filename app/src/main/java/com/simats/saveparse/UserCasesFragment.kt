package com.simats.saveparse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserCasesFragment : Fragment() {

    private lateinit var rvCases: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private var tabType: String = "ongoing" // "ongoing" or "closed"

    companion object {
        private const val ARG_TAB_TYPE = "tab_type"

        fun newInstance(tabType: String): UserCasesFragment {
            val fragment = UserCasesFragment()
            val args = Bundle()
            args.putString(ARG_TAB_TYPE, tabType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabType = arguments?.getString(ARG_TAB_TYPE) ?: "ongoing"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_cases_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCases = view.findViewById(R.id.rvCases)
        progressBar = view.findViewById(R.id.progressBar)
        emptyState = view.findViewById(R.id.emptyState)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle)

        rvCases.layoutManager = LinearLayoutManager(requireContext())

        // Set empty state text based on tab type
        if (tabType == "closed") {
            tvEmptyTitle.text = "No Closed Cases"
            tvEmptySubtitle.text = "Completed rescues will appear here"
        } else {
            tvEmptyTitle.text = getString(R.string.no_cases_found)
            tvEmptySubtitle.text = getString(R.string.report_first_case)
        }

        fetchCases()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when fragment becomes visible
        fetchCases()
    }

    private fun fetchCases() {
        val sharedPref = requireContext().getSharedPreferences("SavePawsPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        rvCases.visibility = View.GONE
        emptyState.visibility = View.GONE

        ApiClient.api.getUserCasesByStatus(userId, tabType).enqueue(object : Callback<OngoingCasesResponse> {
            override fun onResponse(
                call: Call<OngoingCasesResponse>,
                response: Response<OngoingCasesResponse>
            ) {
                if (!isAdded) return // Fragment not attached

                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val cases = response.body()?.cases ?: emptyList()

                    if (cases.isNotEmpty()) {
                        rvCases.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE

                        val adapter = OngoingCasesAdapter(cases) { case ->
                            // Navigate to CaseTrackActivity
                            val intent = Intent(requireContext(), CaseTrackActivity::class.java)
                            intent.putExtra("case_id", case.caseId)
                            intent.putExtra("photo", case.photo)
                            intent.putExtra("animal_type", case.typeOfAnimal)
                            intent.putExtra("condition", case.animalCondition)
                            intent.putExtra("status", case.caseStatus)
                            startActivity(intent)
                        }
                        rvCases.adapter = adapter
                    } else {
                        rvCases.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load cases",
                        Toast.LENGTH_SHORT
                    ).show()
                    emptyState.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<OngoingCasesResponse>, t: Throwable) {
                if (!isAdded) return // Fragment not attached

                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
