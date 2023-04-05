package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.room.APIKeyDatabase
import org.muilab.noti.summary.model.APIKeyEntity

class APIKeyViewModel(application: Application, apiKeyDatabase: APIKeyDatabase) :
    AndroidViewModel(application) {

    private val sharedPreferences =
        getApplication<Application>().getSharedPreferences("ApiPref", Context.MODE_PRIVATE)

    private val apiKeyDao = apiKeyDatabase.apiKeyDao()

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    private val defaultAPI = context.getString(R.string.system_key)
    private val _apiKey = MutableLiveData<String>()
    val apiKey: LiveData<String> = _apiKey
    val allAPIKey: LiveData<List<String>> = apiKeyDao.getAllAPI().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    init {
        val resultValue = sharedPreferences.getString("userAPIKey", defaultAPI)
        _apiKey.value = resultValue!!
    }

    fun addAPI(newApiKey: String) {
        val updateApiKey = newApiKey.trim()
        scope.launch {
            apiKeyDao.insertAPIKeyIfNotExists(APIKeyEntity(0, updateApiKey))
        }
        chooseAPI(updateApiKey)
    }

    fun chooseAPI(updateAPIKey: String) {
        if (_apiKey.value != updateAPIKey) {
            _apiKey.postValue(updateAPIKey)
            sharedPreferences.edit().putString("userAPIKey", updateAPIKey).apply()
        }
    }

    fun deleteAPI(apiKey: String) {
        scope.launch {
            apiKeyDao.deleteByAPIKey(apiKey)
        }
        chooseAPI(defaultAPI)
    }
}
