package org.muilab.noti.summary.view

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.viewModel.SummaryViewModel
import java.lang.Float.max

@Composable
fun NotiDrawer(appContext: Context, sumViewModel: SummaryViewModel) {
    val notifications by sumViewModel.notifications.observeAsState()
    var drawerHeight = remember { mutableStateOf(Float.POSITIVE_INFINITY) }

    val padToPx = with(LocalDensity.current) {16.dp.toPx() / drawerHeight.value}
    val brush = Brush.verticalGradient(
        0.0f to MaterialTheme.colorScheme.surfaceVariant,
        padToPx to Color.Transparent,
        1 - padToPx  to Color.Transparent,
        1.0f to MaterialTheme.colorScheme.surfaceVariant
    )

    Box {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .onSizeChanged { drawerHeight.value = max(it.height.toFloat(), 1F) },
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            notifications?.forEach {
                item {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(MaterialTheme.colorScheme.secondary),
                                text = "${it.appName} / ${it.time}",
                                style = TextStyle(color = MaterialTheme.colorScheme.onSecondary)
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(Color.Transparent),
                                text = it.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(Color.Transparent),
                                text = it.content,
                                style = TextStyle(color = MaterialTheme.colorScheme.onSecondary)
                            )
                        }
                    }
                }
            }
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = brush
            )
        ) {

        }
    }
}