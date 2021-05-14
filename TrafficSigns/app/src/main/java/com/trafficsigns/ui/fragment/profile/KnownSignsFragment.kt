package com.trafficsigns.ui.fragment.profile

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
import com.trafficsigns.R
import com.trafficsigns.data.database.viewmodel.MyProfileViewModel
import com.trafficsigns.data.dataclass.TrafficSign
import com.trafficsigns.databinding.FragmentKnownSignsBinding
import com.trafficsigns.ui.adapter.SampleListAdapter
import com.trafficsigns.ui.constant.Alert
import com.trafficsigns.ui.interfaces.ItemLongClickListener
import com.trafficsigns.ui.singleton.GeneralSingleton

const val KNOWN_TAG = "knownsigns"

/**
 * @author: Halász Botond
 * @since: 05/10/2021
 *
 * Display a list of known traffic signs, read data from database.
 * The elements can be deleted by long click.
 */

class KnownSignsFragment : Fragment(), ItemLongClickListener {

    private lateinit var binding: FragmentKnownSignsBinding
    private lateinit var mProfileViewModel: MyProfileViewModel
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
        GeneralSingleton.isGrid = false
        trafficSignListAdapter = SampleListAdapter(R.id.action_knownSigns_to_detailFragment, this, context)
        binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trafficSignListAdapter
        }

        mProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        mProfileViewModel.myProfile.observe(viewLifecycleOwner, { profile ->
            profile.knownTrafficSigns?.let { trafficSignListAdapter.changeData(it as List<TrafficSign>) }
        })

        binding.testButton.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_knownSigns_to_quizFragment)
        }
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


