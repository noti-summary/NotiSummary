package org.muilab.noti.summary.view.home


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import org.muilab.noti.summary.util.logUserAction
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel

@Composable
fun HomeScreen(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    sumViewModel: SummaryViewModel,
    promptViewModel: PromptViewModel
) {

    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()

    val (submitButtonState, setSubmitButtonState) = remember { mutableStateOf(SSButtonState.IDLE) }

    val drawerCardState = remember { mutableStateOf(true) }
    val summaryCardState = remember { mutableStateOf(true) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val minorHeight = (
        with(LocalDensity.current) {MaterialTheme.typography.bodyLarge.lineHeight.toDp()}
        + 32.dp + 50.dp + 110.dp
    )
    val maxMainHeight = screenHeight - minorHeight
    val titleHeight = with(LocalDensity.current) {
        MaterialTheme.typography.headlineSmall.lineHeight.toDp()
    }
    val collapseHeight = titleHeight + 16.dp
    val drawerCardHeight by animateDpAsState(
        targetValue = if (drawerCardState.value && summaryCardState.value)
            maxMainHeight / 2
        else if (drawerCardState.value)
            maxMainHeight - collapseHeight
        else
            collapseHeight,
        animationSpec = tween(durationMillis = 500)
    )
    val summaryCardHeight by animateDpAsState(
        targetValue = if (drawerCardState.value && summaryCardState.value)
            maxMainHeight / 2
        else if (summaryCardState.value)
            maxMainHeight - collapseHeight
        else
            collapseHeight,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .height(drawerCardHeight)
        ) {
            Column(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            drawerCardState.value = if (!summaryCardState.value) {
                                drawerCardState.value
                            } else {
                                if (!drawerCardState.value)
                                    logUserAction("cardExpand", "both", context)
                                else
                                    logUserAction("cardExpand", "summary", context)
                                !drawerCardState.value
                            }
                        }) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                        Text(
                            text = stringResource(R.string.my_notifications),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (!drawerCardState.value)
                            Icon(
                                painter = painterResource(id = R.drawable.expand_arrow),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(titleHeight)
                                    .padding(4.dp)
                            )
                        else if (summaryCardState.value)
                            Icon(
                                painter = painterResource(id = R.drawable.collapse_arrow),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(titleHeight)
                                    .padding(4.dp)
                            )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
                NotiDrawer(context, sumViewModel)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .height(summaryCardHeight)
        ) {
            Column(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            summaryCardState.value = if (!drawerCardState.value) {
                                summaryCardState.value
                            } else {
                                if (!summaryCardState.value)
                                    logUserAction("cardExpand", "both", context)
                                else
                                    logUserAction("cardExpand", "drawer", context)
                                !summaryCardState.value
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.my_summary),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        if (summaryCardState.value) {
                            Spacer(modifier = Modifier.padding(16.dp))
                            Credit(context, lifecycleOwner, userId)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (!summaryCardState.value)
                            Icon(
                                painter = painterResource(id = R.drawable.expand_arrow),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(titleHeight)
                                    .padding(4.dp)
                            )
                        else if (drawerCardState.value)
                            Icon(
                                painter = painterResource(id = R.drawable.collapse_arrow),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(titleHeight)
                                    .padding(4.dp)
                            )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
                SummaryCard(context, sumViewModel, promptViewModel, submitButtonState, setSubmitButtonState)
            }
        }
        SubmitButton(context, userId, sumViewModel, submitButtonState)
    }
}

@Composable
fun Credit(context: Context, lifecycleOwner: LifecycleOwner, userId: String) {

    val documentRef = Firebase.firestore.collection("user").document(userId)
    val (result) = remember { documentStateOf(documentRef, lifecycleOwner) }
    var displayText by remember { mutableStateOf("${context.getString(R.string.daily_quota)}：- / $maxCredit") }

    val sharedPref = context.getSharedPreferences("ApiPref", Context.MODE_PRIVATE)
    val userAPIKey = sharedPref.getString("userAPIKey", stringResource(R.string.system_key))!!

    if (userAPIKey == stringResource(R.string.system_key)) {
        if (result is FirestoreDocument.Snapshot) {
            displayText = if (result.snapshot.exists()) {
                val res = result.snapshot.toObject<UserCredit>()!!
                "${context.getString(R.string.daily_quota)}：${res.credit} / $maxCredit"
            } else {
                stringResource(SummaryResponse.NETWORK_ERROR.message)
            }
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun SubmitButton(
    context: Context,
    userId: String,
    sumViewModel: SummaryViewModel,
    submitButtonState: SSButtonState
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SSJetPackComposeProgressButtonMaterial3(
            type = SSButtonType.CIRCLE,
            width = 300.dp,
            height = 50.dp,
            onClick = {
                if (submitButtonState != SSButtonState.LOADING) {
                    val db = Firebase.firestore
                    val docRef = db.collection("user").document(userId)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                if (document.exists()) {
                                    val res = document.toObject<UserCredit>()!!
                                    if (res.credit > 0) {
                                        logUserAction("genSummary", "Submit", context)
                                        sumViewModel.getSummaryText()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.reached_limit),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        logUserAction("genSummary", "NoQuota", context)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }
                }
            },
            assetColor = MaterialTheme.colorScheme.onPrimary,
            text = stringResource(R.string.generate_summary),
            buttonState = submitButtonState
        )
    }
}