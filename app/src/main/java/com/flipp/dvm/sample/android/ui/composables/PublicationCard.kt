package com.flipp.dvm.sample.android.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flipp.dvm.sample.android.ui.theme.Typography

/**
 * A card representing a Publication with all its information
 *
 * @param modifier the modifier
 * @param imageUrl the preview image of the Publication
 * @param name the name of the Publication
 * @param publicationId the id of the Publication
 * @param description the description of the Publication
 * @param validFrom the date the offer is valid from as a ISO-8601 string
 * @param validTo the date the offer is expires from as a ISO-8601 string
 * @param onSfmlClick called when the user clicks the SFML button
 * @param onDvmClick called when the user clicks the DVM button
 */
@Composable
fun PublicationCard(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    name: String?,
    publicationId: String,
    description: String?,
    validFrom: String?,
    validTo: String?,
    onSfmlClick: (() -> Unit)?,
    onDvmClick: (() -> Unit)?,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (imageUrl != null) {
                AsyncImage(
                    modifier = Modifier.size(120.dp),
                    model = imageUrl,
                    contentDescription = "image",
                )
            } else {
                Box(modifier = Modifier.size(120.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = name ?: "",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = publicationId,
                    style = Typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(text = description ?: "", style = Typography.bodySmall)
                if (validFrom != null || validTo != null) {
                    Text(
                        text =
                            buildString {
                                validFrom?.let { append("Valid: ${it.take(10)} ") }
                                validTo?.let { append("To: ${it.take(10)}") }
                            },
                        style = Typography.bodySmall,
                    )
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(enabled = onSfmlClick != null, onClick = onSfmlClick ?: {}) {
                Text("View SFML", textAlign = TextAlign.Center)
            }
            Button(enabled = onDvmClick != null, onClick = onDvmClick ?: {}) {
                Text("View DVM", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
@Preview
fun PublicationCardPreview() {
    PublicationCard(
        imageUrl = "https://f.wishabi.net/flyers/6858328/first_page_thumbnail_400w/1727715764.jpg",
        name = "Weekly Flyer - Valid Thursday, September 26 - Wednesday, October 2",
        description = "Weekly Flyer - Valid Thursday, September 26 - Wednesday, October 2 - LSL-2",
        publicationId = "01J91Y4TX5BBZ4ZVK6JKCYGPAA",
        validFrom = "2024-09-26T04:00:00Z",
        validTo = "2024-10-03T03:59:59Z",
        onDvmClick = {},
        onSfmlClick = {},
    )
}
