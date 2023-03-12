package org.muilab.noti.summary.view


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.firestore.FirestoreDocument
import org.muilab.noti.summary.database.firestore.documentStateOf
import org.muilab.noti.summary.model.UserCredit
import org.muilab.noti.summary.util.TAG

@Composable
fun HomeScreen(context: Context, lifecycleOwner: LifecycleOwner) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_700))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "我的摘要",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }

    Credit(context, lifecycleOwner)

    SubtractButton(context)

}

@Composable
fun Credit(context: Context, lifecycleOwner: LifecycleOwner) {
    val documentRef = Firebase.firestore.collection("user-free-credit").document("001")
    val (result) = remember { documentStateOf(documentRef, lifecycleOwner) }

    if (result is FirestoreDocument.Snapshot) {
        val item = result.snapshot.toObject<UserCredit>()!!

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "Daily Credit: ${item.credit} / 100")
            }
        }
    }

}

@Composable
fun SubtractButton(context: Context) {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp, end=25.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                val db = Firebase.firestore
                val docRef = db.collection("user-free-credit").document("001")
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val res = document.toObject<UserCredit>()!!
                            docRef
                                .update("credit", res.credit-1)
                                .addOnSuccessListener { Toast.makeText(context, "-1", Toast.LENGTH_LONG).show() }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
            },
        ) {
            Icon(painter = painterResource(id = com.google.android.gms.base.R.drawable.googleg_disabled_color_18), "", modifier = Modifier.size(50.dp).padding(3.dp))
        }
    }
}
