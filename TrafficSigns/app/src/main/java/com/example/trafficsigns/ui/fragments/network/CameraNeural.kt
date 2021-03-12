package com.example.trafficsigns.ui.fragments.network

import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.trafficsigns.databinding.FragmentCameraNeuralBinding
import java.io.ByteArrayOutputStream


class CameraNeural : Fragment() {

    private lateinit var binding: FragmentCameraNeuralBinding
    private var lensFacing = CameraX.LensFacing.BACK
    private val TAG = "MainActivity"

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA")

    private lateinit var tfLiteClassifier: TFLiteClassifier

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            com.example.trafficsigns.R.layout.fragment_camera_neural,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tfLiteClassifier = TFLiteClassifier(requireContext())
        if (allPermissionsGranted()) {
            binding.textureView.post { startCamera() }
            binding.textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateTransform()
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        tfLiteClassifier
            .initialize()
            .addOnSuccessListener { }
            .addOnFailureListener { e -> Log.e(TAG, "Error in setting up the classifier.", e) }
    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also { binding.textureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(1, 1)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(requireActivity().windowManager.defaultDisplay.rotation)
            setTargetRotation(binding.textureView.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            binding.textureView.setSurfaceTexture(it.surfaceTexture)
            updateTransform()
        }


        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread("AnalysisThread").apply {
                start()
            }
            setCallbackHandler(Handler(analyzerThread.looper))
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()


        val analyzerUseCase = ImageAnalysis(analyzerConfig)
        analyzerUseCase.analyzer =
            ImageAnalysis.Analyzer { image: ImageProxy, rotationDegrees: Int ->

                val bitmap = image.toBitmap()

                tfLiteClassifier
                    .classifyAsync(bitmap)
                    .addOnSuccessListener { resultText -> binding.predictedTextView.text = resultText }
                    .addOnFailureListener { error ->  }

            }
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }

    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = binding.textureView.width / 2f
        val centerY = binding.textureView.height / 2f

        val rotationDegrees = when (binding.textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        binding.textureView.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                //finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {

        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        tfLiteClassifier.close()
        super.onDestroy()
    }

}