package com.trafficsigns.ui.network.classifiers

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import com.trafficsigns.ui.constant.Network
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

/** @author: Halász Botond
 *  @since: 10/05/2021
 *
 * A classifier specialized to classify images with Tensorflow Lite models.
 * For an input image it can decide what it is. Depends on neural network.
 * */
abstract class Classifier protected constructor(
    activity: Activity?,
    device: Device?,
    numThreads: Int
) {

    /** The runtime device type used for executing classification.  */
    enum class Device {
        CPU, NNAPI, GPU
    }

    private val imageSizeX: Int
    private val imageSizeY: Int
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null
    private var tfLiteInterpreter: Interpreter
    private val tfLiteOptions = Interpreter.Options()
    private val labels: List<String>
    private var inputImageBuffer: TensorImage
    private val outputProbabilityBuffer: TensorBuffer
    private val probabilityProcessor: TensorProcessor

    class Recognition(
        val id: Int,
        val title: String,
        val confidence: Float,
        private var location: RectF?
    ) {

        override fun toString(): String {
            var resultString = "("
            resultString += "$title "
            resultString += String.format("(%.4f%%) ", confidence * 100.0f) + ")"
            return resultString.trim { it <= ' ' }
        }
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        Trace.beginSection("recognizeImage")
        Trace.beginSection("loadImage")
        val startTimeForLoadImage = SystemClock.uptimeMillis()
        inputImageBuffer = loadImage(bitmap)
        val endTimeForLoadImage = SystemClock.uptimeMillis()
        Trace.endSection()
        Log.v(TAG, "Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage))

        Trace.beginSection("runInference")
        val startTimeForReference = SystemClock.uptimeMillis()
        tfLiteInterpreter.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())
        val endTimeForReference = SystemClock.uptimeMillis()
        Trace.endSection()
        Log.v(
            TAG,
            "Timecost to run model inference: " + (endTimeForReference - startTimeForReference)
        )

        val labeledProbability =
            TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer)).categoryList
        Trace.endSection()
        return getTopKProbability(labeledProbability)
    }

    fun close() {
        tfLiteInterpreter.close()
        gpuDelegate?.close()
        nnApiDelegate?.close()
    }

    private fun loadImage(bitmap: Bitmap): TensorImage {
        inputImageBuffer.load(bitmap)
        val cropSize = min(bitmap.width, bitmap.height)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeMethod.NEAREST_NEIGHBOR))
            .add(preprocessNormalizeOp)
            .build()
        return imageProcessor.process(inputImageBuffer)
    }

    protected abstract val modelPath: String
    protected abstract val labelPath: String
    protected abstract val preprocessNormalizeOp: TensorOperator
    protected abstract val postprocessNormalizeOp: TensorOperator

    companion object {
        const val TAG = "ClassifierWithSupport"

        /** Number of results to show in the UI.  */
        const val MAX_RESULTS = 5
    }

    fun getTopKProbability(labelProb: List<Category>): List<Recognition> {
        val pq = PriorityQueue(MAX_RESULTS,
            Comparator<Recognition?> { o1, o2 -> o2.confidence.compareTo(o1.confidence) })

        for (i in 0..(labelProb.size-1)) {
            if (labelProb[i].score > Network.MINIM_CONFIDENCE_RESULT) {
                pq.add(Recognition(i, labelProb[i].label, labelProb[i].score, null))
            }
        }
        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            pq.poll()?.let { recognitions.add(it) }
        }
        return recognitions
    }

    /** Initializes a `Classifier`.  */
    init {
        val tfliteModel = FileUtil.loadMappedFile(activity!!, modelPath)
        when (device) {
            Device.NNAPI -> {
                nnApiDelegate = NnApiDelegate()
                tfLiteOptions.addDelegate(nnApiDelegate)
            }
            Device.GPU -> {
                gpuDelegate = GpuDelegate()
                tfLiteOptions.addDelegate(gpuDelegate)
            }
            Device.CPU -> tfLiteOptions.setUseXNNPACK(true)
        }
        tfLiteOptions.setNumThreads(numThreads)
        tfLiteInterpreter = Interpreter(tfliteModel, tfLiteOptions)
        labels = FileUtil.loadLabels(activity, labelPath)

        val imageTensorIndex = 0
        val imageShape =
            tfLiteInterpreter.getInputTensor(imageTensorIndex).shape() // {1, height, width, 3}
        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType = tfLiteInterpreter.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape =
            tfLiteInterpreter.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}
        val probabilityDataType =
            tfLiteInterpreter.getOutputTensor(probabilityTensorIndex).dataType()

        inputImageBuffer = TensorImage(imageDataType)
        outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        probabilityProcessor = TensorProcessor.Builder().add(postprocessNormalizeOp).build()
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.")
    }
}
