package org.muilab.noti.summary.viewModel

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.model.Prompt

class PromptViewModel(promptDatabase: PromptDatabase) : ViewModel() {
    private val promptDao = promptDatabase.promptDao()

    private val _promptSentence =
        MutableLiveData("Summarize the notifications in a Traditional Chinese statement.")
    val promptSentence: LiveData<String> = _promptSentence
    val allPromptSentence: LiveData<List<String>> = promptDao.getAllPrompt().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    fun addPrompt(newPromptText: String) {
        val updatePrompt = newPromptText.trim()
        scope.launch {
            promptDao.insertPromptIfNotExists(Prompt(0, updatePrompt))
        }
        _promptSentence.value = updatePrompt
    }

    fun choosePrompt(updatePrompt: String) {
        _promptSentence.value = updatePrompt
        if (promptSentence.value != null)
            Log.d("choosePrompt", promptSentence.value!!)
    }

    fun getCurPrompt(): String {
        return promptSentence.value ?: "Summarize the notifications in a Traditional Chinese statement."
    }
}