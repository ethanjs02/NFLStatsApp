package com.example.nflstatsapp.ui.fragments

import PlayerStatsData
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.example.nflstatsapp.R
import com.example.nflstatsapp.ui.adapters.PlayerAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerViewModel
import com.example.nflstatsapp.ui.viewModels.PlayerViewModelFactory
import com.example.nflstatsapp.data.players.PlayerRepository
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.ui.activties.ComparisonActivity

class SearchPlayerFragment : Fragment(R.layout.fragment_search) {

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerViewModel: PlayerViewModel
    private val playerRepository: PlayerRepository by lazy {
        (requireActivity().application as NFLStatsApplication).playerRepository
    }

    private var player1: PlayerStatsData? = null
    private var tabValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            player1 = it.getSerializable(ARG_PLAYER1) as? PlayerStatsData
            tabValue = it.getString(ARG_TAB_VALUE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        playerAdapter = PlayerAdapter { player ->
            val pos1 = player1?.position
            val pos2 = player.position

            // Check if player1 and player2 are compatible
            if (isCompatible(pos1, pos2)) {
                // If compatible, pass the players to the ComparisonActivity
                val intent = Intent(requireContext(), ComparisonActivity::class.java).apply {
                    putExtra("player2_data", player)
                    putExtra("player1_data", player1)
                    putExtra("tabValue", tabValue)
                }
                startActivity(intent)
            } else {
                // If incompatible, show a Toast message
                Toast.makeText(requireContext(), "Incompatible player comparison!", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = playerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val viewModelFactory = PlayerViewModelFactory(playerRepository)
        playerViewModel = ViewModelProvider(this, viewModelFactory)[PlayerViewModel::class.java]

        playerViewModel.searchResults.observe(viewLifecycleOwner) { players ->
            playerAdapter.setData(players)
        }

        val searchView = view.findViewById<SearchView>(R.id.searchView)

        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.cardBackground), PorterDuff.Mode.SRC_IN)

        val closeIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red), PorterDuff.Mode.SRC_IN)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                playerViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun isCompatible(position1: String?, position2: String?): Boolean {
        return when {
            position1 == "Quarterback" && position2 == "Quarterback" -> true
            position1 == "Place kicker" && position2 == "Place kicker" -> true
            position1 != "Quarterback" && position2 != "Quarterback"
                    && position1 != "Place kicker" && position2 != "Place kicker" -> true
            else -> false
        }
    }

    companion object {
        private const val ARG_PLAYER1 = "player1"
        private const val ARG_TAB_VALUE = "tab_value"

        fun newInstance(player1: PlayerStatsData, tabValue: String): SearchPlayerFragment {
            val fragment = SearchPlayerFragment()
            val args = Bundle().apply {
                putSerializable(ARG_PLAYER1, player1)
                putString(ARG_TAB_VALUE, tabValue)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
