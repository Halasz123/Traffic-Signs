package com.trafficsigns.ui.network.classifiers

import android.app.Activity
import com.trafficsigns.ui.constant.Network
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.ops.NormalizeOp

/** @author: Halász Botond
 * @since: 10/05/2021
 *
 * Set the parameters of Tensorflow Lite Classifier.
 * */
class ClassifierInit (activity: Activity?, device: Device?, numThreads: Int) : Classifier(activity, device, numThreads) {

    override val modelPath: String
         get() = "KagleModel100EPIL.tflite"
    override val labelPath: String
         get() = Network.CLASSIFICATION_LABELS_FILE_NAME
    override val preprocessNormalizeOp: TensorOperator
         get() = NormalizeOp(IMAGE_MEAN, IMAGE_STD)
    override val postprocessNormalizeOp: TensorOperator
         get() = NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)

    companion object {
        /**
         * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
         * bypass the normalization.
         */
        private const val IMAGE_MEAN = 0.0f
        private const val IMAGE_STD = 255.0f

        /** Quantized MobileNet requires additional dequantization to the output probability.  */
        private const val PROBABILITY_MEAN = 0.0f
        private const val PROBABILITY_STD = 1.0f
    }
}