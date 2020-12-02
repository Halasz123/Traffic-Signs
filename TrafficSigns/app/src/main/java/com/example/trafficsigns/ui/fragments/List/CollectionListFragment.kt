package com.example.trafficsigns.ui.fragments.List

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.FragmentCollectionListBinding
import com.example.trafficsigns.ui.adapters.TrafficCollectionListAdapter
import com.example.trafficsigns.ui.interfaces.SetOnCheckedChangeListener
import com.google.android.material.tabs.TabLayoutMediator


class CollectionListFragment : Fragment() {

    private lateinit var trafficCollectionAdapter: TrafficCollectionListAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentCollectionListBinding
    lateinit var mTrafficViewModel: TrafficSignsCollectionViewModel
    private var startPosition: Int = 1
    var gridListener: SetOnCheckedChangeListener? = null

    private var mCollectionList =  emptyList<TrafficSignsCollection>()

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
           // mCollectionList = bundle.getSerializable("collectionList") as List<TrafficSignsCollection>
        }
//        binding.backButton.setOnClickListener {
//            view.findNavController().navigate(R.id.action_collectionListFragment_to_mainScreenFragment)
//        }


        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            sendGridOnData(isChecked)
        }

        trafficCollectionAdapter = TrafficCollectionListAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = trafficCollectionAdapter
        viewPager.setCurrentItem(startPosition, false)
        val tabLayout = binding.tabLayout

        mTrafficViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        mTrafficViewModel.readAllData.observe(viewLifecycleOwner, Observer { collection ->
            trafficCollectionAdapter.setData(collection)
            mCollectionList = collection
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = mCollectionList[position].groupId
            }.attach()
        })

    }

    companion object {
        @JvmStatic
        fun newInstanceBundle(startPosition: Int): Bundle {
            val args = Bundle()
            args.putInt("currentPosition", startPosition)
            return args
        }
    }

    private fun sendGridOnData(isGrid: Boolean) {
        val broadcastIntent = Intent("sendGridOnMessage")
        broadcastIntent.putExtra("grid", isGrid)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(broadcastIntent)
    }


}