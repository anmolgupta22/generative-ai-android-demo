package com.example.geminiapi

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.geminiapi.databinding.ActivityMainBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectPhotoBtn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            binding.image.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_placeholder_view_vector))
            binding.textView.text = ""
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data

                // Call a method to convert the Uri to Bitmap
                val bitmap = convertUriToBitmap(selectedImageUri)

                val generativeModel = GenerativeModel(
                    modelName = "gemini-pro-vision",
                    apiKey = BuildConfig.apiKey
                )
                if (bitmap != null) {
                    val cookieImage: Bitmap = bitmap
                    val inputContent = content {
                        image(cookieImage)
                        text("can you describe this image?")
                    }

                    lifecycleScope.launch {
                        val response = generativeModel.generateContent(inputContent)
                        print(response.text)
                        binding.image.setImageBitmap(bitmap)
                        binding.textView.text = response.text
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

    private fun convertUriToBitmap(uri: Uri?): Bitmap? {
        uri?.let {
            binding.progressBar.visibility = View.VISIBLE
            try {
                // Use ContentResolver to open the InputStream
                val inputStream = this.contentResolver.openInputStream(it)

                // Convert InputStream to Bitmap
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }
}