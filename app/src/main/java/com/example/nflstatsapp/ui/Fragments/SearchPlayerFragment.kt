package com.example.nflstatsapp.ui.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nflstatsapp.R
import com.example.nflstatsapp.ui.adapters.PlayerAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerViewModel
import com.example.nflstatsapp.ui.viewModels.PlayerViewModelFactory
import com.example.nflstatsapp.data.players.PlayerRepository
import com.example.nflstatsapp.NFLStatsApplication

class SearchPlayerFragment : Fragment(R.layout.fragment_search) {

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerViewModel: PlayerViewModel
    private val playerRepository: PlayerRepository by lazy {
        (requireActivity().application as NFLStatsApplication).playerRepository
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        playerAdapter = PlayerAdapter { player ->
            // Handle player click
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
}
