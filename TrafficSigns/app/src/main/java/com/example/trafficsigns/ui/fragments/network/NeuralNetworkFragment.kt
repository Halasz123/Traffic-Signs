package com.example.trafficsigns.ui.fragments.network

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.databinding.FragmentNeuralNetworkBinding
import com.example.trafficsigns.ml.*
import com.example.trafficsigns.ui.adapters.NetworkResult
import com.example.trafficsigns.ui.constants.Data
import com.example.trafficsigns.ui.constants.ToastMessage
import com.example.trafficsigns.ui.fragments.network.tflite.*
import com.example.trafficsigns.ui.fragments.profile.CAPTURE_PHOTO_CODE
import com.example.trafficsigns.ui.fragments.profile.IMAGE_PICK_CODE
import com.example.trafficsigns.ui.fragments.profile.PERMISSION_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import com.example.trafficsigns.ui.fragments.network.live.ImageClassifier as MyClassifier

class NeuralNetworkFragment : Fragment() {

    private lateinit var binding: FragmentNeuralNetworkBinding
    private lateinit var photoFile: File
    private lateinit var imageFromFile: Bitmap
    private lateinit var resultTextView: TextView
    private lateinit var gtsrbClassifier: GtsrbClassifier
    private lateinit var tfLiteClassifier: TFLiteClassifier
    private var myClassifier: MyClassifier? = null
    private lateinit var networkResultRecyclerView: RecyclerView
    private lateinit var networkResultAdapter: NetworkResult
    private lateinit var classifier: Classifier

    /** Input image size of the model along x axis.  */
    private var imageSizeX = 0

