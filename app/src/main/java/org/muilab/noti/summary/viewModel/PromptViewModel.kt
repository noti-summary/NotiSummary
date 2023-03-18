package org.muilab.noti.summary.viewModel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.model.Prompt

class PromptViewModel(promptDatabase: PromptDatabase): ViewModel() {
    private val promptDao = promptDatabase.promptDao()

    private val _promptSentence = MutableLiveData("")
    val promptSentence: LiveData<String> = _promptSentence
    val allPromptSentence: LiveData<List<String>> = promptDao.getAllPrompt().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    fun addPrompt(newPromptText: String) {
        scope.launch {
            promptDao.insertPrompt(Prompt(0, newPromptText))
        }
        _promptSentence.value = newPromptText
    }

    fun choosePrompt(updatePrompt: String) {
        _promptSentence.value = updatePrompt
    }
}