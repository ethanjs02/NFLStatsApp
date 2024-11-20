package com.example.nflstatsapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import com.example.nflstatsapp.data.NFLDatabase
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.data.players.PlayerRepository

class PlayerViewModel(private val repository: PlayerRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Player>> = _searchQuery.switchMap { query ->
        if (query.isEmpty()) {
            MutableLiveData(emptyList()) // Show nothing when query is empty
        } else {
            repository.searchPlayers(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class PlayerViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
