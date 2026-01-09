package com.simats.saveparse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UpdateRescueActivity : AppCompatActivity() {

    private var caseId: Int = -1
    private var centerId: Int = -1
    private var reached: Boolean = false
    private var spotted: Boolean = false
    private var rescued: Boolean = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private lateinit var ivReachedCheck: ImageView
    private lateinit var ivSpottedCheck: ImageView
    private lateinit var ivRescuedCheck: ImageView
    private lateinit var btnReached: Button
    private lateinit var btnSpotted: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnCloseCase: Button
    private lateinit var ivRescuePhoto: ImageView

    companion object {
        private const val TAG = "UpdateRescueActivity"
        private const val PERMISSION_CAMERA = 102
    }

    // Modern camera result handler
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null && photoFile!!.exists()) {
            Log.d(TAG, "Photo saved: ${photoFile!!.absolutePath}")
            Glide.with(this).load(photoFile!!).into(ivRescuePhoto)
            ivRescuePhoto.visibility = View.VISIBLE
            btnCloseCase.visibility = View.VISIBLE
        } else {
            Log.e(TAG, "Photo capture failed or file doesn't exist")
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_rescue)

        // Get data from intent
        caseId = intent.getIntExtra("case_id", -1)
        reached = intent.getBooleanExtra("reached", false)
        spotted = intent.getBooleanExtra("spotted", false)
        rescued = intent.getBooleanExtra("rescued", false)
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        val photo = intent.getStringExtra("photo")
        val animalType = intent.getStringExtra("animal_type")

        // Get center_id from SharedPreferences
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Initialize views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val ivAnimalPhoto = findViewById<ImageView>(R.id.ivAnimalPhoto)
        val tvAnimalType = findViewById<TextView>(R.id.tvAnimalType)
        val tvCaseId = findViewById<TextView>(R.id.tvCaseId)
        val tvCoordinates = findViewById<TextView>(R.id.tvCoordinates)
        val btnNavigate = findViewById<Button>(R.id.btnNavigate)

        ivReachedCheck = findViewById(R.id.ivReachedCheck)
        ivSpottedCheck = findViewById(R.id.ivSpottedCheck)
        ivRescuedCheck = findViewById(R.id.ivRescuedCheck)
        btnReached = findViewById(R.id.btnReached)
        btnSpotted = findViewById(R.id.btnSpotted)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnCloseCase = findViewById(R.id.btnCloseCase)
        ivRescuePhoto = findViewById(R.id.ivRescuePhoto)

        btnBack.setOnClickListener { finish() }

        // Set case info
        tvCaseId.text = "Case #$caseId"
        tvAnimalType.text = animalType ?: "Animal"
        tvCoordinates.text = String.format("%.6f, %.6f", latitude, longitude)

        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(ApiClient.IMAGE_BASE_URL + "uploads/" + photo)
                .into(ivAnimalPhoto)
        }

        // Navigation button - opens turn-by-turn navigation
        btnNavigate.setOnClickListener {
            val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback to browser
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }

        // Update UI based on current progress
        updateProgressUI()

        // Button listeners
        btnReached.setOnClickListener { markReached() }
        btnSpotted.setOnClickListener { markSpotted() }
        btnTakePhoto.setOnClickListener { takePhoto() }
        btnCloseCase.setOnClickListener { closeCase() }
    }

    private fun updateProgressUI() {
        val greenColor = ContextCompat.getColor(this, R.color.green_primary)
        val grayColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val whiteColor = Color.WHITE

        // Reached
        if (reached) {
            ivReachedCheck.setColorFilter(greenColor)
            btnReached.text = "Done"
            btnReached.isEnabled = false
            btnReached.setBackgroundColor(whiteColor)
            btnReached.setTextColor(greenColor)
        }

        // Spotted (only enabled if reached)
        if (spotted) {
            ivSpottedCheck.setColorFilter(greenColor)
            btnSpotted.text = "Done"
            btnSpotted.isEnabled = false
            btnSpotted.setBackgroundColor(whiteColor)
            btnSpotted.setTextColor(greenColor)
        } else {
            btnSpotted.isEnabled = reached
            if (!reached) {
                btnSpotted.setBackgroundColor(grayColor)
            }
        }

        // Rescued (only enabled if spotted)
        if (rescued) {
            ivRescuedCheck.setColorFilter(greenColor)
            btnTakePhoto.visibility = View.GONE
            btnCloseCase.visibility = View.GONE
        } else {
            btnTakePhoto.isEnabled = spotted
            if (!spotted) {
                btnTakePhoto.setBackgroundColor(grayColor)
            }
        }
    }

    private fun markReached() {
        btnReached.isEnabled = false
        btnReached.text = "Marking..."

        ApiClient.api.markReachedLocation(caseId, centerId)
            .enqueue(object : Callback<CaseActionResponse> {
                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        reached = true
                        updateProgressUI()
                        Toast.makeText(
                            this@UpdateRescueActivity,
                            "Marked as reached",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        btnReached.isEnabled = true
                        btnReached.text = "Mark"
                        Toast.makeText(
                            this@UpdateRescueActivity,
                            response.body()?.message ?: "Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    btnReached.isEnabled = true
                    btnReached.text = "Mark"
                    Toast.makeText(
                        this@UpdateRescueActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun markSpotted() {
        btnSpotted.isEnabled = false
        btnSpotted.text = "Marking..."

        ApiClient.api.markSpottedAnimal(caseId, centerId)
            .enqueue(object : Callback<CaseActionResponse> {
                override fun onResponse(
                    call: Call<CaseActionResponse>,
                    response: Response<CaseActionResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        spotted = true
                        updateProgressUI()
                        Toast.makeText(
                            this@UpdateRescueActivity,
                            "Marked as spotted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        btnSpotted.isEnabled = true
                        btnSpotted.text = "Mark"
                        Toast.makeText(
                            this@UpdateRescueActivity,
                            response.body()?.message ?: "Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                    btnSpotted.isEnabled = true
                    btnSpotted.text = "Mark"
                    Toast.makeText(
                        this@UpdateRescueActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA
            )
            return
        }

        try {
            photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile!!
            )
            Log.d(TAG, "Photo URI: $photoUri")
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image file", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("RESCUE_${timestamp}_", ".jpg", storageDir)
    }

    private fun closeCase() {
        if (photoFile == null || !photoFile!!.exists()) {
            Toast.makeText(this, "Please take a rescue photo first", Toast.LENGTH_SHORT).show()
            return
        }

        btnCloseCase.isEnabled = false
        btnCloseCase.text = "Closing..."

        try {
            val caseIdBody = caseId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val centerIdBody = centerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "rescue_photo",
                photoFile!!.name,
                photoFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            )

            ApiClient.api.closeCase(caseIdBody, centerIdBody, photoPart)
                .enqueue(object : Callback<CaseActionResponse> {
                    override fun onResponse(
                        call: Call<CaseActionResponse>,
                        response: Response<CaseActionResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(
                                this@UpdateRescueActivity,
                                "Case closed successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            btnCloseCase.isEnabled = true
                            btnCloseCase.text = "✓  Close Case"
                            Toast.makeText(
                                this@UpdateRescueActivity,
                                response.body()?.message ?: "Failed to close case",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                        Log.e(TAG, "Close case failed", t)
                        btnCloseCase.isEnabled = true
                        btnCloseCase.text = "✓  Close Case"
                        Toast.makeText(
                            this@UpdateRescueActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error closing case", e)
            btnCloseCase.isEnabled = true
            btnCloseCase.text = "✓  Close Case"
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        }
    }
}
