package com.flipp.dvm.sample.android.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flipp.content.v2.model.Offer
import com.flipp.content.v2.model.Promotion
import com.flipp.content.v2.model.RenderingMetadataType
import com.flipp.dvm.sample.android.MainViewModel
import com.flipp.dvm.sample.android.ui.composables.ItemDetails
import com.flipp.dvm.sdk.android.external.FlippPublication
import com.flipp.dvm.sdk.android.external.PublicationController
import com.flipp.dvm.sdk.android.external.PublicationError
import com.flipp.dvm.sdk.android.external.PublicationIdentifiers
import com.flipp.dvm.sdk.android.external.PublicationRendererDelegate
import java.util.Date

/**
 * A composable function that displays a publication screen with debug information
 *
 * @param modifier the modifier
 * @param identifiers values that uniquely identify a Publication
 * @param renderType the name of the [RenderingMetadataType] to render the Publication with
 * @param language the language to render the Publication in, e.g en, fr
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationScreen(
    modifier: Modifier = Modifier,
    identifiers: PublicationIdentifiers,
    renderType: String,
    language: String = "en",
    viewModel: MainViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.selectedOffer.value = null
    }

    val context = LocalContext.current
    val currentOffer by viewModel.selectedOffer.collectAsStateWithLifecycle()

    val onFinishLoadToast = Toast.makeText(context, "DvmRendererDelegate: onFinishLoad", Toast.LENGTH_SHORT)
    val onFailedToLoad = Toast.makeText(context, "DvmRendererDelegate: onFailedToLoad", Toast.LENGTH_SHORT)
    val onTap = Toast.makeText(context, "DvmRendererDelegate: onTap", Toast.LENGTH_SHORT)
    val onTapError = Toast.makeText(context, "DvmRendererDelegate: onTapError", Toast.LENGTH_SHORT)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FlippPublication(
            modifier = Modifier.fillMaxSize(),
            identifiers = identifiers,
            renderType = RenderingMetadataType.valueOf(renderType),
            language = language,
            delegate =
                object : PublicationRendererDelegate {
                    override fun onFinishLoad(
                        controller: PublicationController,
                        legacyIdMap: Map<Long, String>?,
                    ) {
                        onFinishLoadToast.show()
                    }

                    override fun onFailedToLoad(error: PublicationError) {
                        onFailedToLoad.show()
                        Log.d("onError", "onFailedToLoad: ${error.message}")
                    }

                    override fun onTap(offer: Offer) {
                        onTap.setText(offer.details.name)
                        onTap.show()
                        viewModel.selectedOffer.value = offer
                    }

                    override fun onTap(promotion: Promotion) {
                        onTap.setText(promotion.details?.name)
                        onTap.show()
                    }

                    // Called when the user taps a take-to-merchant target within the Publication
                    override fun onTap(url: String) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                },
                            )
                        }.onFailure {
                            Log.w("onTap", "Failed to open url: $url", it)
                        }
                    }

                    override fun onTapError(error: String) {
                        onTapError.show()
                    }
                },
        )
        currentOffer?.let {
            val state =
                rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                )
            ModalBottomSheet(sheetState = state, onDismissRequest = {
                viewModel.selectedOffer.value = null
            }) {
                val listOfImageUrls =
                    mutableListOf<String>().apply {
                        it.details.imageUrl?.let { add(it) }
                        addAll(it.details.additionalMedia.mapNotNull { media -> media.url })
                    }.toList()
                ItemDetails(
                    modifier = Modifier.fillMaxWidth(),
                    name = it.details.name,
                    description = it.details.description,
                    images = listOfImageUrls,
                    id = it.globalId,
                    pricing = it.pricing,
                    offerDetails = it.offerDetails,
                    details = it.details,
                    // v2 dates are epoch seconds
                    validFrom = it.dates?.validFrom?.let { seconds -> Date(seconds * 1000) },
                    validTo = it.dates?.validTo?.let { seconds -> Date(seconds * 1000) },
                    disclaimer = it.offerDetails.disclaimer,
                )
            }
        }
    }
}
