package com.example.trafficsigns.ui.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentMainScreenBinding
import com.example.trafficsigns.ui.adapters.MainMenuAdapter
import com.example.trafficsigns.ui.adapters.SampleListAdapter
import com.example.trafficsigns.ui.utils.Settings
import com.google.android.material.navigation.NavigationView


class MainScreenFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var trafficViewModel: TrafficSignsCollectionViewModel
    private lateinit var trafficRecyclerView: RecyclerView
    private lateinit var binding: FragmentMainScreenBinding
    private var collectionList =  emptyList<TrafficSignsCollection>()
    private lateinit var sampleListAdapter: SampleListAdapter
    private lateinit var menuAdapter: MainMenuAdapter
    private lateinit var allTrafficSign: ArrayList<TrafficSign>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_screen, container, false)
        binding.navView.setNavigationItemSelectedListener(this)
        binding.imageButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sampleListAdapter = SampleListAdapter(R.id.action_mainScreenFragment_to_detailFragment, null,context)
        menuAdapter = MainMenuAdapter(context)

        trafficRecyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        trafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        trafficViewModel.readAllData.observe(viewLifecycleOwner, { collection ->
            menuAdapter.changeData(collection)
            collectionList = collection
            allTrafficSign = ArrayList<TrafficSign>()
            collection.forEach {
                allTrafficSign.addAll(it.trafficSigns)
            }
        })

        binding.search.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != "") {
                    trafficRecyclerView.adapter = sampleListAdapter
                    trafficRecyclerView.forceLayout()
                    sampleListAdapter.filter.filter(newText)
                }
                else {
                    changeAdapter()
                }
                return true
            }
        })

        binding.search.setOnSearchClickListener {
            trafficRecyclerView.forceLayout()
            Settings.isGrid = false
            sampleListAdapter.changeData(allTrafficSign.shuffled())
            binding.title.visibility = View.INVISIBLE
        }

        binding.search.setOnCloseListener { changeAdapter() }
    }

    override fun onResume() {
        super.onResume()
        changeAdapter()
        binding.search.onActionViewCollapsed()
    }

    private fun changeAdapter(): Boolean {
        trafficRecyclerView.adapter = menuAdapter
        binding.title.visibility = View.VISIBLE
        return false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.profileFragment -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_profileFragment)
            }
            R.id.knownSigns -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_knownSigns)
            }
            R.id.quizFragment -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_quizFragment)
            }
            R.id.neuralNetworkFragment -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_neuralNetworkFragment)
            }
            R.id.cameraNeural -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_cameraNeural)
            }
//            R.id.settingsFragment -> {
//                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_settingsFragment)
//            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}