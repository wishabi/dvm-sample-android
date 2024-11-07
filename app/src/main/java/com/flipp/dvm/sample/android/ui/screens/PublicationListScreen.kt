package com.flipp.dvm.sample.android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.flipp.dvm.sample.android.UiState
import com.flipp.dvm.sample.android.navigation.Routes
import com.flipp.dvm.sample.android.ui.composables.PublicationCard
import com.flipp.dvm.sdk.android.external.models.Publication
import com.flipp.dvm.sdk.android.external.models.RenderType
import com.flipp.dvm.sdk.android.external.toIdentifiers

/**
 * A composable function that displays a list of publications
 *
 * @param modifier the modifier
 * @param uiState the state of the UI after fetching Publications
 * @param storeCode the store code
 * @param navController the navigation controller
 */
@Composable
fun PublicationListScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<Publication>,
    storeCode: String,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    LazyColumn(
        modifier =
            modifier
                .padding(12.dp)
                .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (uiState) {
            is UiState.Loading -> {
                item {
                    FullScreenLoading()
                }
            }

            is UiState.Failed -> {
                item {
                    LaunchedEffect(uiState) {
                        Toast.makeText(
                            context,
                            uiState.error,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    Text(
                        "Error",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            is UiState.Empty ->
                item {
                    Text(
                        "No publications found",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

            is UiState.Success -> {
                val publications = uiState.data
                item {
                    Text(
                        "${publications.size} publication(s)",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                items(publications) {
                    val sfmlClickHandler =
                        if (it.renderingTypes.contains(RenderType.SFML)) {
                            {
                                navController.navigate(
                                    Routes.PublicationScreen(
                                        it.toIdentifiers(
                                            storeCode,
                                        ),
                                        RenderType.SFML,
                                    ),
                                )
                            }
                        } else {
                            null
                        }
                    val dvmClickHandler =
                        if (it.renderingTypes.contains(RenderType.DVM)) {
                            {
                                navController.navigate(
                                    Routes.PublicationScreen(
                                        it.toIdentifiers(
                                            storeCode,
                                        ),
                                        RenderType.DVM,
                                    ),
                                )
                            }
                        } else {
                            null
                        }
                    PublicationCard(
                        imageUrl = it.details.imageUrl,
                        name = it.details.name,
                        publicationId = it.globalId,
                        description = it.details.description,
                        validFrom = it.dates.validFrom,
                        validTo = it.dates.validTo,
                        onSfmlClick = sfmlClickHandler,
                        onDvmClick = dvmClickHandler,
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
    ) {
        CircularProgressIndicator()
    }
}
