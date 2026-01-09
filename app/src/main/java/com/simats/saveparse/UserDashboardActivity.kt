package com.simats.saveparse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout

    /* 🔹 LOCATION CLIENT */
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)

        /* ===== LOCATION TEXT ===== */
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getUserLocation(tvLocation)

        /* ===== NAVIGATION VIEWS ===== */
        val cardReport = findViewById<androidx.cardview.widget.CardView>(R.id.cardReport)
        val cardDonate = findViewById<androidx.cardview.widget.CardView>(R.id.cardDonate)
        val cardTrack = findViewById<androidx.cardview.widget.CardView>(R.id.cardTrack)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val carouselItems = listOf(
            CarouselItem(
                R.drawable.carousel_bg_1,
                "Rescue Any Stray in Need",
                "Help injured and suffering animals with just one report"
            ),
            CarouselItem(
                R.drawable.carousel_bg_2,
                "24/7 Emergency Support",
                "Cases are monitored and responded to anytime"
            ),
            CarouselItem(
                R.drawable.carousel_bg_3,
                "Smart Nearby Help",
                "Alerts reach the nearest rescue centers instantly"
            )
        )

        val adapter = CarouselAdapter(carouselItems)
        viewPager.adapter = adapter

        setupIndicator(carouselItems.size)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position)
            }
        })

        /* ===== NAVIGATION INTENTS (UNCHANGED) ===== */

        cardReport.setOnClickListener {
            startActivity(Intent(this, ReportCaseActivity::class.java))
        }

        cardDonate.setOnClickListener {
            startActivity(Intent(this, DonationListActivity::class.java))
        }

        cardTrack.setOnClickListener {
            startActivity(Intent(this, OngoingCasesActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_track -> {
                    startActivity(Intent(this, OngoingCasesActivity::class.java))
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

    /* ===== LOCATION FUNCTION ===== */

    private fun getUserLocation(tv: TextView) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(
                        it.latitude,
                        it.longitude,
                        1
                    )
                    tv.text = addresses?.get(0)?.locality ?: "Your City"
                } catch (e: Exception) {
                    tv.text = "Your City"
                }
            }
        }
    }

    /* ===== PERMISSION RESULT ===== */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val tvLocation = findViewById<TextView>(R.id.tvLocation)
            getUserLocation(tvLocation)
        }
    }

    /* ===== EXISTING FUNCTIONS (UNCHANGED) ===== */

    private fun setupIndicator(count: Int) {
        val indicators = Array(count) { ImageView(this) }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in indicators.indices) {
            indicators[i].setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.indicator_dot_unselected
                )
            )
            indicators[i].layoutParams = params
            indicatorLayout.addView(indicators[i])
        }
        updateIndicator(0)
    }

    private fun updateIndicator(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == position)
                        R.drawable.indicator_dot_selected
                    else
                        R.drawable.indicator_dot_unselected
                )
            )
        }
    }
}
