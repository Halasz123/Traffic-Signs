package com.trafficsigns.ui.fragments.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.trafficsigns.R
import com.trafficsigns.data.MyProfileViewModel
import com.trafficsigns.data.TrafficSign
import com.trafficsigns.databinding.FragmentDetailBinding
import com.trafficsigns.ui.constants.Key
import com.trafficsigns.ui.constants.ToastMessage

const val DETAIL_TAG = "detailFragment"

class DetailFragment : Fragment(){

    companion object {
        @JvmStatic
        fun newInstanceBundle(trafficSign: TrafficSign): Bundle {
            val args = Bundle()
            args.putSerializable(Key.CURRENT_ITEM, trafficSign )
            return args
        }
    }

    private lateinit var binding: FragmentDetailBinding
    private lateinit var trafficSign: TrafficSign
    private lateinit var myProfileViewModel: MyProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        myProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = this.arguments
        if (bundle != null){
            trafficSign = bundle.getSerializable(Key.CURRENT_ITEM) as TrafficSign
        }
        Glide
            .with(view)
            .load(trafficSign.image)
            .override(binding.trafficImage.width, binding.trafficImage.height)
            .into(binding.trafficImage)
        binding.nameTextView.text = trafficSign.name
        binding.groupTextView.text = "${trafficSign.group?.capitalize()} signs"
        binding.descriptionTextView.text = trafficSign.description

        myProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            if (profile.knownTrafficSigns?.contains(trafficSign) == true) {
                binding.starButton.isEnabled = false
                binding.starButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow), android.graphics.PorterDuff.Mode.MULTIPLY)
            }
        })

        binding.starButton.setOnClickListener {
            updateMyTrafficSignList()
            binding.starButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow), android.graphics.PorterDuff.Mode.MULTIPLY)
            Toast.makeText(requireContext(), ToastMessage.KNOWN_SIGN, Toast.LENGTH_SHORT).show()
        }

        binding.floatingActionButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_detailFragment_to_mainScreenFragment)
        }

        binding.readMoreButton.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW)
            urlIntent.data = Uri.parse(trafficSign.traffic_rules)
            startActivity(urlIntent)
        }
    }

    private fun updateMyTrafficSignList(){
        myProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            Log.d(DETAIL_TAG, profile.toString())
            if (profile.knownTrafficSigns?.contains(trafficSign) == false){
                profile.knownTrafficSigns?.add(trafficSign)
                myProfileViewModel.updateProfile(profile)
            }
        })
    }

}