    /** Input image size of the model along y axis.  */
    private var imageSizeY = 0


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
        networkResultRecyclerView = binding.networkResultRecyclrerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        val bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.gyalogos00018)

        binding.photo.setOnClickListener {
            startCameraIntent()
        }

        binding.galleryPicture.setOnClickListener {
            checkPermission()
        }

        binding.button.setOnClickListener {
               // iterateOnTestDirectory()
             iterateOnTestMicroDirectory()
            //   iterateOnTestMicroDirectoryWithprocessImage()

             tfLiteClassifier
                        .classifyAsync(bitmap)
                        .addOnSuccessListener { resultText -> Log.d("Neural-TFliteClass", resultText) }
                        .addOnFailureListener {   }


        }
        //init ImageClassifier
        myClassifier= MyClassifier(requireActivity())
        // createClassifier(getModel())
        classifier = ClassifierQuantizedMobileNet(requireActivity(), Classifier.Device.CPU, 8)
        tfLiteClassifier = TFLiteClassifier(requireContext())

         tfLiteClassifier
                        .initialize()
                        .addOnSuccessListener { }
                        .addOnFailureListener { error ->Log.d("TFLiteClassifier", error.toString())  }


        // loadGtsrbClassifier()


      //  processImage(bitmap)

        lifecycleScope.launch {
            val result: Classifications
            withContext(Dispatchers.IO) {
                result = classifier(bitmap)
            }
            networkResultAdapter = NetworkResult(result)
            networkResultRecyclerView.adapter = networkResultAdapter
            networkResultRecyclerView.adapter?.notifyDataSetChanged()
        }


        // Log.d("NEURAL", "Gyalogos")
        // classifier2(bitmap)
    }

    private fun processImage(bitmap: Bitmap): List<Classifier.Recognition> {
        val list = classifier.recognizeImage(bitmap, 0)
        return list
        /*
        lifecycleScope.launch {
            var list: List<Classifier.Recognition>
            withContext(Dispatchers.IO) {
                list = classifier.recognizeImage(bitmap, 0)
            }
            Log.d("Neural-Lib-Support", list.toString())
        }

         */
    }
    private fun iterateOnTestMicroDirectoryWithprocessImage()
    {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dir = "/data/data/com.example.trafficsigns/TestMicro/"
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
                    val line = Pair(it.split(",")[6], it.split(",")[7])
                    val bitmap = BitmapFactory.decodeFile(dir + line.second)

                    val resultByModel = processImage(bitmap)

                    val resultClassId = labels.indexOf(resultByModel[0].title)

                    val searchedLabel = labels[line.first.toInt()]
                    val resultObject =
                        resultByModel.firstOrNull { it1 -> it1.title == searchedLabel }
                    if (resultObject != null) {
                        averageModel += resultObject.confidence
                    }

                    Log.d(
                        "NEURAL-TEST",
                        "ClassId: ${line.first}... Result: $resultClassId | ${resultByModel[0]} "
                    )

                    Log.d("NEURAL", resultByModel.toString())

                    if (resultClassId == line.first.toInt()) {
                        correctId++
                    }
                }
                Log.d(
                    "NEURAL",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId  --- Atlag Megtalalasi szazalek: ${averageModel / 201}"
                )
            }
        }
    }

    override fun onDestroy() {
        myClassifier?.close()
        super.onDestroy()
    }

    private fun loadGtsrbClassifier() {
        try {
            gtsrbClassifier =
                GtsrbClassifier.classifier(
                    requireActivity().assets,
                    GtsrbModelConfig.MODEL_FILENAME
                )
        } catch (e: IOException) {
            Toast.makeText(
                requireContext(),
                "GTSRB model couldn't be loaded. Check logs for details.",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
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
        if (activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } != null) {
            startActivityForResult(takePictureIntent, CAPTURE_PHOTO_CODE)
        } else {
            Toast.makeText(requireContext(), ToastMessage.UNABLE_OPEN_CAMERA, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    private fun pickGalleryPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                pickGalleryPhoto()
            }
        } else {
            pickGalleryPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAPTURE_PHOTO_CODE -> {
                    binding.profilePicture.setImageBitmap(BitmapFactory.decodeFile(photoFile.absolutePath))
                    val bitmap: Bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val result = processImage(bitmap)
                  //  val resultImageClassifier = classifiImage(bitmap)
                    Log.d("Neural-Lib", result.toString())

                    // tfLiteClassifier
                    //mobilnetet hasznal/ Nem megy a legjobban
//                        .classifyAsync(bitmap)
//                        .addOnSuccessListener { resultText -> Log.d("Neural-TFliteClass", resultText) }
//                        .addOnFailureListener { error ->  }

//                    classifyMobilnet(bitmap)
                    lifecycleScope.launch {
                        val result: Classifications
                        withContext(Dispatchers.IO) {
                            result = classifier(bitmap)
                        }
                        networkResultAdapter = NetworkResult(result)
                        networkResultRecyclerView.adapter = networkResultAdapter
                        networkResultRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                IMAGE_PICK_CODE -> {
                    binding.profilePicture.setImageURI(data?.data)
                    val pickedImage: Uri = data?.data!!
                    val `is`: InputStream? = context?.contentResolver?.openInputStream(pickedImage)
                    val bitmap = BitmapFactory.decodeStream(`is`)
                    `is`?.close()
                    lifecycleScope.launch {
                        val result: Classifications
                        withContext(Dispatchers.IO) {
                            result = classifier(bitmap)
                        }
                        networkResultAdapter = NetworkResult(result)
                        networkResultRecyclerView.adapter = networkResultAdapter
                        networkResultRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

//    private fun classifiImage(bitmap: Bitmap): Any {
//
//
//    }

    private fun classifier(bitmap: Bitmap): Classifications {
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val size = if (height > width) width else height

        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
            //.add(ResizeWithCropOrPadOp(size, size))
            // .add(ResizeOp(30, 30, ResizeOp.ResizeMethod.BILINEAR))
           // .add(NormalizeOp(127.5f, 127.5f))
           // .add(QuantizeOp(0.0f, 1.0f))
            .build()

        var tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)

        val options = ImageClassifierOptions.builder().setMaxResults(10).build()
        val imageClassifier = ImageClassifier.createFromFileAndOptions(
            context,
            "FirstModel200E_clahe_3232.tflite",
            options
        )
        val results = imageClassifier.classify(tImage)
        return results[0]

    }


    private fun classifier2(bitmap: Bitmap): List<Category> {
        val model = Firstmodel200eClahe3232.newInstance(requireContext())
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList.sortedByDescending { it.score }
        model.close()
        return probability
    }

    private fun classifyMobilnet(bitmap: Bitmap) {
        val squareBitmap = ThumbnailUtils.extractThumbnail(
            bitmap,
            getScreenWidth(),
            getScreenWidth()
        )
        val preprocessedImage = ImageUtils.prepareImageForClassification(squareBitmap)

        val recognitions: List<Classification> = gtsrbClassifier.recognizeImage(preprocessedImage)
        Log.d("Neural-Mobilnet", recognitions.toString())
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun iterateOnTestDirectory() {
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

                    val resultClassId = labels.indexOf(resultByModel.categories[0].label)
                    val resultClassId2 = labels.indexOf(resultByModel2[0].label)

                    val searchedLabel = labels[line.first.toInt()]
                    val resultObject = resultByModel.categories.firstOrNull { it1 -> it1.label == searchedLabel }

                    Log.d(
                        "NEURAL-TEST",
                        "ClassId: ${line.first}... Result: $resultClassId | ${resultByModel.categories[0].label} - ${resultByModel.categories[0].score} "
                    )

                    if (resultObject != null) {
                        averageModel += resultObject.score
                        Log.d("NeuralClassifier_Score", resultObject.score.toString())
                    }
                    val resultObject2 =
                        resultByModel2.firstOrNull { it1 -> it1.label == searchedLabel }

                    Log.d(
                        "NEURAL-TEST-2",
                        "ClassId: ${line.first}... Result: $resultClassId2 | ${resultByModel2[0].label} - ${resultByModel2[0].score} "
                    )
                    if (resultObject2 != null) {
                        averageModel2 += resultObject2.score
                        Log.d("NeuralClassifier2_Score", resultObject2.score.toString())
                    }
//                    Log.d("NEURAL", resultByModel.toString())
//                    Log.d("NEURAL-Classifier2", resultByModel2.toString())

                    if (resultClassId == line.first.toInt()) {
                        correctId++
                    }
                    if (resultClassId2 == line.first.toInt()) {
                        correctId2++
                    }
                }
                Log.d(
                    "NEURAL",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId  --- Atlag Megtalalasi szazalek: ${averageModel / csvLines.size}"
                )
                Log.d(
                    "NEURAL-2",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId2  --- Atlag Megtalalasi szazalek: ${averageModel2 / csvLines.size}\""
                )
            }
        }
    }


    private fun iterateOnTestMicroDirectory() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dir = "/data/data/com.example.trafficsigns/TestMicro/"
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
                    val line = Pair(it.split(",")[6], it.split(",")[7])
                    val bitmap = BitmapFactory.decodeFile(dir + line.second)

                    val resultByModel = classifier(bitmap)
                    val resultByModel2 = classifier2(bitmap)

                    val resultClassId = labels.indexOf(resultByModel.categories[0].label)
                    val resultClassId2 = labels.indexOf(resultByModel2[0].label)

                    val searchedLabel = labels[line.first.toInt()]
                    val resultObject =
                        resultByModel.categories.firstOrNull { it1 -> it1.label == searchedLabel }
                    Log.d(
                        "NEURAL-TEST",
                        "ClassId: ${line.first}... Result: $resultClassId | ${resultByModel.categories[0].label} - ${resultByModel.categories[0].score} "
                    )
                    if (resultObject != null) {
                        averageModel += resultObject.score
                        Log.d("NeuralClassifier_Score", resultObject.score.toString())
                    }
                    val resultObject2 =
                        resultByModel2.firstOrNull { it1 -> it1.label == searchedLabel }
                    Log.d("NEURAL-TEST-2", "ClassId: ${line.first}... Result: $resultClassId2 | ${resultByModel2[0].label} - ${resultByModel2[0].score} ")

                    if (resultObject2 != null) {
                        averageModel2 += resultObject2.score
                        Log.d("NeuralClassifier2_Score", resultObject2.score.toString())
                    }

                   //                    Log.d("NEURAL", resultByModel.toString())
//                    Log.d("NEURAL-Classifier2", resultByModel2.toString())

                    if (resultClassId == line.first.toInt()) {
                        correctId++
                    }
                    if (resultClassId2 == line.first.toInt()) {
                        correctId2++
                    }
                }
                Log.d(
                    "NEURAL",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId  --- Atlag Megtalalasi szazalek: ${averageModel / csvLines.size}"
                )
                Log.d(
                    "NEURAL-2",
                    "Ossz: ${csvLines.size}  |  Helyes: $correctId2  --- Atlag Megtalalasi szazalek: ${averageModel2 / csvLines.size}\""
                )
            }
        }
    }


}

