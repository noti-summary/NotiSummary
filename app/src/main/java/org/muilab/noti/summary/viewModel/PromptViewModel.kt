package org.muilab.noti.summary.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.model.Prompt

class PromptViewModel(application: Application, promptDatabase: PromptDatabase) :
    AndroidViewModel(application) {

    private val sharedPreferences =
        getApplication<Application>().getSharedPreferences("PromptPref", Context.MODE_PRIVATE)

    private val promptDao = promptDatabase.promptDao()

    private val defaultPrompt = "Summarize the notifications in a Traditional Chinese statement."
    private val _promptSentence = MutableLiveData<String>()
    val promptSentence: LiveData<String> = _promptSentence
    val allPromptSentence: LiveData<List<String>> = promptDao.getAllPrompt().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    init {
        val resultValue = sharedPreferences.getString("curPrompt", defaultPrompt)
        _promptSentence.value = resultValue!!
    }

    fun addPrompt(newPromptText: String) {
        val updatePrompt = newPromptText.trim()
        scope.launch {
            promptDao.insertPromptIfNotExists(Prompt(0, updatePrompt))
        }
        choosePrompt(updatePrompt)
    }

    fun choosePrompt(updatePrompt: String) {
        if (_promptSentence.value != updatePrompt) {
            _promptSentence.postValue(updatePrompt)
            sharedPreferences.edit().putString("curPrompt", updatePrompt).apply()
        }
    }

    fun getCurPrompt(): String {
        return promptSentence.value ?: "Summarize the notifications in a Traditional Chinese statement."
    }

    fun updatePrompt(oldPrompt: String, newPrompt: String) {
        scope.launch{
            promptDao.updatePromptText(oldPrompt, newPrompt)
            choosePrompt(newPrompt)
        }
    }

    fun deletePrompt(prompt: String) {
        scope.launch {
            promptDao.deleteByPromptText(prompt)
        }
        choosePrompt(defaultPrompt)
    }
}
