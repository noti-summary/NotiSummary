package org.muilab.noti.summary.view


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import org.muilab.noti.summary.util.TAG
import org.muilab.noti.summary.viewModel.SummaryViewModel

@Composable
fun HomeScreen(context: Context, lifecycleOwner: LifecycleOwner, sumViewModel: SummaryViewModel) {

    val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()

    val (submitButtonState, setSubmitButtonState) = remember { mutableStateOf(SSButtonState.IDLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_700))
            .wrapContentSize(Alignment.Center)
    ) {
        Credit(lifecycleOwner, userId)
        SummaryCard(sumViewModel, setSubmitButtonState)
        SubmitButton(context, userId, sumViewModel, submitButtonState, setSubmitButtonState)
    }

}

@Composable
fun Credit(lifecycleOwner: LifecycleOwner, userId: String) {

    val documentRef = Firebase.firestore.collection("user-free-credit").document(userId)
    val (result) = remember { documentStateOf(documentRef, lifecycleOwner) }

    if (result is FirestoreDocument.Snapshot) {
        val item = result.snapshot.toObject<UserCredit>()!!

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "Daily Credit: ${item.credit} / $maxCredit")
            }
        }
    }

}

@Composable
fun SubmitButton(context: Context, userId: String, sumViewModel: SummaryViewModel, submitButtonState: SSButtonState, setSubmitButtonState: (SSButtonState) -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SSJetPackComposeProgressButtonMaterial3(
            type = SSButtonType.CIRCLE,
            width = 300.dp,
            height = 50.dp,
            onClick = {
                if (submitButtonState != SSButtonState.LOADING){
                    val db = Firebase.firestore
                    val docRef = db.collection("user-free-credit").document(userId)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val res = document.toObject<UserCredit>()!!
                                if(res.credit > 0){
                                    sumViewModel.getSummaryText()
                                } else{
                                    Toast.makeText(context, "已達到每日摘要次數上限", Toast.LENGTH_LONG).show()
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
