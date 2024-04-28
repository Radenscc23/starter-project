package com.dicoding.asclepius.view
import ClassificationsHelper
import com.google.gson.Gson
import com.dicoding.asclepius.databinding.ActivityResultBinding
import android.util.Log
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ResultActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
    private lateinit var appBinding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(appBinding.root)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE))
        val resultsJson = intent.getStringExtra(EXTRA_RESULT)
        val results = Gson().fromJson(resultsJson, Array<ClassificationsHelper>::class.java)

        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            appBinding.resultImage.setImageURI(it)
        }

        presentResults(results)
    }
    private fun presentResults(results: Array<ClassificationsHelper>) {
        val imageResults = StringBuilder()
        for (imageClassification in results) {
            imageResults.append(imageClassification.toString())
        }
        appBinding.resultText.text = imageResults.toString()
    }
}