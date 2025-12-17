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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flipp.dvm.sample.android.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    validFrom: Date?,
    validTo: Date?,
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
                    val dateFormat =
                        remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

                    Text(
                        text =
                            buildString {
                                validFrom?.let { append("Valid: ${dateFormat.format(it)} ") }
                                validTo?.let { append("To: ${dateFormat.format(it)}") }
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
        validFrom = Date(),
        validTo = Date(),
        onDvmClick = {},
        onSfmlClick = {},
    )
}
