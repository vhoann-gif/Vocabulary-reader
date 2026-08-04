package com.tomoe.wordscanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tomoe.wordscanner.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, R.string.camera_permission_needed, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)
        binding.scanButton.setOnClickListener { scanAndRead() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (error: Exception) {
                Toast.makeText(this, "Không mở được camera: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun scanAndRead() {
        val previewBitmap = binding.previewView.bitmap
        if (previewBitmap == null || binding.scanOverlay.scanRect.isEmpty) {
            Toast.makeText(this, "Camera chưa sẵn sàng.", Toast.LENGTH_SHORT).show()
            return
        }

        setScanningState(true)

        val cropped = cropToScanFrame(previewBitmap)
        val image = InputImage.fromBitmap(cropped, 0)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val word = chooseBestWord(result, cropped.width, cropped.height)
                if (word == null) {
                    binding.resultText.visibility = View.VISIBLE
                    binding.resultText.setText(R.string.no_word_found)
                } else {
                    binding.resultText.visibility = View.VISIBLE
                    binding.resultText.text = word
                    speak(word)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Lỗi nhận diện: ${error.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                cropped.recycle()
                setScanningState(false)
            }
    }

    private fun cropToScanFrame(bitmap: Bitmap): Bitmap {
        val overlay = binding.scanOverlay
        val rect = overlay.scanRect

        val scaleX = bitmap.width.toFloat() / overlay.width.toFloat()
        val scaleY = bitmap.height.toFloat() / overlay.height.toFloat()

        val left = max(0, (rect.left * scaleX).toInt())
        val top = max(0, (rect.top * scaleY).toInt())
        val right = min(bitmap.width, (rect.right * scaleX).toInt())
        val bottom = min(bitmap.height, (rect.bottom * scaleY).toInt())

        return Bitmap.createBitmap(bitmap, left, top, max(1, right - left), max(1, bottom - top))
    }

    private fun chooseBestWord(result: Text, imageWidth: Int, imageHeight: Int): String? {
        val centerX = imageWidth / 2f
        val centerY = imageHeight / 2f

        return result.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .mapNotNull { element ->
                val box = element.boundingBox ?: return@mapNotNull null
                val cleaned = element.text
                    .trim()
                    .replace(Regex("[^A-Za-z'-]"), "")
                if (cleaned.isBlank()) return@mapNotNull null

                val dx = box.exactCenterX() - centerX
                val dy = box.exactCenterY() - centerY
                val distanceSquared = dx * dx + dy * dy
                cleaned to distanceSquared
            }
            .minByOrNull { it.second }
            ?.first
    }

    private fun speak(text: String) {
        if (!ttsReady) {
            Toast.makeText(this, R.string.tts_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word-${System.currentTimeMillis()}")
    }

    private fun setScanningState(scanning: Boolean) {
        binding.scanButton.isEnabled = !scanning
        binding.scanButton.setText(if (scanning) R.string.scanning else R.string.scan_and_read)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            textToSpeech?.setSpeechRate(0.82f)
        }
    }

    override fun onDestroy() {
        recognizer.close()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}
