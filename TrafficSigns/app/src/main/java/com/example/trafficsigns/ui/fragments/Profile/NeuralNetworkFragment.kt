package com.example.trafficsigns.ui.fragments.Profile

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.trafficsigns.R
import com.example.trafficsigns.databinding.FragmentNeuralNetworkBinding
import com.example.trafficsigns.ml.Kaglemodel2
import com.example.trafficsigns.ui.constants.Data
import com.example.trafficsigns.ui.constants.ToastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import java.io.File
import java.io.FileReader
import java.io.InputStream


class NeuralNetworkFragment : Fragment() {

    private lateinit var binding: FragmentNeuralNetworkBinding
    private lateinit var photoFile: File
    private lateinit var imageFromFile: Bitmap
    private lateinit var resultTextView: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_neural_network,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultTextView = binding.resultTextView
        binding.photo.setOnClickListener {
            startCameraIntent()
        }

        binding.galleryPicture.setOnClickListener {
            checkPermission()
        }

        binding.button.setOnClickListener {
            iterateOnTestDirectory()
        }

        val bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.gyalogos00018)

       // Log.d("NEURAL", "Gyalogos")
       // classifier2(bitmap)
    }

    private fun startCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile("Traffic")

        val fileProvider = FileProvider.getUriForFile(
            requireContext(),
            Data.PACKAGE_FILEPROVIDER_PATH,
            photoFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } != null){
            startActivityForResult(takePictureIntent, CAPTURE_PHOTO_CODE)
        }
        else {
            Toast.makeText(requireContext(), ToastMessage.UNABLE_OPEN_CAMERA, Toast.LENGTH_LONG).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  File.createTempFile(fileName, ".jpg", storageDirectory)
    }
    private fun pickGalleryPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else {
                pickGalleryPhoto()
            }
        }
        else {
            pickGalleryPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAPTURE_PHOTO_CODE -> {
                    binding.profilePicture.setImageBitmap(BitmapFactory.decodeFile(photoFile.absolutePath))
                    val bitmap: Bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    lifecycleScope.launch {
                        val result: MutableList<Classifications>
                        withContext(Dispatchers.IO){
                            result = classifier(bitmap)
                        }
                        resultTextView.text = "${result[0].categories[0].label}  - ${result[0].categories[0].score}"
                    }
                }
                IMAGE_PICK_CODE -> {
                    binding.profilePicture.setImageURI(data?.data)
                    val pickedImage: Uri = data?.data!!
                    val `is`: InputStream? = context?.contentResolver?.openInputStream(pickedImage)
                    val bitmap = BitmapFactory.decodeStream(`is`)
                    `is`?.close()
                    lifecycleScope.launch {
                        val result: MutableList<Classifications>
                        withContext(Dispatchers.IO){
                            result = classifier(bitmap)
                        }
                        resultTextView.text = "${result[0].categories[0].label}  - ${result[0].categories[0].score}"
                    }


                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun classifier(bitmap: Bitmap): MutableList<Classifications> {
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val size = if (height > width) width else height

        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
                .add(ResizeWithCropOrPadOp(size, size))
                .add(ResizeOp(30, 30, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(127.5f, 127.5f))
                .add(QuantizeOp(128.0f, 1 / 128.0f))
                .build()

        var tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)

        val options = ImageClassifierOptions.builder().setMaxResults(10).build()
        val imageClassifier = ImageClassifier.createFromFileAndOptions(
            context,
            "KagleModel 2.tflite",
            options
        )
        val results = imageClassifier.classify(tImage)
//        Log.d("NEURAL", results.toString())
        Log.d("NEURAL-best", "${results[0].categories[0].label}  = ${results[0].categories[0].score} | ${results[0].headIndex}")
        return  results

    }

    private fun classifier2(bitmap: Bitmap): List<Category> {
        val model = Kaglemodel2.newInstance(requireContext())
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        //Log.d("NEURAL-Classifier2", outputs.toString())
        val probability = outputs.probabilityAsCategoryList.sortedByDescending { it.score }
       // Log.d("NEURAL-Classifier2", outputs.probabilityAsCategoryList.sortedByDescending { it.score }.toString())
       // Log.d("NEURAL-Classifier2", probability.sortedWith(compareBy { it.score}).reversed().toString())
        model.close()
        return probability
    }

    private fun iterateOnTestDirectory(){

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {


                val dir = "/data/data/com.example.trafficsigns/Test/"
                var reader = FileReader(dir + "Test.csv")
                val csvLines = reader.readLines()
                reader = FileReader(dir + "label43.txt")
                val labels = reader.readLines()
               // Log.d("NEURAL", labels.toString())
                var correctId = 0
                var correctId2 = 0
                var averageModel = 0.0
                var averageModel2 = 0.0
                csvLines.forEach {
                    val line = Pair(it.split(",")[0], it.split(",")[1])
                    val bitmap = BitmapFactory.decodeFile(dir + line.second)

                    val resultByModel = classifier(bitmap)
                    val resultByModel2 = classifier2(bitmap)

                    val resultClassId = labels.indexOf(resultByModel[0].categories[0].label)
                    val resultClassId2 = labels.indexOf(resultByModel2[0].label)

                    val searchedLabel = labels[line.first.toInt()]
                    val resultObject = resultByModel[0].categories.firstOrNull { it1 -> it1.label == searchedLabel }
                    if (resultObject != null){
                        averageModel += resultObject.score
                    }
                    val resultObject2 = resultByModel2.firstOrNull { it1 -> it1.label == searchedLabel }
                    if (resultObject2 != null){
                        averageModel2 += resultObject2.score
                    }
                    //averageModel += resultByModel


                    Log.d(
                        "NEURAL-TEST",
                        "ClassId: ${line.first}... Result: $resultClassId | ${resultByModel[0].categories[0].label} - ${resultByModel[0].categories[0].score} "
                    )
                    Log.d(
                        "NEURAL-TEST-2",
                        "ClassId: ${line.first}... Result: $resultClassId2 | ${resultByModel2[0].label} - ${resultByModel2[0].score} "
                    )
                    Log.d("NEURAL", resultByModel.toString())
                    Log.d("NEURAL-Classifier2", resultByModel2.toString())

                    if (resultClassId == line.first.toInt()) {
                        correctId++
                    }
                    if (resultClassId2 == line.first.toInt()) {
                        correctId2++
                    }
                }
                Log.d(
                    "NEURAL",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId  --- Atlag Megtalalasi szazalek: ${averageModel / 43}"
                )
                Log.d(
                    "NEURAL-2",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId2  --- Atlag Megtalalasi szazalek: ${averageModel2 / 43}\""
                )
            }
        }
    }

}
