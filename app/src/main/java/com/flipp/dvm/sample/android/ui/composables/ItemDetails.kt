package com.flipp.dvm.sample.android.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flipp.dvm.sample.android.ui.theme.Typography
import com.flipp.dvm.sdk.android.external.models.Details
import com.flipp.dvm.sdk.android.external.models.Offer
import com.flipp.dvm.sdk.android.external.models.OfferDetails
import com.flipp.dvm.sdk.android.external.models.OfferType
import com.flipp.dvm.sdk.android.external.models.Pricing
import kotlinx.serialization.json.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A composable showing the details of an offer in a card
 * Displays v2 pricing information
 *
 * @param modifier the modifier
 * @param name the name of the offer
 * @param images a list of image urls for the item
 * @param id the item's unique id
 * @param validFrom the date the offer is valid from as a ISO-8601 string
 * @param validTo the date the offer expires as a ISO-8601 string
 * @param description a description of the item
 * @param disclaimer a disclaimer for this item
 * @param offerDetails  offer details
 * @param pricing pricing information for the item
 * @param details details of the item used for v2 information
 */
@Composable
fun ItemDetails(
    modifier: Modifier = Modifier,
    name: String?,
    images: List<String>,
    id: String?,
    validFrom: Date?,
    validTo: Date?,
    description: String?,
    disclaimer: String?,
    offerDetails: OfferDetails?,
    pricing: Pricing?,
    details: Details?,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(16.dp).verticalScroll(scrollState)) {
        if (images.size == 1) {
            AsyncImage(
                model = images.first(),
                contentDescription = name,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(200.dp),
            )
        } else if (images.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(images) { index, image ->
                    AsyncImage(
                        modifier = Modifier.height(200.dp),
                        model = image,
                        contentDescription = "$name $index",
                        contentScale = ContentScale.FillHeight,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        name?.let {
            Text(it, style = Typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        id?.let {
            Text(it, style = Typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        getV2PriceMapping(pricing, offerDetails).let {
            val v2PriceText =
                buildString {
                    if (it.prePrice.isNotEmpty()) {
                        append(it.prePrice)
                        append(" ")
                    }
                    append(it.price)
                }
            Row {
                Text(
                    v2PriceText,
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    style = Typography.titleLarge,
                )
                if (it.postPrice.isNotEmpty()) {
                    Text(" ${it.postPrice}")
                }
            }
        }
        getV2SaleStory(pricing, offerDetails, details).let {
            if (it.isNotEmpty()) {
                Text(it)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        validFrom?.let { vf ->
            validTo?.let { vt ->
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val vff = outputFormat.format(vf)
                val vtf = outputFormat.format(vt)
                Text("Valid: $vff - $vtf")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        description?.let {
            Text("Description", style = Typography.titleLarge)
            Text(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        disclaimer?.let {
            Text(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        details?.additionalInfo?.get("sku")?.content?.let {
            Text("SKU: $it")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Gets the v2 sale story of an [Offer]
 *
 * @param pricing the pricing information of the [Offer]
 * @param offerDetails the offer-details of the [Offer]
 * @param details the details of the [Offer]
 * @return the sale story
 */
fun getV2SaleStory(
    pricing: Pricing?,
    offerDetails: OfferDetails?,
    details: Details?,
): String {
    val labels = pricing?.labels
    var saleStory = offerDetails?.saleStory ?: ""
    val prePriceText = offerDetails?.prePriceText ?: ""

    // Save labels
    labels?.let {
        if (it.contains("show-save")) saleStory = "SAVE $saleStory"
    }

    // Qualifying Quantity
    if (pricing?.offerType == OfferType.BUY_X_GET_Y &&
        pricing.percentOff != null &&
        pricing.qualifyingQuantity != null &&
        pricing.rewardQuantity != null
    ) {
        if (saleStory.isNotEmpty()) {
            saleStory += " "
        }
        saleStory += "BUY ${pricing.qualifyingQuantity} GET ${pricing.rewardQuantity} ${pricing.percentOff}% OFF"
    } else if (pricing?.qualifyingQuantity != null &&
        !prePriceText.contains(Regex("Buy \\d+ Or More"))
    ) {
        if (saleStory.isNotEmpty()) {
            saleStory += " "
        }
        saleStory += "WHEN YOU BUY ${pricing.qualifyingQuantity}"
    }

    // Loyalty
    pricing?.loyaltyPoints?.let { loyaltyPoints ->
        if (saleStory.isNotEmpty()) {
            saleStory += ", "
        }
        saleStory += "PC Optimum $loyaltyPoints pts"
        details?.additionalInfo?.get("loyalty_points_story")?.content?.let { loyaltyPointsStory ->
            if (loyaltyPointsStory.isNotEmpty()) {
                saleStory += " $loyaltyPointsStory"
            }
        }
        details?.additionalInfo?.get("loyalty_points_value")?.content?.let { loyaltyValue ->
            saleStory += ", $$loyaltyValue Value"
        }
    }

    return saleStory
}

/**
 * Gets the price mapping for v2 details
 *
 * @param pricing the pricing information of the [Offer]
 * @param offerDetails offerDetails the offer-details of the [Offer]
 * @return the price mapping of the offer
 */
fun getV2PriceMapping(
    pricing: Pricing?,
    offerDetails: OfferDetails?,
): V2PriceMapping {
    val prePriceText = offerDetails?.prePriceText ?: ""
    var priceText = ""
    val postPriceText = offerDetails?.postPriceText ?: ""
    var wasPriceText = ""

    val priceRange = pricing?.salePriceRange ?: pricing?.priceRange
    val price = pricing?.salePrice ?: pricing?.price
    priceText =
        when {
            priceRange != null -> "$${"%.2f".format(priceRange.from)} - $${"%.2f".format(priceRange.to)}"
            price != null -> "$${"%.2f".format(price)}"
            pricing?.offerType == OfferType.PERCENT_OFF && pricing.percentOff != null -> "${pricing.percentOff}% OFF"
            pricing?.offerType == OfferType.AMOUNT_OFF && pricing.amountOff != null -> "$${pricing.amountOff} OFF"
            else -> priceText
        }

    val wasPriceRange = if (pricing?.salePriceRange != null) pricing.priceRange else null
    val wasPrice = if (pricing?.salePrice != null) pricing.price else null
    wasPriceText =
        when {
            wasPriceRange != null -> "$${"%.2f".format(wasPriceRange.from)} - $${"%.2f".format(wasPriceRange.to)}"
            wasPrice != null -> "$${"%.2f".format(wasPrice)}"
            else -> wasPriceText
        }

    return V2PriceMapping(
        prePrice = prePriceText.trim(),
        price = priceText.trim(),
        postPrice = postPriceText.trim(),
        wasPrice = wasPriceText.trim(),
    )
}

data class V2PriceMapping(
    val prePrice: String,
    val price: String,
    val postPrice: String,
    val wasPrice: String,
)

@Composable
@Preview
fun ItemDetailsPreview() {
    ItemDetails(
        name = "FRESH GRADE A TURKEYS",
        images = listOf<String>(""),
        id = "01J8ZBC550B9R8ZZZFD58C0TRT",
        pricing =
            Pricing(
                price = 2.49,
            ),
        offerDetails =
            OfferDetails(
                postPriceText = "/lb",
                prePriceText = "",
                saleStory = "BUY ONE GET ONE FREE",
            ),
        details =
            Details(
                additionalInfo =
                    mapOf(
                        Pair("sku", JsonPrimitive("20145891_KG")),
                    ),
            ),
        validFrom = Date(),
        validTo = Date(),
        description = "ALL SIZES\\nAVAILABLE 5.49/KG",
        disclaimer = "DISCLAIMER",
    )
}
