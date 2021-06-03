package com.trafficsigns.ui.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trafficsigns.R
import com.trafficsigns.data.dataclass.TrafficHistory
import com.trafficsigns.ui.fragment.DetailFragment
import com.trafficsigns.ui.singleton.TrafficSignMemoryCache
import kotlinx.android.synthetic.main.history_item.view.*
import java.util.concurrent.TimeUnit

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Set elements to the recyclerview on dialog with the history of traffic signs.
 */
class TrafficSignHistoryAdapter(private var history: List<TrafficHistory>, private val context: Context, val view: View, var dialog: Dialog? = null): RecyclerView.Adapter<TrafficSignHistoryAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val  currentItem = history[position]
        val sign = TrafficSignMemoryCache.instance.getCachedTrafficSign(currentItem.id)

        context.let { Glide.with(it).load(sign?.image).override(holder.itemView.table_imageview.width, holder.itemView.table_imageview.height).into(holder.itemView.table_imageview) }
        holder.itemView.label_history_text_view.text = sign?.name
        val time = (System.currentTimeMillis() - currentItem.timeStamp )
        holder.itemView.time_text_view.text = String.format("%02d min, %02d sec Ago",
        TimeUnit.MILLISECONDS.toMinutes(time),
        TimeUnit.MILLISECONDS.toSeconds(time) -  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)))

        holder.itemView.setOnClickListener {
            view.findNavController().navigate(R.id.action_cameraNeural_to_detailFragment,
                sign?.let { it1 -> DetailFragment.newInstanceBundle(it1) })
            dialog?.dismiss()
        }
    }

    override fun getItemCount(): Int {
            return history.count()
    }
}