    package com.example.neuroed.viewmodel

    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.neuroed.model.UserAppVisitData
    import com.example.neuroed.model.Uservisitdatarespponse
    import com.example.neuroed.repository.UservisitRepository
    import kotlinx.coroutines.launch

    class UservisitdataViewmodel(private val repository: UservisitRepository): ViewModel(){
        private val _saveResponse = MutableLiveData<Uservisitdatarespponse>()
        val saveResponse: LiveData<Uservisitdatarespponse> get() = _saveResponse

        fun Uservisitdata(model: UserAppVisitData){
            viewModelScope.launch {
                val response = repository.Uservisitdatasave(model)
                _saveResponse.value = response
            }
        }
    }