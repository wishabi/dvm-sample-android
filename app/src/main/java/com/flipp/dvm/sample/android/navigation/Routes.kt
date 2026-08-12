package com.flipp.dvm.sample.android.navigation

import android.os.Bundle
import androidx.navigation.NavType
import com.flipp.dvm.sdk.android.external.PublicationIdentifiers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface Routes {
    /**
     * The starting route containing the map and input screen
     */
    @Serializable
    object Initial

    /**
     * Screen that displays the publications for a storeCode and merchantId
     */
    @Serializable
    object PublicationsList

    /**
     * Screen that displays the FlippPublication with additional debugging information

     * @property identifiers Contains values to uniquely identify the Publication to load
     * @property renderType The name of the [com.flipp.content.v2.model.RenderingMetadataType] to
     * render the Publication with. Passed as a String because navigation routes only carry types
     * it knows how to serialize
     * @property language The language code to render the Publication in, e.g en, fr
     */
    @Serializable
    data class PublicationScreen(
        val identifiers: PublicationIdentifiers,
        val renderType: String,
        val language: String,
    ) {
        companion object {
            /**
             * This is used to serialize and deserialize the object from the composable route
             */
            val identifiersNavType =
                object : NavType<PublicationIdentifiers>(isNullableAllowed = false) {
                    override fun get(
                        bundle: Bundle,
                        key: String,
                    ): PublicationIdentifiers? {
                        return bundle.getParcelable(key)
                    }

                    override fun parseValue(value: String): PublicationIdentifiers {
                        return Json.decodeFromString(value)
                    }

                    override fun put(
                        bundle: Bundle,
                        key: String,
                        value: PublicationIdentifiers,
                    ) {
                        bundle.putParcelable(key, value)
                    }

                    override fun serializeAsValue(value: PublicationIdentifiers): String {
                        return Json.encodeToString(PublicationIdentifiers.serializer(), value)
                    }
                }
        }
    }
}
