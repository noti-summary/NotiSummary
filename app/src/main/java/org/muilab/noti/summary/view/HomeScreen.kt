package org.muilab.noti.summary.view


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButtonMaterial3
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.firestore.FirestoreDocument
import org.muilab.noti.summary.database.firestore.documentStateOf
import org.muilab.noti.summary.maxCredit
import org.muilab.noti.summary.model.UserCredit
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import org.muilab.noti.summary.util.TAG
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel

@Composable
fun HomeScreen(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    sumViewModel: SummaryViewModel,
    promptViewModel: PromptViewModel
) {

    val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()

    val (submitButtonState, setSubmitButtonState) = remember { mutableStateOf(SSButtonState.IDLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_700))
            .wrapContentSize(Alignment.Center)
    ) {
        Credit(context, lifecycleOwner, userId)
        NotiDrawer(context, sumViewModel)
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }
        SummaryCard(sumViewModel, submitButtonState, setSubmitButtonState)
        SubmitButton(context, userId, sumViewModel, promptViewModel, submitButtonState)
    }

}

@Composable
fun Credit(context: Context, lifecycleOwner: LifecycleOwner, userId: String) {

    val documentRef = Firebase.firestore.collection("user-free-credit").document(userId)
    val (result) = remember { documentStateOf(documentRef, lifecycleOwner) }
    var displayText by remember { mutableStateOf("每日額度：- / $maxCredit") }

    val sharedPref = context.getSharedPreferences("ApiPref", Context.MODE_PRIVATE)
    val userAPIKey = sharedPref.getString("userAPIKey", "default")!!

    if (userAPIKey == "default") {
        if (result is FirestoreDocument.Snapshot) {
            if (result.snapshot.exists()) {
                val res = result.snapshot.toObject<UserCredit>()!!
                displayText = "每日額度：${res.credit} / $maxCredit"
            } else {
                displayText = "${SummaryResponse.NETWORK_ERROR.message}，並重新啟動 app"
            }
        }
    } else {
        displayText = "正在使用您的 API 金鑰：sk-****" + userAPIKey!!.takeLast(4)
    }

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = displayText)
        }
    }

}

@Composable
fun CurrentPrompt(curPrompt: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)) {
        Text("當前摘要提示句：$curPrompt")
    }
}

@Composable
fun SubmitButton(
    context: Context,
    userId: String,
    sumViewModel: SummaryViewModel,
    promptViewModel: PromptViewModel,
    submitButtonState: SSButtonState
) {

    val prompt = promptViewModel.getCurPrompt()

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SSJetPackComposeProgressButtonMaterial3(
            type = SSButtonType.CIRCLE,
            width = 300.dp,
            height = 50.dp,
            onClick = {
                if (submitButtonState != SSButtonState.LOADING) {
                    val db = Firebase.firestore
                    val docRef = db.collection("user-free-credit").document(userId)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                if(document.exists()){
                                    val res = document.toObject<UserCredit>()!!
                                    if(res.credit > 0) {
                                        sumViewModel.getSummaryText(prompt)
                                    } else {
                                        Toast.makeText(context, "已達到每日摘要次數上限", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }
                }
            },
            assetColor = Color.Black,
            text = "產生摘要",
            buttonState = submitButtonState
        )
    }
}

@Composable
fun NotiDrawer(appContext: Context, sumViewModel: SummaryViewModel) {
    val notifications by sumViewModel.notifications.observeAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val HEIGHT_RATIO = 0.3f
    val cardHeight = (screenHeight * HEIGHT_RATIO).toInt()

    LazyColumn(modifier = Modifier.fillMaxWidth().height(cardHeight.dp).background(Color(0, 23, 53))) {
        notifications?.forEach {
            item {

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth().padding(horizontal = 10.dp),
                            text = "${it.appName} / ${it.time}"
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth().padding(horizontal = 10.dp),
                            text = it.title,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth().padding(horizontal = 10.dp),
                            text = it.content
                        )
                    }
                }
        }
    }
}