package org.muilab.noti.summary.view.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.ui.theme.DarkColorScheme
import org.muilab.noti.summary.util.logUserAction
import org.muilab.noti.summary.viewModel.SummaryViewModel
import java.lang.Float.max

@Composable
fun NotiDrawer(context: Context, sumViewModel: SummaryViewModel) {
    val notifications by sumViewModel.notifications.observeAsState()
    var drawerHeight by remember { mutableStateOf(Float.POSITIVE_INFINITY) }

    val state = rememberLazyListState()

    val padToPx = with(LocalDensity.current) {16.dp.toPx() / drawerHeight}
    val brush = Brush.verticalGradient(
        0.0f to MaterialTheme.colorScheme.surfaceVariant,
        padToPx to Color.Transparent,
        1 - padToPx  to Color.Transparent,
        1.0f to MaterialTheme.colorScheme.surfaceVariant
    )

    Box {

        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .onSizeChanged { drawerHeight = max(it.height.toFloat(), 1F) },
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
                        Column(modifier = Modifier.background(DarkColorScheme.secondary)) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                text = "${it.appName} / ${it.time}",
                                style = TextStyle(color = DarkColorScheme.onSecondary)
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(Color.Transparent),
                                text = it.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkColorScheme.onSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .background(Color.Transparent),
                                text = it.content,
                                style = TextStyle(color = DarkColorScheme.onSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().background(brush)) { }
    }

    state.apply {
        val notiCount = layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (!isScrollInProgress)
            logUserAction("scroll", "drawer", context, notiCount.toString())
    }
}