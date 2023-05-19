package org.muilab.noti.summary.view.userInit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.R
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    onContinue: (Int, String, String, String) -> Unit
) {
    var birthYear by remember { mutableStateOf("") }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (1900..currentYear).toList().reversed()
    var birthYearExpanded by remember { mutableStateOf(false) }
    val birthYearInteractionSource = remember { MutableInteractionSource() }
    val birthYearPressed: Boolean by birthYearInteractionSource.collectIsPressedAsState()

    var gender by remember { mutableStateOf("") }
    val genderOptions = listOf(
        stringResource(R.string.male),
        stringResource(R.string.female),
        stringResource(R.string.non_binary),
        stringResource(R.string.not_to_state)
    )
    var genderExpanded by remember { mutableStateOf(false) }
    val genderInteractionSource = remember { MutableInteractionSource() }
    val genderPressed: Boolean by genderInteractionSource.collectIsPressedAsState()

    val countries = getListOfCountries()
    var query by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val filteredCountries = remember(countries, query) {
        countries.filter { query.isNotEmpty() && it.contains(query, ignoreCase = true) }
    }

    var source by remember { mutableStateOf("") }
    val sourceOptions = listOf(
        stringResource(R.string.related_groups),
        stringResource(R.string.social_media),
        stringResource(R.string.google_play),
        stringResource(R.string.friends_family),
        stringResource(R.string.others)
    )
    var sourceExpanded by remember { mutableStateOf(false) }
    val sourceInteractionSource = remember { MutableInteractionSource() }
    val sourcePressed: Boolean by sourceInteractionSource.collectIsPressedAsState()

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
                text = stringResource(R.string.fill_background),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            ExposedDropdownMenuBox(
                expanded = birthYearExpanded, {}
            ) {

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    label = { Text(stringResource(R.string.birth_year)) },
                    colors = customTextFieldColors,
                    readOnly = true,
                    interactionSource = birthYearInteractionSource,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = { birthYearExpanded = true }),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.birth_year))
                    }
                )

                LaunchedEffect(birthYearPressed){
                    if (birthYearPressed)
                        birthYearExpanded = true
                }

                ExposedDropdownMenu(
                    expanded = birthYearExpanded,
                    onDismissRequest = {birthYearExpanded = false},
                ) {
                    years.forEach { b ->
                        DropdownMenuItem(onClick = {
                            birthYear = b.toString()
                            birthYearExpanded = false
                        }, text = { Text(b.toString()) })
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = genderExpanded, {}
            ) {

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text(stringResource(R.string.gender)) },
                    colors = customTextFieldColors,
                    readOnly = true,
                    interactionSource = genderInteractionSource,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = { genderExpanded = true }),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.select_gender))
                    }
                )

                LaunchedEffect(genderPressed){
                    if (genderPressed)
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
                    label = { Text(stringResource(R.string.country)) },
                    colors = customTextFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_text),
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
                        label = { Text(stringResource(R.string.country)) },
                        colors = customTextFieldColors,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        trailingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_country)) }
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

            ExposedDropdownMenuBox(
                expanded = sourceExpanded, {}
            ) {

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text(stringResource(R.string.source)) },
                    colors = customTextFieldColors,
                    readOnly = true,
                    interactionSource = sourceInteractionSource,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = { sourceExpanded = true }),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.source))
                    }
                )

                LaunchedEffect(sourcePressed){
                    if (sourcePressed)
                        sourceExpanded = true
                }

                ExposedDropdownMenu(
                    expanded = sourceExpanded,
                    onDismissRequest = {sourceExpanded = false},
                ) {
                    sourceOptions.forEach { s ->
                        DropdownMenuItem(onClick = {
                            source = s
                            sourceExpanded = false
                        }, text = { Text(s) })
                    }
                }
            }

            Button(
                    enabled = birthYear.isNotEmpty() && gender.isNotEmpty() && country.isNotEmpty() && source.isNotEmpty(),
                    onClick = { onContinue(birthYear.toInt(), gender, country, source) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
            ) {
                Text(stringResource(R.string.welcome))
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
