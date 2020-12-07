package com.example.trafficsigns.ui.fragments.Profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trafficsigns.R
import com.example.trafficsigns.data.MyProfile
import com.example.trafficsigns.data.MyProfileViewModel
import com.example.trafficsigns.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator

const val PROFILE_TAG = "profile"

class ProfileFragment : Fragment() {

    private lateinit var mMyProfileViewModel: MyProfileViewModel
    private var myProfile: MyProfile? = null

    private lateinit var binding: FragmentProfileBinding
    private lateinit var saveButton: Button
    private lateinit var name: EditText
    private lateinit var age: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        saveButton = binding.saveButton
        name = binding.nameEdittext
        age = binding.age

        mMyProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        myProfile = mMyProfileViewModel.myProfile

            Log.d(PROFILE_TAG, myProfile.toString())
            name.setText(myProfile?.name)
            age.setText(myProfile?.age.toString())


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        name.afterTextChanged { saveButton.isEnabled = true }
        age.afterTextChanged { saveButton.isEnabled = true }


        saveButton.setOnClickListener {
            val newProfile = MyProfile(0, name.text.toString(), age.text.toString().toInt())
             mMyProfileViewModel.updateProfile(newProfile)
        }

        Log.d(PROFILE_TAG, myProfile.toString())
    }

    companion object {
        fun newInstance() = ProfileFragment()
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
