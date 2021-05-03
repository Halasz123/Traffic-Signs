package com.example.trafficsigns.ui.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.ui.fragments.detail.DetailFragment
import com.example.trafficsigns.ui.fragments.network.TrafficSignMemoryCache
import com.example.trafficsigns.ui.fragments.network.tflite.Classifier
import kotlinx.android.synthetic.main.network_result_item.view.*
import org.tensorflow.lite.task.vision.classifier.Classifications

class NetworkResult(private var results: List<Classifier.Recognition>, val view: View, var dialog: Dialog? = null): RecyclerView.Adapter<NetworkResult.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.network_result_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val  currentItem = results[position]
        val sign = TrafficSignMemoryCache.instance.getCachedTrafficSign(currentItem.id)

        holder.itemView.index_textView.text = "${position+1}."
        holder.itemView.label_textView.text = sign?.name
        holder.itemView.percentage_textView.text = "${(currentItem.confidence * 100).toString().take(4)}%"

        holder.itemView.setOnClickListener {
            view.findNavController().navigate(R.id.action_cameraNeural_to_detailFragment,
                sign?.let { it1 -> DetailFragment.newInstanceBundle(it1) })
            dialog?.dismiss()
        }


    }

    override fun getItemCount(): Int {
            return results.count()
    }

    fun changeData(results: List<Classifier.Recognition>){
        this.results = results
        notifyDataSetChanged()
    }
}