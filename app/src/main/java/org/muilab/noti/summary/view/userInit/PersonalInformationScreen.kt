package org.muilab.noti.summary.view.userInit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.muilab.noti.summary.MainActivity
import org.muilab.noti.summary.util.TAG
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    onContinue: (Int, String, String) -> Unit
) {
    var age by remember { mutableStateOf("") }
    var ageIsError by rememberSaveable { mutableStateOf(false) }

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

    fun validateAge(age: String) {
        val ageInt = age.toIntOrNull()
        ageIsError = if (ageInt == null) {
            true
        } else {
            ageInt <= 0
        }
    }

    Box (Modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Text(
                text = "請填寫您的基本資料",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it; validateAge(age) },
                label = { Text("年齡") },
                colors = customTextFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                isError = ageIsError,
                supportingText = {
                    if (ageIsError) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "請輸入合法的年齡",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded, {}
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
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.Clear,
                            contentDescription = "clear text",
                            modifier = Modifier.clickable { country = "" }
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
                        expanded = query.isNotEmpty() && filteredCountries.isNotEmpty(),
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

            Button(
                    enabled = !ageIsError && gender.isNotEmpty() && country.isNotEmpty(),
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
