package com.trafficsigns.ui.fragments.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.trafficsigns.R
import com.trafficsigns.data.TrafficSignsCollection
import com.trafficsigns.databinding.FragmentCollectionListBinding
import com.trafficsigns.ui.adapters.TrafficCollectionListAdapter
import com.trafficsigns.ui.constants.Key
import com.trafficsigns.ui.utils.Settings
import com.google.android.material.tabs.TabLayoutMediator


class CollectionListFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstanceBundle(startPosition: Int, trafficSignsCollections: List<TrafficSignsCollection>): Bundle {
            val args = Bundle()
            args.putInt(Key.START_POSITION, startPosition)
            args.putSerializable(Key.CURRENT_LIST, trafficSignsCollections as ArrayList)
            return args
        }
    }

    private lateinit var trafficCollectionAdapter: TrafficCollectionListAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentCollectionListBinding
    private var startPosition: Int = 1
    private var collectionList =  emptyList<TrafficSignsCollection>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collection_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = this.arguments
        if (bundle != null){
            startPosition = bundle.getInt(Key.START_POSITION, 1)
            collectionList = bundle.getSerializable(Key.CURRENT_LIST) as List<TrafficSignsCollection>
        }
        binding.switchToGrid.isChecked = Settings.isGrid

        binding.switchToGrid.setOnCheckedChangeListener { _, isChecked ->
            sendGridOnData(isChecked)
        }

        binding.searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                sendSearchText(newText)
                return false
            }

        })
        val tabLayout = binding.tabLayout
        viewPager = binding.pager
        trafficCollectionAdapter = TrafficCollectionListAdapter(this)
        trafficCollectionAdapter.setData(collectionList)
        viewPager.adapter = trafficCollectionAdapter
        viewPager.setCurrentItem(startPosition, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = collectionList[position].groupId
        }.attach()
    }

    private fun sendGridOnData(isGrid: Boolean) {
        val broadcastIntent = Intent(Key.SEND_GRID_CHANGED_MASSAGE_ACTION)
        broadcastIntent.putExtra(Key.GRID, isGrid)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(broadcastIntent)
    }

    private fun sendSearchText(text: String?){
        val broadcastIntent = Intent(Key.SEND_SEARCH_TEXT_MASSAGE_ACTION)
        broadcastIntent.putExtra(Key.SEARCH, text)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(broadcastIntent)
    }
}