# Flipp Platform SDK Sample App

## Table of Contents

- [About the SDK](#about)
- [Key Types](#key-types)
- [Quick Start](#quick-start)
- [How to Integrate the SDK](#how-to)
- [Upgrading from 1.x to 2.x](#upgrading)

## About the SDK <a name="about"></a>
The Flipp Platform SDK allows mobile retailer apps to render publications in two
formats: in traditional print form (SFML) or in Digital Visual Merchandising
form (DVM).

The DVM format renders publications in a dynamic way that maintains
responsiveness and merchandising flexibility, while also providing a host of
features to allow users to interact with offers.

This sample app targets SDK version **2.1.0**.

## Key Types <a name="key-types"></a>

The models below come from `com.flipp:content` (package `com.flipp.content.v2.model`), which
arrives with the SDK. They are the types the SDK's API is expressed in.

| Type | What it is |
| --- | --- |
| **`CorePublication`** | The *metadata* for one publication. A Publication is what is rendered on screen, either in the traditional print format (SFML) or the new digital format (DVM). |
| **`Offer`** | A purchasable deal inside a publication. Carries pricing information, product information, and other relevant data. |
| **`Promotion`** | A non-purchasable merchandising element inside a publication — a banner, a link, a call-out. |

Entities such as CorePublications, Offers and Promotions all carry a `globalId`, which is their unique identifier among all the content.

## Quick Start <a name="quick-start"></a>
1. Clone this repo
2. Open `dvm-sample-android` in Android Studio
3. Add the Artifactory credentials provided by Flipp to `~/.gradle/gradle.properties` (see
   [Repository](#repository))
4. Add your `clientToken` provided by Flipp in `DvmApplication.kt`
5. Build and run the app

## How to Integrate the SDK <a name="how-to"></a>

### Requirements
The SDK is built against JVM target 17 and its dependencies require `compileSdk 36`,
so the consuming module must use at least:

```kts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

android {
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```

Your Kotlin version must be **2.2.20 or newer** — the SDK is compiled with 2.2.20, and older
compilers reject its metadata.

`compileSdk 36` also sets a floor on the build tooling: **AGP 8.9 or newer** and **Gradle
8.11.1 or newer**. AGP 8.7 and earlier cannot compile against SDK 36 and will fail to resolve
the SDK's dependencies.

Ensure you've added internet permissions to your `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

### Repository <a name="repository"></a>
Add the credentials provided by Flipp to `~/.gradle/gradle.properties` so they are never
committed to source control:
```properties
artifactory_user=[PROVIDED BY FLIPP]
artifactory_password=[PROVIDED BY FLIPP]
```

Then declare the repository in `settings.gradle.kts`:
```kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://flipplib.jfrog.io/artifactory/dvm-sdk-android")
            credentials {
                username = providers.gradleProperty("artifactory_user").orNull ?: ""
                password = providers.gradleProperty("artifactory_password").orNull ?: ""
            }
        }
    }
}
```

### Dependencies
Declare the SDK in `libs.versions.toml` and `build.gradle.kts`
```toml
[versions]
dvmSdk = "2.1.0"

[libraries]
dvm-sdk = { module = "com.flipp:dvm-sdk", version.ref = "dvmSdk" }
```

```kts
implementation(libs.dvm.sdk)
```

Some protobuf artifacts that arrive transitively ship their `.proto` sources in both the Kotlin
and Java variants, so packaging needs a duplicate-resource rule:
```kts
android {
    packaging {
        resources {
            pickFirsts += "**/*.proto"
        }
    }
}
```

### Initializing the SDK
In your application class begin by initializing the SDK. Here you will provide your
`clientToken` provided by Flipp.
**Note**: this must be done before any other functionality of the SDK will work - the SDK
will throw an exception otherwise
```kotlin
import android.app.Application
import com.flipp.dvm.sdk.android.external.DvmSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DvmSdk.initialize(
            clientToken = "[PROVIDED BY FLIPP]",
            context = this,
            userId = "id-of-the-user-launching-the-app", // OPTIONAL
        )
    }
}
```
- If a new user begins using the app without restarting (e.g logs out), then the user id can
  be reassigned via `DvmSdk.config.identifiers.userId`

### Fetching Publications
The publication list is fetched with the `PublicationRepository` from `com.flipp:content`.
Construct it with the client token and curator endpoint that the SDK resolved during
initialization.

Both methods are `suspend`. Note that they do **not** throw — a network error, an invalid
`clientToken` or any other failure comes back as an empty list, indistinguishable from a store
that genuinely has no publications. Bear that in mind when deciding what to show the user.

```kotlin
import com.flipp.content.v2.network.repository.PublicationRepository

private val publicationsRepository =
    PublicationRepository(
        authorization = DvmSdk.config.clientToken,
        baseUrl = DvmSdk.config.endpoints.curator,
    )

// Fetch Publications for a known merchant + store
val publications: List<CorePublication> =
    publicationsRepository.getPublicationsByStore(
        merchantId = merchantId,
        storeCode = storeCode,
        language = "en",
    )

// Fetch Publications for a postal/ZIP code region
val publications: List<CorePublication> =
    publicationsRepository.getPublicationsByPostalCode(
        merchantId = merchantId,
        postalCode = postalCode,
        countryCode = countryCode,
        language = "en",
    )
```

### Rendering Publications
`FlippPublication` is a Jetpack Compose composable that renders a Publication. It is
identified by a `PublicationIdentifiers` — either a `Store` or a `PostalCode` — and a
`RenderingMetadataType` from the Publication's `renderingMetadataTypes`.

```kotlin
/**
 * @param modifier The modifier to be applied to the composable
 * @param identifiers The identifiers for the publication
 * @param renderType The method of rendering the Publication
 * @param language The language code, e.g en, fr
 * @param longPressDurationMs The duration of a long-press action in ms, null if long-press
 * support is not needed
 * @param caching True if the Publication should be cached, false otherwise
 * @param linkedOfferId The id of an Offer to guarantee is linked into the Publication
 * @param delegate The delegate for handling Publication events
 * @param header Optional composable rendered above the publication
 * @param footer Optional composable rendered below the publication
 */
@Composable
fun FlippPublication(
    modifier: Modifier = Modifier,
    identifiers: PublicationIdentifiers,
    renderType: RenderingMetadataType,
    language: String = "en",
    longPressDurationMs: Int? = null,
    caching: Boolean = true,
    linkedOfferId: String? = null,
    delegate: PublicationRendererDelegate? = null,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
)
```

A `CorePublication` is turned into identifiers with the `toIdentifiers` extension:
```kotlin
FlippPublication(
    identifiers = publication.toIdentifiers(storeCode),
    renderType = RenderingMetadataType.DVM_TEMPLATE,
    language = publication.language,
    delegate = delegate,
)
```

When a `header` and/or `footer` is provided they scroll together with the publication as one
continuous surface, and pinch-zoom on the publication is disabled.

### Delegate Functions
A delegate can also be provided which allows you to handle specific events related to the
Publication. Every method has a no-op default — override only the events you care about.
```kotlin
interface PublicationRendererDelegate {
    /** Called once the Publication successfully loads */
    fun onFinishLoad(controller: PublicationController, legacyIdMap: Map<Long, String>?)

    /**
     * Called if there is an error loading the Publication. This is terminal for the
     * FlippPublication instance that reported it — to retry, remove the composable and add
     * it back so a new Publication session is started.
     */
    fun onFailedToLoad(error: PublicationError)

    /** Called if the user taps an Offer */
    fun onTap(offer: Offer)

    /** Called if the user taps a Promotion */
    fun onTap(promotion: Promotion)

    /** Called if the user taps a take-to-merchant (external link) target */
    fun onTap(url: String)

    /** Called if there was an error after tapping an Offer */
    fun onTapError(error: String)

    /**
     * Called if the user long-presses an Offer. Only fires when longPressDurationMs was
     * passed to FlippPublication
     */
    fun onLongPress(offer: Offer)

    /** Called if there was an error after long-pressing an Offer */
    fun onLongPressError(error: String)

    /** Called when the user scrolls within the Publication */
    fun onScroll(flyerHeightPx: Int, viewportBottomOffsetPx: Int)

    /** Called after a requested scroll-to action completes for an Offer target */
    fun onScrollToFinished(offer: Offer?)

    /** Called after a requested scroll-to action completes for a Promotion target */
    fun onScrollToFinished(promotion: Promotion?)

    /** Called when the user sees more than 50% of an Offer on screen */
    fun onImpressionOffers(offers: List<String>)

    /** Called when the user sees more than 50% of a Promotion on screen */
    fun onImpressionPromotions(promotions: List<String>)

    /** Called once the visit counts as engaged: the user interacts, or 6 seconds elapse */
    fun onEngagedVisit()

    /**
     * Called when the Publication's hydration returns post-processing strategy results,
     * e.g. the offer-linking strategy used when a linkedOfferId is provided
     */
    fun onStrategyResults(results: List<StrategyResult>)
}
```

### Publication Controller
A `PublicationController` is a way to perform actions on a rendered Publication. It is provided in
`PublicationRendererDelegate.onFinishLoad` and stays valid for the lifetime of that
Publication.
```kotlin
interface PublicationController {
    /** Scrolls to an item given a globalId */
    fun scrollTo(globalId: String)

    /**
     * Registers annotation definitions. An Annotation's type must be registered before it
     * can be applied with addAnnotations
     */
    fun registerAnnotations(annotations: List<Annotation>)

    /** Adds annotations of a specific type to the given global IDs */
    fun addAnnotations(type: AnnotationType, globalIds: Set<String>)

    /** Removes annotations of a specific type from the given global IDs */
    fun removeAnnotations(type: AnnotationType, globalIds: Set<String>)

    /**
     * Notifies the renderer whether the Publication is currently visible to the user.
     * Becoming visible resets the engaged-visit timer
     */
    fun setVisibility(isVisible: Boolean)
}
```

#### Annotations
An `Annotation` is an image drawn on top of individual items in a rendered Publication.

Define one with a `type` you choose, the image to draw, and the corner it anchors to. `width` and
`height` are optional:

```kotlin
val circleAnnotation = Annotation(
    type = "circleAnnotation",
    imageUrl = "[YOUR IMAGE URL]",
    position = AnnotationPosition.TOP_RIGHT,
)
```

Register the definitions once the Publication has loaded, then apply them to items by `globalId`:

```kotlin
override fun onFinishLoad(
    controller: PublicationController,
    legacyIdMap: Map<Long, String>?,
) {
    controller.registerAnnotations(listOf(circleAnnotation))
    controller.addAnnotations("circleAnnotation", listOf("id1", "id2", "id3"))
    // you can also remove them with controller.removeAnnotations("circleAnnotation", listOf("id1", "id2", "id3"))
}
```

A few things worth knowing:

- Register per Publication. Every `FlippPublication` gets its own controller with no annotations
  applied, so registration belongs in `onFinishLoad`.
- You do not need to re-apply after a configuration change — the SDK re-registers and re-applies
  everything itself when the page reloads.
- The SDK does not persist annotations beyond the life of the Publication. Which items are clipped
  is your app's state to store and re-apply on the next load.
- More than one type can apply to the same item, so a "coupon" and a "clipped" badge can sit on
  one Offer together.
- `imageUrl` is loaded by the renderer, so it has to be reachable from the device rather than
  bundled in your app.

## Upgrading from 1.x to 2.x <a name="upgrading"></a>
2.0 was a breaking release. The main changes:

| 1.x | 2.x |
| --- | --- |
| `com.flipp.dvm.sdk.android.external.models.*` | `com.flipp.content.v2.model.*` (from `com.flipp:content`) |
| `Publication` | `CorePublication` |
| `Pricing` | `OfferPricing`, with `Float` instead of `Double` prices |
| `RenderType.DVM` / `RenderType.SFML` | `RenderingMetadataType.DVM_TEMPLATE`, `DVM_STATIC`, `SFML_VERTICAL`, `SFML_HORIZONTAL` |
| `Publication.renderingTypes` | `CorePublication.renderingMetadataTypes` |
| `Dates.validFrom` / `validTo` as `Date` | epoch seconds as `Long?` |
| `Details.additionalInfo` values as `JsonPrimitive` | `JsonElement` — read with `.jsonPrimitive.contentOrNull` |
| SDK's own `PublicationRepository.getPublications` returning `Result<PublicationList>` | content's `PublicationRepository.getPublicationsByStore` / `getPublicationsByPostalCode` returning `List<CorePublication>` and throwing on failure |
| `onImpression(offers)` | `onImpressionOffers(offers)` and `onImpressionPromotions(promotions)` |

Also new in 2.x: `PublicationIdentifiers.PostalCode` for locating a Publication without a
store, `onTap(promotion)` and `onTap(url)` delegate callbacks, `onStrategyResults`, the
`linkedOfferId` parameter, and the `header`/`footer` slots on `FlippPublication`.
