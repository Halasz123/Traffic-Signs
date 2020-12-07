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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
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

        mMyProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            this.myProfile = profile
            Log.d(PROFILE_TAG, profile.toString())
            name.setText(myProfile?.name)
            age.setText(myProfile?.age.toString())
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        name.afterTextChanged { saveButton.isEnabled = true }
        age.afterTextChanged { saveButton.isEnabled = true }


        saveButton.setOnClickListener {
            updateProfile()
        }

        binding.knownSigns.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_profileFragment_to_knownSigns)
        }

        Log.d(PROFILE_TAG, myProfile.toString())
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private fun updateProfile(){
        mMyProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, { profile ->
            profile.name = name.text.toString()
            profile.age = age.text.toString().toInt()
            mMyProfileViewModel.updateProfile(profile)
        })
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
