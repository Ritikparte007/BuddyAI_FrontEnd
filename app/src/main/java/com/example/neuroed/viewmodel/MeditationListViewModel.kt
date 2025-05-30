// ========== MeditationListViewModel.kt ==========
package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.neuroed.model.Meditation
import com.example.neuroed.repository.MeditationRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
//import com.google.gson.Gson


class MeditationListViewModel(
    private val repository: MeditationRepository,
    private val userId: Int
) : ViewModel() {

    private val _meditations = MutableLiveData<List<Meditation>>(emptyList())
    val meditations: LiveData<List<Meditation>> = _meditations

    init {
        loadMeditations(onlyToday = false)
    }

    fun loadMeditations(onlyToday: Boolean) {
        viewModelScope.launch {
            Log.d("MeditationListVM", "Starting fetch for userId=$userId, onlyToday=$onlyToday")
            try {
                val list = repository.fetchMeditations(userId, onlyToday)

                Log.d("MeditationListVM", "Fetched items: $list")


                val json = Gson().toJson(list)
                Log.d("MeditationListVM", "Fetched items JSON:\n$json")


                _meditations.value = list
            } catch (e: Exception) {
                Log.e("MeditationListVM", "Failed to load meditations", e)
                _meditations.value = emptyList()
            }
        }
    }
}
