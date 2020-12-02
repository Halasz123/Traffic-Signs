package com.example.trafficsigns.ui.fragments.List

import android.content.BroadcastReceiver
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.databinding.FragmentSampleListBinding
import com.example.trafficsigns.ui.adapters.MainMenuAdapter
import com.example.trafficsigns.ui.adapters.SampleListAdapter
import com.example.trafficsigns.ui.fragments.Detail.DetailFragment
import com.example.trafficsigns.ui.interfaces.ItemClickListener
import com.example.trafficsigns.ui.interfaces.SetOnCheckedChangeListener

const val ARG_OBJECT = "object"

class SampleListFragment : Fragment(), ItemClickListener, SetOnCheckedChangeListener {

    private lateinit var binding: FragmentSampleListBinding
    private lateinit var trafficSignList: List<TrafficSign>
    private lateinit var recyclerView: RecyclerView
    private lateinit var broadcastReceiver: BroadcastReceiver

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
                R.layout.fragment_sample_list,
                container,
                false
            )
//        val parent = parentFragment as CollectionListFragment
//        parent.gridListener = this

        //broadcastReceiver.

        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            trafficSignList= getSerializable(ARG_OBJECT) as List<TrafficSign>
        }
        Log.d("List", trafficSignList.toString())

        val mAdapter = SampleListAdapter(this)
        mAdapter.setData(trafficSignList)
        recyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

    }

    companion object {

        @JvmStatic
        fun newInstance( trafficSignList: List<TrafficSign>): SampleListFragment {
            val fragment = SampleListFragment()

            val args = Bundle()
            args.putSerializable(ARG_OBJECT, trafficSignList as ArrayList)
            fragment.arguments = args
            return fragment
        }

    }

    override fun onItemClickListener(position: Int) {
    }

    override fun onItemClickListener(trafficSign: TrafficSign) {
        binding.root.findNavController().navigate(R.id.action_collectionListFragment_to_detailFragment, DetailFragment.newInstanceBundle(trafficSign))
    }

    override fun setOnCheckedChangeListener(isChecked: Boolean) {
        if (isChecked) {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }
}