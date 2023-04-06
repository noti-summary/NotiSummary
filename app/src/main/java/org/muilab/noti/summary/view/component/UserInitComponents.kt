package org.muilab.noti.summary.view.component

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.*

@Composable
fun PrivacyPolicyDialog(
    onAgree: () -> Unit
) {
    var agree by remember { mutableStateOf(false) }
    val privacyURL = "https://notisum.nycu.me" // TODO: Change in the future
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val privacyHeight = screenHeight * 3 / 5

    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.wrapContentSize(),
        title = {
            Text(text = "隱私權政策")
        },
        text = {
            Column {
                Box {
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                loadUrl(privacyURL)
                            }
                        },
                        modifier = Modifier.height(privacyHeight)
                    )
                }
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agree,
                        onCheckedChange = { agree = it }
                    )
                    Text(text = "我同意本APP之隱私權政策")
                }
            }
        },
        confirmButton = {
            Button(onClick = onAgree, enabled = agree) {
                Text(text = "Agree")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    onContinue: (Int, String, String) -> Unit
) {
    var age by remember { mutableStateOf("") }

    var gender by remember { mutableStateOf("") }
    val genderOptions = listOf("男性", "女性", "非二元性別", "不便透露")
    var genderExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()

    val countries = getListOfCountries()
    var query by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val filteredCountries = remember(countries, query) {
        countries.filter { query.isNotEmpty() && it.contains(query, ignoreCase = true) }
    }

    val customTextFieldColors = TextFieldDefaults.textFieldColors(
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Box (Modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "請填寫您的基本資料",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("年齡") },
                colors = customTextFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = {genderExpanded}
            ) {

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("性別") },
                    colors = customTextFieldColors,
                    readOnly = true,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = { genderExpanded = true }),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                    }
                )

                LaunchedEffect(isPressed){
                    if (isPressed)
                        genderExpanded = true
                }

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = {genderExpanded = false},
                ) {
                    genderOptions.forEach { g ->
                        DropdownMenuItem(onClick = {
                            gender = g
                            genderExpanded = false
                        }, text = { Text(g) })
                    }
                }
            }

            if (country.isNotEmpty()) {
                OutlinedTextField(
                    value = country,
                    onValueChange = {},
                    label = { Text("居住國家/地區") },
                    colors = customTextFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.Clear,
                            contentDescription = "clear text",
                            modifier = Modifier
                                .clickable {
                                    country = ""
                                }
                        )
                    }
                )
            } else {

                ExposedDropdownMenuBox(
                    expanded = query.isNotEmpty(),
                    onExpandedChange = {}
                ) {

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("居住國家/地區") },
                        colors = customTextFieldColors,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        trailingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") }
                    )

                    ExposedDropdownMenu(
                        expanded = query.isNotEmpty(),
                        onDismissRequest = {},
                    ) {
                        filteredCountries.forEach { c ->
                            DropdownMenuItem(onClick = {
                                query = ""
                                country = c
                            }, text = { Text(c) })
                        }
                    }
                }
            }

            if (age.toIntOrNull().let { it != null && it > 0 } && gender.isNotEmpty() && country.isNotEmpty()) {
                Button(
                    onClick = { onContinue(age.toInt(), gender, country) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text(text = "歡迎使用 NotiSummary !")
                }
            }
        }
    }
}

fun getListOfCountries(): ArrayList<String> {
    val flagOffset = 0x1F1E6
    val asciiOffset = 0x41
    val isoCountryCodes = Locale.getISOCountries()
    val countryListWithEmojis = ArrayList<String>()
    for (countryCode in isoCountryCodes) {
        val locale = Locale("", countryCode)
        val countryName = locale.displayCountry
        val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
        val flag = (String(Character.toChars(firstChar)) + String(Character.toChars(secondChar)))
        countryListWithEmojis.add("$flag $countryCode $countryName")
    }
    return countryListWithEmojis
}