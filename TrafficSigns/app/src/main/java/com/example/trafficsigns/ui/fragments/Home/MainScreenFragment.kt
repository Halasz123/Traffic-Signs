package com.example.trafficsigns.ui.fragments.Home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentMainScreenBinding
import com.example.trafficsigns.ui.adapters.MainMenuAdapter
import com.example.trafficsigns.ui.fragments.List.CollectionListFragment
import com.example.trafficsigns.ui.interfaces.ItemClickListener
import com.google.android.material.navigation.NavigationView


class MainScreenFragment : Fragment(), ItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var mTrafficViewModel: TrafficSignsCollectionViewModel
    private lateinit var trafficRecyclerView: RecyclerView
    private lateinit var binding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(
                inflater,
                R.layout.fragment_main_screen,
                container,
                false
            )
        binding.navView.setNavigationItemSelectedListener(this)
        binding.imageButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mAdapter = MainMenuAdapter(this)
        trafficRecyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }


        mTrafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        mTrafficViewModel.readAllData.observe(viewLifecycleOwner, Observer { collection ->
            mAdapter.setData(collection)
        })
    }

    override fun onItemClickListener(position: Int) {
        binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_collectionListFragment, CollectionListFragment.newInstanceBundle(position))
    }

    override fun onItemClickListener(trafficSign: TrafficSign) {
        //do nothing
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.profileFragment -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_profileFragment)
            }
            R.id.signInFragment -> { binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_signInFragment)}
            R.id.signUpFragment -> { binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_signUpFragment)}
            R.id.neuralNetworkFragment -> { binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_neuralNetworkFragment)}
            R.id.settingsFragment -> { binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_settingsFragment)}
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}