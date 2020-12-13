package com.example.trafficsigns.ui.fragments.Home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentMainScreenBinding
import com.example.trafficsigns.ui.adapters.MainMenuAdapter
import com.example.trafficsigns.ui.adapters.SampleListAdapter
import com.example.trafficsigns.ui.fragments.Detail.DetailFragment
import com.example.trafficsigns.ui.fragments.List.CollectionListFragment
import com.example.trafficsigns.ui.fragments.Profile.observeOnce
import com.example.trafficsigns.ui.interfaces.ItemClickListener
import com.google.android.material.navigation.NavigationView


class MainScreenFragment : Fragment(), ItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var mTrafficViewModel: TrafficSignsCollectionViewModel
    private lateinit var trafficRecyclerView: RecyclerView
    private lateinit var binding: FragmentMainScreenBinding
    private var mCollectionList =  emptyList<TrafficSignsCollection>()
    private lateinit var sampleListAdapter: SampleListAdapter
    private lateinit var menuAdapter: MainMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        sampleListAdapter = SampleListAdapter(this)
        menuAdapter = MainMenuAdapter(this)

        trafficRecyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        mTrafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        mTrafficViewModel.readAllData.observe(viewLifecycleOwner, { collection ->
            menuAdapter.setData(collection)
            mCollectionList = collection
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
            mTrafficViewModel.readAllData.observeOnce(this@MainScreenFragment, { it ->
                val arrayList = ArrayList<TrafficSign>()
                it.forEach {
                    arrayList.addAll(it.trafficSigns)
                }
                sampleListAdapter.setData(arrayList.shuffled())
            })
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

    override fun onItemClickListener(position: Int) {
        binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_collectionListFragment, CollectionListFragment.newInstanceBundle(position, mCollectionList))
    }

    override fun onItemClickListener(trafficSign: TrafficSign) {
        binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_detailFragment, DetailFragment.newInstanceBundle(trafficSign))
    }

    override fun onItemLongClickListener(trafficSign: TrafficSign) {
        //do nothing
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
            R.id.settingsFragment -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_settingsFragment)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}