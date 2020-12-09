package com.example.trafficsigns.ui.fragments.Profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
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
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.trafficsigns.R
import com.example.trafficsigns.data.MyProfile
import com.example.trafficsigns.data.MyProfileViewModel
import com.example.trafficsigns.databinding.FragmentProfileBinding
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.text.DecimalFormat


const val PROFILE_TAG = "profile"
const val CAPTURE_PHOTO_CODE = 42
const val FILE_NAME = "photo.jpg"
const val IMAGE_PICK_CODE = 1000
const val PERMISSION_CODE = 1001

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
            binding.profilePicture.setImageURI(Uri.parse(myProfile?.picturePath))

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

        binding.galleryPicture.setOnClickListener {
            checkPermission()
        }

        Log.d(PROFILE_TAG, myProfile.toString())
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

    private fun pickGalleryPhoto() {
        val intent = Intent(ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)

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

        val fileProvider = FileProvider.getUriForFile(
            requireContext(),
            "com.example.trafficsigns.ui.fragments.fileprovider",
            photoFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } != null){
            startActivityForResult(takePictureIntent, CAPTURE_PHOTO_CODE)
        }
        else {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_LONG).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var path: String = ""
            when (requestCode) {
                CAPTURE_PHOTO_CODE -> {
                    binding.profilePicture.setImageBitmap(BitmapFactory.decodeFile(photoFile.absolutePath))
                    path = photoFile.absolutePath
                }
                IMAGE_PICK_CODE -> {
                    binding.profilePicture.setImageURI(data?.data)
                    path = data?.data.toString()
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
            mMyProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, {
                it.picturePath = path
                mMyProfileViewModel.updateProfile(it)
            })
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickGalleryPhoto()
                } else {
                    //permission popup denied
                    Toast.makeText(requireContext(), "Presmission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
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
