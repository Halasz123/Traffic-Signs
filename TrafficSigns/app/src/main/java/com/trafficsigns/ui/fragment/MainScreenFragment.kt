package com.trafficsigns.ui.fragment

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
import com.trafficsigns.R
import com.trafficsigns.data.dataclass.TrafficSign
import com.trafficsigns.data.dataclass.TrafficSignsCollection
import com.trafficsigns.data.database.viewmodel.TrafficSignsCollectionViewModel
import com.trafficsigns.databinding.FragmentMainScreenBinding
import com.trafficsigns.ui.adapter.MainMenuAdapter
import com.trafficsigns.ui.adapter.SampleListAdapter
import com.google.android.material.navigation.NavigationView
import com.trafficsigns.ui.singleton.GeneralSingleton

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * This is the home screen of the application,
 * here is the base of the navigation system.
 * Display a list of traffic sign types, after touching one of them,
 * we are navigating to the CollectionListFragment. Search field on the top with
 * onChange event for refreshing recyclerview and adapter. Basically the adapter is
 * replaced after text change.
 * Navigation drawer menu on the left side.
**/
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.imageButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        sampleListAdapter = SampleListAdapter(R.id.action_mainScreenFragment_to_detailFragment, null,context)
        menuAdapter = MainMenuAdapter(context)

        trafficRecyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        trafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        trafficViewModel.readAllData.observe(viewLifecycleOwner) { collection ->
            menuAdapter.changeData(collection)
            collectionList = collection
            allTrafficSign = ArrayList<TrafficSign>()
            collection.forEach {
                allTrafficSign.addAll(it.trafficSigns)
            }
        }

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
            GeneralSingleton.isGrid = false
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
            R.id.cameraNeural -> {
                binding.root.findNavController().navigate(R.id.action_mainScreenFragment_to_cameraNeural)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroyView() {
        binding.navView.setNavigationItemSelectedListener(null)
        super.onDestroyView()
    }

}