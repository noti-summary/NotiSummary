package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.model.Prompt
import org.muilab.noti.summary.util.PromptAction
import org.muilab.noti.summary.util.uploadData

class PromptViewModel(application: Application, promptDatabase: PromptDatabase) :
    AndroidViewModel(application) {

    private val sharedPreferences =
        getApplication<Application>().getSharedPreferences("PromptPref", Context.MODE_PRIVATE)

    private val promptDao = promptDatabase.promptDao()

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext


    private val defaultPrompt = context.getString(R.string.default_summary_prompt)
    private val _promptSentence = MutableLiveData<String>()
    val promptSentence: LiveData<String> = _promptSentence
    val allPromptSentence: LiveData<List<String>> = promptDao.getAllPrompt().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    init {
        val resultValue = sharedPreferences.getString("curPrompt", defaultPrompt)
        _promptSentence.value = resultValue!!
    }

    fun addPrompt(newPromptText: String, editHistory: Map<String, String>) {
        val updatePrompt = newPromptText.trim()
        scope.launch {
            promptDao.insertPromptIfNotExists(Prompt(0, updatePrompt))
        }
        choosePrompt(updatePrompt, false)
        logPrompt("create", editHistory, newPromptText)
    }

    fun choosePrompt(updatePrompt: String, userDecision: Boolean) {
        if (_promptSentence.value != updatePrompt) {
            _promptSentence.postValue(updatePrompt)
            sharedPreferences.edit().putString("curPrompt", updatePrompt).apply()
        }
        if (userDecision)
            logPrompt("switch", mapOf(), updatePrompt)
    }

    fun getCurPrompt(): String {
        return promptSentence.value ?: context.getString(R.string.default_summary_prompt)
    }

    fun updatePrompt(oldPrompt: String, newPrompt: String, editHistory: Map<String, String>) {
        scope.launch{
            promptDao.updatePromptText(oldPrompt, newPrompt)
            choosePrompt(newPrompt, false)
        }
        logPrompt("update", editHistory, newPrompt)
    }

    fun deletePrompt(prompt: String) {
        scope.launch {
            promptDao.deleteByPromptText(prompt)
        }
        logPrompt("delete", mapOf(), prompt)
        choosePrompt(defaultPrompt, false)
    }

    fun logPrompt(action: String, history: Map<String, String>, newPrompt: String) {
        val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()
        val timestamp = System.currentTimeMillis()
        val promptAction = PromptAction(userId, timestamp, action, history, newPrompt)
        uploadData("promptAction", promptAction)
    }
}
