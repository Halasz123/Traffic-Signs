package com.example.trafficsigns.ui.fragments.List

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentCollectionListBinding
import com.example.trafficsigns.ui.adapters.TrafficCollectionListAdapter
import com.google.android.material.tabs.TabLayoutMediator

class CollectionListFragment() : Fragment() {

    private lateinit var trafficCollectionAdapter: TrafficCollectionListAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentCollectionListBinding
    private var startPosition: Int = 1

    private lateinit var mTrafficViewModel: TrafficSignsCollectionViewModel
    private lateinit var mCollectionList: List<TrafficSignsCollection>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(
                inflater,
                R.layout.fragment_collection_list,
                container,
                false
            )
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = this.arguments
        if (bundle != null){
            startPosition = bundle.getInt("currentPosition", 1)
            mCollectionList = bundle.getSerializable("collectionList") as List<TrafficSignsCollection>
        }

        trafficCollectionAdapter = TrafficCollectionListAdapter(this, mCollectionList)
        viewPager = binding.pager
        viewPager.adapter = trafficCollectionAdapter
        viewPager.setCurrentItem(startPosition, false)

        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = mCollectionList[position].groupId
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance(startPosition: Int, collectionList: List<TrafficSignsCollection>): CollectionListFragment? {
            val f = CollectionListFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putInt("currentPosition", startPosition)
            args.putSerializable("collectionList", collectionList as ArrayList)
            f.arguments = args
            return f
        }
    }


}