package com.example.trafficsigns.ui.fragments.List

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.trafficsigns.R

const val ARG_OBJECT = "object"

class SampleListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sample_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val textView = view.findViewById<TextView>(R.id.text1)
            textView.text = getInt(ARG_OBJECT).toString()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = SampleListFragment()

    }
}