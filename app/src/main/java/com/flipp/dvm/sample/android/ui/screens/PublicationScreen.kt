package com.flipp.dvm.sample.android.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flipp.dvm.sample.android.MainViewModel
import com.flipp.dvm.sample.android.ui.composables.ItemDetails
import com.flipp.dvm.sdk.android.external.PublicationRendererDelegate
import com.flipp.dvm.sdk.android.external.FlippPublication
import com.flipp.dvm.sdk.android.external.PublicationError
import com.flipp.dvm.sdk.android.external.PublicationIdentifiers
import com.flipp.dvm.sdk.android.external.models.Offer
import com.flipp.dvm.sdk.android.external.models.RenderType

/**
 * A composable function that displays a publication screen with debug information
 *
 * @param modifier the modifier
 * @param identifiers values that uniquely identify a Publication
 * @param renderType the type of rendering method to use when displaying the publication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationScreen(
    modifier: Modifier = Modifier,
    identifiers: PublicationIdentifiers,
    renderType: RenderType,
    viewModel: MainViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.selectedOffer.value = null
    }

    val currentOffer by viewModel.selectedOffer.collectAsStateWithLifecycle()

    val onFinishLoadToast = Toast.makeText(LocalContext.current, "DvmRendererDelegate: onFinishLoad", Toast.LENGTH_SHORT)
    val onFailedToLoad = Toast.makeText(LocalContext.current, "DvmRendererDelegate: onFailedToLoad", Toast.LENGTH_SHORT)
    val onTap = Toast.makeText(LocalContext.current, "DvmRendererDelegate: onTap", Toast.LENGTH_SHORT)
    val onTapError = Toast.makeText(LocalContext.current, "DvmRendererDelegate: onTapError", Toast.LENGTH_SHORT)

    val hotSwapRenderType = rememberSaveable { mutableStateOf(renderType) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            if (hotSwapRenderType.value == RenderType.DVM) {
                hotSwapRenderType.value = RenderType.SFML
            } else {
                hotSwapRenderType.value = RenderType.DVM
            }
        }) { Text("Toggle Render Type") }
        FlippPublication(
            modifier = Modifier,
            identifiers = identifiers,
            renderType = hotSwapRenderType.value,
            delegate =
                object : PublicationRendererDelegate {
                    override fun onFinishLoad() {
                        onFinishLoadToast.show()
                    }

                    override fun onFailedToLoad(error: PublicationError) {
                        onFailedToLoad.show()
                        Log.d("onError", "onFailedToLoad: ${error.message}")
                    }

                    override fun onTap(offer: Offer) {
                        onTap.setText(offer.details?.name)
                        onTap.show()
                        viewModel.selectedOffer.value = offer
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
                        it.details?.imageUrl?.let { add(it) }
                        it.details?.additionalMedia?.mapNotNull { media -> media.url }
                            ?.let { addAll(it) }
                    }.toList()
                ItemDetails(
                    modifier = Modifier.fillMaxWidth(),
                    name = it.details?.name,
                    description = it.details?.description,
                    images = listOfImageUrls,
                    id = it.globalId,
                    pricing = it.pricing,
                    offerDetails = it.offerDetails,
                    details = it.details,
                    validFrom = it.dates?.validFrom,
                    validTo = it.dates?.validTo,
                    disclaimer = it.offerDetails?.disclaimer,
                )
            }
        }
    }
}
