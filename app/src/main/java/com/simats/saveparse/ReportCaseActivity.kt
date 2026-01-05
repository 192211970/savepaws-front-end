package com.simats.saveparse

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ReportCaseActivity : AppCompatActivity() {

    private lateinit var imageFile: File
    private lateinit var imageUri: Uri
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var imgPreview: ImageView
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var tvLocationStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSubmit: Button

    private var latitude = ""
    private var longitude = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_case)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val cardUpload = findViewById<CardView>(R.id.cardUpload)
        btnSubmit = findViewById(R.id.btnSubmit)
        val etAnimalType = findViewById<EditText>(R.id.etAnimalType)
        val spinner = findViewById<Spinner>(R.id.spCondition)

        imgPreview = findViewById(R.id.imgPreview)
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        progressBar = findViewById(R.id.progressBar)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Injured", "Critical", "Normal")
        )

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    // Compress image after capture
                    compressImage(imageFile)
                    imgPreview.setImageURI(null) // Clear cache
                    imgPreview.setImageURI(imageUri)
                    imgPreview.visibility = View.VISIBLE
                    uploadPlaceholder.visibility = View.GONE
                    tvLocationStatus.text = "Detecting location..."
                    fetchCurrentLocation()
                }
            }

        cardUpload.setOnClickListener { openCamera() }

        btnSubmit.setOnClickListener {
            if (!::imageFile.isInitialized) {
                toast("Upload photo first")
                return@setOnClickListener
            }

            if (latitude.isEmpty()) {
                toast("Location not detected yet")
                return@setOnClickListener
            }

            uploadCase(
                etAnimalType.text.toString().trim(),
                spinner.selectedItem.toString()
            )
        }

        // --- NAVIGATION LOGIC ---

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, UserDashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, UserDashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navTrack).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun openCamera() {
        imageFile = File(cacheDir, "case_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", imageFile)
        cameraLauncher.launch(imageUri)
    }

    /**
     * Compress image to reduce upload time
     * Reduces to max 800x800 resolution with 70% JPEG quality
     * Typically reduces 3-5MB images to 200-500KB
     */
    private fun compressImage(file: File) {
        try {
            // Decode with inSampleSize to reduce memory usage
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate inSampleSize
            val maxSize = 800
            var inSampleSize = 1
            if (options.outHeight > maxSize || options.outWidth > maxSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize) {
                    inSampleSize *= 2
                }
            }

            // Decode with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)

            // Scale to exact size if needed
            val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // Save compressed image
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            }

            // Recycle bitmaps
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            Log.d("ReportCaseActivity", "Image compressed to ${file.length() / 1024} KB")
        } catch (e: Exception) {
            Log.e("ReportCaseActivity", "Image compression failed", e)
        }
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tvLocationStatus.text = "Location permission required"
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
                tvLocationStatus.text = "Location detected ✓"
            } else {
                tvLocationStatus.text = "Turn ON GPS"
            }
        }
    }

    private fun uploadCase(type: String, condition: String) {

        // Get actual user ID from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        
        if (userId == -1) {
            toast("Please login again")
            return
        }

        // Show progress and disable button
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false
        btnSubmit.text = "Uploading..."

        val photoPart = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        ApiClient.api.reportCase(
            photoPart,
            userId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            type.toRequestBody("text/plain".toMediaTypeOrNull()),
            condition.toRequestBody("text/plain".toMediaTypeOrNull()),
            latitude.toRequestBody("text/plain".toMediaTypeOrNull()),
            longitude.toRequestBody("text/plain".toMediaTypeOrNull())
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                 if (response.isSuccessful) {
                    startActivity(
                        Intent(
                            this@ReportCaseActivity,
                            CaseRegisteredSuccessActivity::class.java
                        )
                    )
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("ReportCaseActivity", "Upload failed: $errorBody")
                    toast("Error: ${response.code()} - $errorBody")
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Report"
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ReportCaseActivity", "Network failure", t)
                toast("Upload failed: ${t.message}")
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Report"
            }
        })
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

