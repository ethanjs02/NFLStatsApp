package com.example.nflstatsapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.players.Player

    class PlayerAdapter(private val onItemClick: (Player) -> Unit) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
        private var players = emptyList<Player>()

        class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val playerName: TextView = itemView.findViewById(R.id.player_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
            return PlayerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
            val currentPlayer = players[position]
            holder.playerName.text = currentPlayer.fullName

            holder.itemView.setOnClickListener {
                onItemClick(currentPlayer)
            }
        }

        override fun getItemCount(): Int = players.size

        // Update the list of players and refresh the RecyclerView
        fun setData(newPlayers: List<Player>) {
            players = newPlayers
            notifyDataSetChanged()
        }
    }

