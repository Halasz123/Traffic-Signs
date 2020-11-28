package com.example.trafficsigns.ui.fragments.Detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign


class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstanceBundle(trafficSign: TrafficSign): Bundle {
            val args = Bundle()
            args.putSerializable("currentItem", trafficSign )
            return args
        }
    }

}