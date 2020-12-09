package com.example.trafficsigns.ui.fragments.Profile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.room.TypeConverter
import com.example.trafficsigns.R
import com.example.trafficsigns.data.MyProfile
import com.example.trafficsigns.data.MyProfileViewModel
import com.example.trafficsigns.databinding.FragmentProfileBinding
import kotlinx.android.synthetic.main.group_item.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


const val PROFILE_TAG = "profile"
const val REQUEST_CODE = 42
const val FILE_NAME = "photo.jpg"

class ProfileFragment : Fragment() {

    private lateinit var mMyProfileViewModel: MyProfileViewModel
    private var myProfile: MyProfile? = null

    private lateinit var binding: FragmentProfileBinding
    private lateinit var saveButton: Button
    private lateinit var name: EditText
    private lateinit var age: EditText
    private lateinit var photoFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        saveButton = binding.saveButton
        name = binding.nameEdittext
        age = binding.age


        mMyProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)

        mMyProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            this.myProfile = profile
            Log.d(PROFILE_TAG, profile.toString())
            name.setText(myProfile?.name)
            age.setText(myProfile?.age.toString())
            binding.averageScoreValue.text =
                DecimalFormat("##.##").format(myProfile?.scores?.average())
            binding.maxScoreValue.text = myProfile?.scores?.maxOrNull().toString()
            if (myProfile?.picturePath != "") {
                val profileImage = BitmapFactory.decodeFile(myProfile?.picturePath)
                binding.profilePicture.setImageBitmap(profileImage)
            }

            saveButton.isEnabled = false

        })

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        name.afterTextChanged { saveButton.isEnabled = true }
        age.afterTextChanged { saveButton.isEnabled = true }


        saveButton.setOnClickListener {
            updateProfile()
        }

        binding.knownSigns.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_profileFragment_to_knownSigns)
        }

        binding.testButton.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_profileFragment_to_quizFragment)
        }

        binding.createPictureButton.setOnClickListener {
            startCameraIntent()
        }

        Log.d(PROFILE_TAG, myProfile.toString())
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  File.createTempFile(fileName, ".jpg", storageDirectory)

    }


    private fun updateProfile(){
        mMyProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, { profile ->
            profile.name = name.text.toString()
            profile.age = age.text.toString().toInt()
            mMyProfileViewModel.updateProfile(profile)
        })
    }

    private fun startCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)

        val fileProvider = FileProvider.getUriForFile(requireContext(), "com.example.trafficsigns.ui.fragments.fileprovider", photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } != null){
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        }
        else {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
           // val takenImage = data?.extras?.get("data") as Bitmap
               val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.profilePicture.setImageBitmap(takenImage)
            mMyProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, {
                it.picturePath = photoFile.absolutePath
                mMyProfileViewModel.updateProfile(it)
            })
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
