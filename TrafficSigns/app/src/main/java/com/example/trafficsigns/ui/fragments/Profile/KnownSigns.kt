package com.example.trafficsigns.ui.fragments.Profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trafficsigns.R
import com.example.trafficsigns.data.MyProfileViewModel
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.databinding.FragmentKnownSignsBinding
import com.example.trafficsigns.ui.adapters.SampleListAdapter
import com.example.trafficsigns.ui.constants.Alert
import com.example.trafficsigns.ui.fragments.Detail.DetailFragment
import com.example.trafficsigns.ui.interfaces.ItemClickListener
import com.example.trafficsigns.ui.utils.Settings

const val KNOWN_TAG = "knownsigns"

class KnownSigns : Fragment(), ItemClickListener {

    private lateinit var binding: FragmentKnownSignsBinding
    lateinit var mProfileViewModel: MyProfileViewModel
    private lateinit var trafficSignListAdapter: SampleListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil
                .inflate(
                        inflater,
                        R.layout.fragment_known_signs,
                        container,
                        false
                )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Settings.isGrid = false
        trafficSignListAdapter = SampleListAdapter(this)
        binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trafficSignListAdapter
        }

        mProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        mProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            profile.knownTrafficSigns?.let { trafficSignListAdapter.setData(it as List<TrafficSign>) }
        })

        binding.testButton.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_knownSigns_to_quizFragment)
        }
    }

    override fun onItemClickListener(position: Int) {
        //do nothing
    }

    override fun onItemClickListener(trafficSign: TrafficSign) {
        binding.root.findNavController().navigate(R.id.action_knownSigns_to_detailFragment, DetailFragment.newInstanceBundle(trafficSign))
    }

    override fun onItemLongClickListener(trafficSign: TrafficSign) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(Alert.YES) { _, _ ->
            deleteSign(trafficSign)
        }
        builder.setNegativeButton(Alert.NO) { _, _ ->
        }
        builder.setTitle(Alert.DELETE)
        builder.setMessage(Alert.DELETE_SURE)
        builder.create().show()
    }

    private fun deleteSign(trafficSign: TrafficSign){
       mProfileViewModel.myProfile.observeOnce(viewLifecycleOwner, { profile ->
           profile.knownTrafficSigns?.remove(trafficSign)
           mProfileViewModel.updateProfile(profile)
           Log.d(KNOWN_TAG, "Deleted")
       })
    }

}


