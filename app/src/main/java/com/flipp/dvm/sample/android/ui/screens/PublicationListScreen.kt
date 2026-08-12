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
import com.flipp.content.v2.model.CorePublication
import com.flipp.dvm.sample.android.UiState
import com.flipp.dvm.sample.android.navigation.Routes
import com.flipp.dvm.sample.android.ui.composables.PublicationCard
import com.flipp.dvm.sdk.android.external.toIdentifiers
import java.util.Date

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
    uiState: UiState<CorePublication>,
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
                items(publications) { publication ->
                    PublicationCard(
                        imageUrl = publication.details.imageUrl,
                        name = publication.details.name,
                        publicationId = publication.globalId,
                        description = publication.details.description,
                        // v2 dates are epoch seconds
                        validFrom = publication.dates.validFrom?.let { Date(it * 1000) },
                        validTo = publication.dates.validTo?.let { Date(it * 1000) },
                        tags = publication.tags,
                        renderTypes = publication.renderingMetadataTypes,
                        onRenderTypeClick = { renderType ->
                            navController.navigate(
                                Routes.PublicationScreen(
                                    identifiers = publication.toIdentifiers(storeCode),
                                    renderType = renderType.name,
                                    language = publication.language,
                                ),
                            )
                        },
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
