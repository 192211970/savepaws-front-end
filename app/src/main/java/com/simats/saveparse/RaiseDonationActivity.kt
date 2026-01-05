package com.simats.saveparse

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RaiseDonationActivity : AppCompatActivity() {

    private var centerId: Int = -1
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private lateinit var ivAnimalPhoto: ImageView
    private lateinit var layoutPhotoPlaceholder: LinearLayout
    private lateinit var btnSubmit: Button

    companion object {
        private const val TAG = "RaiseDonationActivity"
        private const val PERMISSION_CAMERA = 102
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null && photoFile!!.exists()) {
            Glide.with(this).load(photoFile!!).into(ivAnimalPhoto)
            layoutPhotoPlaceholder.visibility = View.GONE
        } else {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raise_donation)

        // Get center_id
        val sharedPref = getSharedPreferences("SavePawsPrefs", MODE_PRIVATE)
        centerId = sharedPref.getInt("center_id", -1)

        // Initialize views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etCaseId = findViewById<EditText>(R.id.etCaseId)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto)
        layoutPhotoPlaceholder = findViewById(R.id.layoutPhotoPlaceholder)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnBack.setOnClickListener { finish() }

        btnTakePhoto.setOnClickListener { takePhoto() }
        layoutPhotoPlaceholder.setOnClickListener { takePhoto() }

        btnSubmit.setOnClickListener {
            val caseId = etCaseId.text.toString().trim()
            val amount = etAmount.text.toString().trim()

            if (caseId.isEmpty()) {
                Toast.makeText(this, "Please enter case ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (photoFile == null || !photoFile!!.exists()) {
                Toast.makeText(this, "Please take a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitDonationRequest(caseId.toInt(), amount.toDouble())
        }
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
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("DONATION_${timestamp}_", ".jpg", storageDir)
    }

    private fun compressImage(file: File): File {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            val targetWidth = 400
            var sampleSize = 1
            if (options.outWidth > targetWidth) {
                sampleSize = options.outWidth / targetWidth
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: return file

            val compressedFile = File(file.parent, "compressed_${file.name}")
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out)
            }
            bitmap.recycle()

            Log.d(TAG, "Original: ${file.length() / 1024}KB, Compressed: ${compressedFile.length() / 1024}KB")
            return compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Compression failed", e)
            return file
        }
    }

    private fun submitDonationRequest(caseId: Int, amount: Double) {
        btnSubmit.isEnabled = false
        btnSubmit.text = "Compressing..."

        try {
            val compressedFile = compressImage(photoFile!!)
            btnSubmit.text = "Uploading..."

            val centerIdBody = centerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val caseIdBody = caseId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val amountBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "image_of_animal",
                compressedFile.name,
                compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            ApiClient.api.createDonationRequest(centerIdBody, caseIdBody, amountBody, photoPart)
                .enqueue(object : Callback<CaseActionResponse> {
                    override fun onResponse(
                        call: Call<CaseActionResponse>,
                        response: Response<CaseActionResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(
                                this@RaiseDonationActivity,
                                "Donation request submitted!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            btnSubmit.isEnabled = true
                            btnSubmit.text = "Submit Donation Request"
                            Toast.makeText(
                                this@RaiseDonationActivity,
                                response.body()?.message ?: "Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<CaseActionResponse>, t: Throwable) {
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Submit Donation Request"
                        Toast.makeText(
                            this@RaiseDonationActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            btnSubmit.isEnabled = true
            btnSubmit.text = "Submit Donation Request"
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
