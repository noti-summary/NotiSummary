package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.muilab.noti.summary.service.NotiUnit
import org.muilab.noti.summary.service.SummaryService
import org.muilab.noti.summary.view.home.SummaryResponse

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences =
        getApplication<Application>().getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)

    private val _notifications = MutableLiveData<List<NotiUnit>>()
    val notifications: LiveData<List<NotiUnit>> = _notifications

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    @SuppressLint("StaticFieldLeak")
    private lateinit var summaryService: SummaryService

    init {
        updateFromSharedPref()
        resetNotiDrawer()
    }

    private fun updateFromSharedPref() {
        val resultValue = sharedPreferences.getString(
            "resultValue", context.getString(SummaryResponse.HINT.message))
        _result.value = resultValue!!
    }

    fun setService(summaryService: SummaryService) {
        this.summaryService = summaryService
        if (summaryService.getStatusText() != context.getString(SummaryResponse.GENERATING.message)) {
            updateFromSharedPref()
            resetNotiDrawer()
        } else
            updateStatusText()
    }

    private fun resetNotiDrawer() {
        val notiDrawerJson = sharedPreferences.getString("notiDrawer", "")
        if (notiDrawerJson!!.isNotEmpty()) {
            val notiDrawerType = object : TypeToken<List<NotiUnit>>() {}.type
            _notifications.value = Gson()
                .fromJson<List<NotiUnit>>(notiDrawerJson, notiDrawerType)
                .sortedBy { it.drawerIndex }
        } else
            _notifications.value = listOf()
    }

    fun getSummaryText() {
        val intent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun updateStatusText() {
        if (!::summaryService.isInitialized)
            return

        Log.d("SummaryViewModel", "updateStatusText")
        val newStatus = summaryService.getStatusText()
        val activeNotifications = summaryService.getNotiInProcess()
        if (newStatus.isNotEmpty()) {
            _result.postValue(newStatus)
            if (newStatus == context.getString(SummaryResponse.GENERATING.message))
                _notifications.postValue(activeNotifications)
            else
                resetNotiDrawer()
        }
    }

    fun updateSummaryText(activeNotifications: ArrayList<NotiUnit>, isScheduled: Boolean) {
        if (activeNotifications.size > 0) {
            _result.postValue(context.getString(SummaryResponse.GENERATING.message))
            Log.d("sendToServer", "Trigger NotScheduled")
            viewModelScope.launch {
                val responseMessage = summaryService.sendToServer(activeNotifications, isScheduled)
                _result.postValue(responseMessage)
                resetNotiDrawer()
            }
        } else {
            _result.postValue(context.getString(SummaryResponse.NO_NOTIFICATION.message))
        }
    }
}