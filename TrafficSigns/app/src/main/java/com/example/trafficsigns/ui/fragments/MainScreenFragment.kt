package com.example.trafficsigns.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentMainScreenBinding
import com.example.trafficsigns.ui.adapters.MainMenuAdapter


class MainScreenFragment : Fragment() {

    lateinit var mTrafficViewModel: TrafficSignsCollectionViewModel
    private lateinit var trafficRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil
            .inflate<FragmentMainScreenBinding>(
                inflater,
                R.layout.fragment_main_screen,
                container,
                false
            )


        val mAdapter = MainMenuAdapter(parentFragmentManager)
        trafficRecyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }


        mTrafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        mTrafficViewModel.readAllData.observe(viewLifecycleOwner, Observer { collection ->
            mAdapter.setData(collection)
        })

        return binding.root
    }

}