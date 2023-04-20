package org.muilab.noti.summary.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.muilab.noti.summary.database.room.UserActionDao
import org.muilab.noti.summary.database.room.UserActionDatabase
import org.muilab.noti.summary.model.UserAction

fun insertUserAction(type: String, actionName: String, metadata: String, context: Context) {

    Log.d("UserAction", "insert")

    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val time = System.currentTimeMillis()
    val userAction = UserAction(userId, time, type, actionName, metadata)

    CoroutineScope(Dispatchers.IO).launch {
        val userActionDatabase = UserActionDatabase.getInstance(context)
        val userActionDao = userActionDatabase.userActionDao()
        userActionDao.insertAction(userAction)
        if (userActionDao.getActionsCount() >= 100 || type == "lifeCycle")
            uploadUserAction(userActionDao)
    }
}

fun uploadUserAction(userActionDao: UserActionDao) {

    Log.d("UserAction", "upload")

    val db = Firebase.firestore
    val batch = db.batch()

    val userActions = userActionDao.getAllActions()
    userActions.forEach { userAction ->
        val docRef = db.collection("user_action").document(userAction.primaryKey)
        batch.set(docRef, userAction)
    }
    batch.commit()
        .addOnSuccessListener {
            Log.d("UserAction", "Uploaded user actions to Firestore")
            CoroutineScope(Dispatchers.IO).launch {
                userActionDao.deleteAll()
            }
        }
        .addOnFailureListener {
            Log.d("UserAction", "Failed to upload to Firestore")
        }
}