package org.muilab.noti.summary.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun MainSettingScreen(navController: NavHostController) {

    val uriHandler = LocalUriHandler.current

    MaterialTheme {
        Column {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(
                    items = SettingScreenItem.values()
                        .slice(1 until SettingScreenItem.values().size)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 3.dp, bottom = 3.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (it.name == "Feedback") {
                                    uriHandler.openUri("https://example.com")
                                } else if (it.name == "About") {
                                    uriHandler.openUri("https://github.com/noti-summary/NotiSummary")
                                } else if (it.name == "Recruitment") {
                                    uriHandler.openUri("https://example.com")
                                } else {
                                    navController.navigate(it.name)
                                }
                            },
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 12.dp).size(25.dp),
                                painter = painterResource(id = it.iconId),
                                contentDescription = stringResource(it.titleId),
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(0.93f),
                                text = stringResource(it.titleId),
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                stringResource(it.titleId)
                            )
                        }
                    }
                }
            }
        }
    }
}