package com.example.nflstatsapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.api.Stat

class StatsAdapter(private val statList: List<Stat>) : RecyclerView.Adapter<StatsAdapter.StatsViewHolder>() {

    // ViewHolder to hold the reference to the views
    class StatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val statName: TextView = view.findViewById(R.id.statName)
        val statValue: TextView = view.findViewById(R.id.statValue)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_stat, parent, false)
        return StatsViewHolder(view)
    }

    // Bind the stat data to the views
    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val stat = statList[position]
        holder.statName.text = stat.statName
        holder.statValue.text = stat.statValue ?: "N/A"  // Display "N/A" if the stat value is null
    }

    // Return the size of the stat list
    override fun getItemCount(): Int = statList.size
}
