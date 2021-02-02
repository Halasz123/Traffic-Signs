package com.example.trafficsigns.ui.fragments.Profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.trafficsigns.R
import com.example.trafficsigns.databinding.FragmentKnownSignsBinding
import com.example.trafficsigns.databinding.FragmentNeuralNetworkBinding
import com.example.trafficsigns.ml.Second
import com.example.trafficsigns.ui.constants.Data
import com.example.trafficsigns.ui.constants.ToastMessage
import org.tensorflow.lite.support.image.TensorImage
import java.io.File


class NeuralNetworkFragment : Fragment() {

    private lateinit var binding: FragmentNeuralNetworkBinding
    private lateinit var photoFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(
                inflater,
                R.layout.fragment_neural_network,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.photo.setOnClickListener {
            startCameraIntent()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var path: String = ""
            when (requestCode) {
                CAPTURE_PHOTO_CODE -> {
                    binding.profilePicture.setImageBitmap(BitmapFactory.decodeFile(photoFile.absolutePath))
                    val bitmap: Bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val model = Second.newInstance(requireContext())

// Creates inputs for reference.
                    val image = TensorImage.fromBitmap(bitmap)

// Runs model inference and gets result.
                    val outputs = model.process(image)
                    Log.d("NEURAL", outputs.toString())
                    val probability = outputs.probabilityAsCategoryList
                    Log.d("NEURAL", probability.toString())

// Releases model resources if no longer used.
                    model.close()
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

}