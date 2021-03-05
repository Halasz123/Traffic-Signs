package com.example.trafficsigns

import android.R.attr.bitmap
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.trafficsigns.databinding.ActivityMainBinding
import org.checkerframework.checker.units.qual.min
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        val outputs = TensorBuffer.createFixedSize(intArrayOf(1, 1024), DataType.FLOAT32)
//
//        val options: ImageClassifier.ImageClassifierOptions =
//            ImageClassifier.ImageClassifierOptions.builder().setMaxResults(3).build()
//        val imageClassifier = ImageClassifier.createFromFileAndOptions(
//            this,
//            "src/main/ml/second.tflite", options
//        )
//
//        val inputImage = TensorImage.fromBitmap(bitmap)
//        val width: Int = bitmap.getWidth()
//        val height: Int = bitmap.getHeight()
//        val cropSize: Int = min(width, height)
//        val imageOptions = ImageProcessingOptions.builder()
//            .setOrientation(getOrientation(sensorOrientation)) // Set the ROI to the center of the image.
//            .setRoi(
//                Rect( /*left=*/
//                    (width - cropSize) / 2,  /*top=*/
//                    (height - cropSize) / 2,  /*right=*/
//                    (width + cropSize) / 2,  /*bottom=*/
//                    (height + cropSize) / 2
//                )
//            )
//            .build()
//
//        val results = imageClassifier.classify(
//            inputImage,
//            imageOptions
//        )

    }

}
