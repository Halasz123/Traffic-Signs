package com.trafficsigns.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trafficsigns.R
import com.trafficsigns.databinding.FragmentCameraNeuralBinding
import com.trafficsigns.ui.adapter.NetworkResult
import com.trafficsigns.ui.singleton.TrafficSignMemoryCache
import com.trafficsigns.ui.network.AutoFitTextureView
import com.trafficsigns.ui.network.classifiers.ImageClassifier
import com.trafficsigns.ui.constant.Network
import com.trafficsigns.data.dataclass.TrafficHistory
import com.trafficsigns.ui.network.classifiers.Classifier
import com.trafficsigns.ui.network.classifiers.ClassifierInit
import java.io.IOException
import java.lang.Long.signum
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Open Camera2 and it classifies every frame with an ImageClassifier object
 * and shows the result.
 * Capture one frame by button press and see the more detailed result on Dialog.
 */

class CameraNeuralFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = "TfLiteCameraTraffic"
    private val HANDLE_THREAD_NAME = "CameraBackground"
    private val PERMISSIONS_REQUEST_CODE = 1

    private val lock = Any()
    private var runClassifier = false
    private var checkedPermissions = false
    private lateinit var resultTextView: TextView
    private lateinit var classifier: ImageClassifier
    private lateinit var classifierForDialog: ClassifierInit
    private var listOfTrafficSignHistory: ArrayList<TrafficHistory> = ArrayList()
    private val classifierCacheInstance = TrafficSignMemoryCache.instance

    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a [ ].
     */
    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    private lateinit var cameraId: String
    private lateinit var textureView: AutoFitTextureView
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size

    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.  */
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull currentCameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = currentCameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(@NonNull currentCameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            currentCameraDevice.close()
        }

        override fun onError(@NonNull currentCameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            currentCameraDevice.close()
            activity?.finish()
        }
    }

    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private var imageReader: ImageReader? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private val cameraOpenCloseLock = Semaphore(1)

    /** A [CameraCaptureSession.CaptureCallback] that handles events related to capture.  */
    private val captureCallback: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureProgressed(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest,
            @NonNull partialResult: CaptureResult
        ) {
        }

        override fun onCaptureCompleted(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest,
            @NonNull result: TotalCaptureResult
        ) {
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showToast(labelId: String, confidence: Float) {
        activity?.runOnUiThread {
            if (confidence != 0f) {
                val elem = TrafficSignMemoryCache.instance.getCachedTrafficSign(labelId)
                binding.predictedTextView.text = "${elem?.name} : ${confidence * 100}%"
                context?.let {
                    Glide
                        .with(it)
                        .load(elem?.image)
                        .override(binding.trafficImage.width, binding.trafficImage.height)
                        .placeholder(R.drawable.ic_stop_splash)
                        .into(binding.trafficImage)
                }
            } else {
                binding.predictedTextView.text = labelId
            }
        }

    }

    /**
     * Resizes image.
     *
     * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
     * resulting in gorgeous previews but the storage of garbage capture data.
     *
     * Given `choices` of `Size`s supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size, and
     * whose aspect ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth The maximum width that can be chosen
     * @param maxHeight The maximum height that can be chosen
     * @param aspectRatio The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size? {
        val bigEnough: MutableList<Size> = ArrayList()
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }
        return when {
            bigEnough.size > 0 -> {
                Collections.min(bigEnough, CompareSizesByArea())
            }
            notBigEnough.size > 0 -> {
                Collections.max(notBigEnough, CompareSizesByArea())
            }
            else -> {
                Log.e(TAG, "Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }

    companion object {
        private val ORIENTATIONS = SparseIntArray()

        fun addOrientations() {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private lateinit var binding: FragmentCameraNeuralBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_camera_neural,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textureView = binding.textureView
        resultTextView = binding.predictedTextView
        addOrientations()
        classifierForDialog = ClassifierInit(requireActivity(), Classifier.Device.CPU, 4)

        binding.capturePhoto.setOnClickListener {
            val bitmap: Bitmap? = textureView.getBitmap(
                binding.textureView.width,
                binding.textureView.height
            )
            val list = processImage(bitmap)
            showDialog(bitmap, list)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            classifier =
                ImageClassifier(activity)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to initialize an image classifier.")
        }
        startBackgroundThread()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        classifier.close()
        classifierForDialog.close()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun processImage(bitmap: Bitmap?): List<Classifier.Recognition> {
        return bitmap?.let { classifierForDialog.recognizeImage(it) }!!
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue
                val largest = Collections.max(
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2)
                val displayRotation = activity?.display?.rotation
                val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                    else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
                }
                val displaySize = Point()
                activity?.display?.getRealSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth,
                    rotatedPreviewHeight,
                    maxPreviewWidth,
                    maxPreviewHeight,
                    largest
                )!!
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }
                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            Toast.makeText(requireContext(), "Error setup Camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        val activity: Activity? = activity
        return try {
            val info = activity?.packageManager?.getPackageInfo(
                activity.packageName,
                PackageManager.GET_PERMISSIONS
            )
            val ps = info?.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun openCamera(width: Int, height: Int) {
        checkedPermissions = if (!checkedPermissions && !allPermissionsGranted()) {
            requestPermissions(
                requireActivity(),
                getRequiredPermissions(),
                PERMISSIONS_REQUEST_CODE
            )
            return
        } else {
            true
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            cameraId.let {
                if (context?.let { it1 ->
                        ActivityCompat.checkSelfPermission(it1, Manifest.permission.CAMERA)
                    } != PackageManager.PERMISSION_GRANTED) {
                    activity?.let { activity ->
                        requestPermissions(activity, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE)}
                    return
                }
                manager.openCamera(it, stateCallback, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun showDialog(bitmap: Bitmap?, list: List<Classifier.Recognition>) {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.custom_dialog_layout)
        val imageView = dialog?.findViewById(R.id.actual_frame) as ImageView
        imageView.setImageBitmap(bitmap)
        val recyclerView = dialog.findViewById(R.id.dialog_result_recyclerview) as RecyclerView
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = view?.let { NetworkResult(list, it, dialog) }
            adapter?.notifyDataSetChanged()
        }
        val okBtn = dialog.findViewById(R.id.dismiss_button) as Button
        okBtn.setOnClickListener {
            dialog.dismiss()
            bitmap?.recycle()
        }
        dialog.show()

    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (permission?.let { ContextCompat.checkSelfPermission(requireContext(), it) } != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            cameraDevice?.close()
            imageReader?.close()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
        synchronized(lock) { runClassifier = true }
        backgroundHandler.post(periodicClassify)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
            synchronized(lock) { runClassifier = false }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /** Takes photos and classify them periodically.  */
    private val periodicClassify: Runnable = object : Runnable {
        override fun run() {
            synchronized(lock) {
                if (runClassifier) {
                    classifyFrame()
                }
            }
            backgroundHandler.post(this)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture: SurfaceTexture? = textureView.surfaceTexture
            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(texture)
            previewRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        if (null == cameraDevice) {
                            return
                        }
                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        showToast("Failed", 0f)
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`. This
     * method should be called after the camera preview size is determined in setUpCameraOutputs and
     * also the size of `textureView` is fixed.
     *
     * @param viewWidth The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = activity?.display?.rotation
        val matrix = Matrix()
        val viewRect = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0F, 0F, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(viewHeight.toFloat() / previewSize.height, viewWidth.toFloat() / previewSize.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /** Classifies a frame from the preview stream.  */
    private fun classifyFrame() {
        if (activity == null || cameraDevice == null) {
            showToast("Uninitialized Classifier or invalid context.", 0f)
            return
        }
        val bitmap: Bitmap? = textureView.getBitmap(
            ImageClassifier.DIM_IMG_SIZE_X,
            ImageClassifier.DIM_IMG_SIZE_Y
        )
        val result = classifier.classifyFrame(bitmap)
        bitmap?.recycle()
        val (labelId, value) = result.split("|")
        if (value.toFloat() > Network.MINIM_CONFIDENCE_DISPLAY) {
            showToast(labelId, value.toFloat())
            manageLastSigns(labelId, value.toFloat())
        }
    }

    private fun manageLastSigns(labelId: String, value: Float) {
        val elem = classifierCacheInstance.getCachedTrafficSign(labelId)
        val now = System.currentTimeMillis() / 1000
        if (listOfTrafficSignHistory.count() == 0) {
            listOfTrafficSignHistory.add(TrafficHistory(labelId, now, value))
        } else if (elem != null && (now - listOfTrafficSignHistory[0].timeStamp) >= 2) {
            listOfTrafficSignHistory.add(0, TrafficHistory(labelId, now, value))
        }

        val imageViews = listOf(binding.lElem0, binding.lElem1, binding.lElem2, binding.lElem3)
        val activity = requireActivity()
        var i = 0
        activity.runOnUiThread {
            context?.let {
                while (i < 4 && i < listOfTrafficSignHistory.count()) {
                    Glide
                        .with(it)
                        .load(classifierCacheInstance.getCachedTrafficSign(listOfTrafficSignHistory[i].id)?.image)
                        .override(imageViews[i].width, imageViews[i].height)
                        .placeholder(R.drawable.ic_stop_splash)
                        .into(imageViews[i])
                    i += 1
                }
            }
        }
    }

    private class CompareSizesByArea : Comparator<Size?> {
        override fun compare(lhs: Size?, rhs: Size?): Int {
            return if (lhs != null && rhs != null) {
                signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
            } else {
                -1
            }
        }
    }
}
