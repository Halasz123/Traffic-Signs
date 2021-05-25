package com.trafficsigns.ui.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.trafficsigns.R
import com.trafficsigns.ui.constant.Network
import com.trafficsigns.ui.fragment.DetailFragment
import com.trafficsigns.ui.singleton.TrafficSignMemoryCache
import com.trafficsigns.ui.network.classifiers.Classifier
import kotlinx.android.synthetic.main.network_result_item.view.*
import org.tensorflow.lite.support.common.FileUtil

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Set elements to the recyclerview on dialog with the result of cnn.
 */
class NetworkResult(private var results: List<Classifier.Recognition>, val view: View, var dialog: Dialog? = null): RecyclerView.Adapter<NetworkResult.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.network_result_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val  currentItem = results[position]
        val sign = TrafficSignMemoryCache.instance.getCachedTrafficSign(currentItem.title)

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
}