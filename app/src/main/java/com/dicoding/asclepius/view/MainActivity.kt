package com.dicoding.asclepius.view

import ClassificationsHelper
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class MainActivity : AppCompatActivity() {
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var appbinding: ActivityMainBinding
    private val CODE_PERMISSION = 101
    private var picture: Uri? = null

    private val ucrop = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]
            val ucrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(5F, 5F)
                .withMaxResultSize(224, 224)
            return ucrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return intent?.let { UCrop.getOutput(it) } ?: Uri.EMPTY
        }
    }
    private val imageContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val inputUri = uri
        val outputUri = File(filesDir, "croppedImage.jpg").toUri()

        imageCrop.launch(listOf(inputUri, outputUri) as List<Uri>?)
    }
    private val imageCrop =
        registerForActivityResult(ucrop) { uri ->
            picture = uri
            appbinding.previewImageView.setImageURI(uri)
            if (picture != null) {
                appbinding.previewImageView.setImageURI(picture)
                appbinding.analyzeButton.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(appbinding.root)

        imageClassifierHelper = ImageClassifierHelper(
            threshold = 0.1f,
            maxResults = 1,
            modelName = "cancer_classification.tflite",
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    toastMessage(error)
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    results?.let {
                        imageResults(picture!!, it)
                    }
                }
            }
        )

        appbinding.galleryButton.setOnClickListener { openGallery() }
        appbinding.analyzeButton.setOnClickListener {
            picture?.let {
                pictureAnalyze(it)
            } ?: run {
                toastMessage(getString(R.string.empty_image_warning))
            }
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showGallery()
        } else {
            requestGalleryPermission()
        }
    }
    private fun imageResults(imageUri: Uri, results: List<Classifications>) {
        val pictureResult = results.map { result ->
            ClassificationsHelper(result.getCategories(), result.getHeadIndex())
        }
        val picIntent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE, imageUri.toString())
            putExtra(ResultActivity.EXTRA_RESULT, Gson().toJson(pictureResult))
        }
        startActivity(picIntent)
    }

    private fun requestGalleryPermission() {
        val gallerypermision = Manifest.permission.READ_MEDIA_IMAGES
        if (shouldShowRequestPermissionRationale(gallerypermision)) {
            Toast.makeText(this, "Gallery access is required to select an image.", Toast.LENGTH_SHORT).show()
        }
        requestPermissions(arrayOf(gallerypermision), CODE_PERMISSION)
    }
    private fun showGallery() {
        imageContent.launch("image/*")
    }
    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun pictureAnalyze(uri: Uri) {
        val bitmap = BitmapFactory.decodeFile(uri.toFile().absolutePath)

        imageClassifierHelper.classifyStaticImage(bitmap)
    }
}