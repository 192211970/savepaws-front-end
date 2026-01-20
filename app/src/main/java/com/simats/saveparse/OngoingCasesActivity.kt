package com.simats.saveparse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OngoingCasesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val tabTitles = arrayOf("Ongoing", "Closed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_cases)

        // Initialize views
        btnBack = findViewById(R.id.btnBack)
        bottomNav = findViewById(R.id.bottomNav)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Setup ViewPager with adapter
        viewPager.adapter = CasesPagerAdapter(this)

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Setup Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_track
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_track -> {
                    // Already on Track page
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    /**
     * ViewPager adapter for the Ongoing/Closed tabs
     */
    private inner class CasesPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UserCasesFragment.newInstance("ongoing")
                1 -> UserCasesFragment.newInstance("closed")
                else -> UserCasesFragment.newInstance("ongoing")
            }
        }
    }
}
