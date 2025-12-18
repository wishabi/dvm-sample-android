# Flipp Platform SDK Sample App

## Table of Contents

- [About the SDK](#about)
- [Quick Start](#quick-start)
- [How to Integrate the SDK](#how-to)

## About the SDK <a name="about"></a>
The Flipp Platform SDK allows mobile retailer apps to render publications in two
formats: in traditional print form (SFML) or in Digital Visual Merchandising
form (DVM).

The DVM format renders publications in a dynamic way that maintains
responsiveness and merchandising flexibility, while also providing a host of
features to allow users to interact with offers.

## Quick Start <a name="quick-start"></a>
1. Clone this repo
2. Open `dvm-sample-android` in Android Studio
3. Add your JFrog credentials in `settings.gradle.kts`
4. Add your `clientToken` provided by Flipp in `DvmApplication.kt`
5. Build and run the app

## How to Integrate the SDK <a name="how-to"></a>
First, ensure you've added internet permissions to your `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

Then add `maven` as a repository source with the artifactory url below and fill in your JFrog account credentials provided by Flipp.
```kts
repositories {
    maven {
        url = uri("https://flipplib.jfrog.io/artifactory/dvm-sdk-android")
        credentials {
            username = "[PROVIDED BY FLIPP]"
            password = "[PROVIDED BY FLIPP]"
        }
    }
}
```
Then add it to your build configuration in `libs.version.toml` and `build.gradle.kts`
```toml
[versions]
dvmSdk = "1.4.0"  # latest version

[libraries]
dvm-sdk = { module = "com.flipp:dvm-sdk", version.ref = "dvmSdk" }
```

```kts
implementation(libs.dvm.sdk)
```
You'll also have to update your build.gradle
### Initializing the SDK
In your application class begin by initializing the SDK. Here you will provide your `clientToken` provided by Flipp.  
**Note**: this must be done before any other functionality of the SDK will work - the SDK will throw an exception otherwise
```kotlin
import com.flipp.dvm_sdk_android.external.DVMSDK
import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DVMSDK.initialize(
            clientToken = "[PROVIDED BY FLIPP]",
            context = this,
            userId = "id-of-the-user-launching-the-app" [OPTIONAL]
        )
    }
}
```
- If a new user begins using the app without restarting (e.g logs out), then the user id can be assigned in the DvmSdk.config object
### Publication Repository
- The dvm-sdk-android provides a `PublicationRepository` with one exposed method `getPublications` that you will use to get Publications
```kotlin
/**
 * Fetches the PublicationList for a given [merchantId] and [storeCode]
 *
 * @param merchantId the merchantId
 * @param storeCode the store code
 * @param limit the max number of results
 * @param nextToken token for pagination and fetching subsequent results
 * @return Result containing a [PublicationList] or the [HttpNetworkException] that occurred
 */
@RequiresPermission(INTERNET)
suspend fun getPublications(
    merchantId: String,
    storeCode: String? = null,
    limit: Int? = null,
    nextToken: String? = null,
): Result<PublicationList>
```

### Rendering Publications
A `FlippPublication` is a Jetpack Compose Composable function that is used to render a `Publication`. It requires a Publication instance given by the `PublicationRepository`.
```kotlin
/**
 * A composable function that renders a Publication to the screen
 *
 * @param modifier The modifier to be applied to the composable
 * @param identifiers The identifiers for a composable
 * @param renderType The method of rendering the Publication
 * @param delegate The delegate for handling Publication events
 */
fun FlippPublication(
    modifier: Modifier = Modifier,
    publication: Publication,
    storeCode: String,
    delegate: DvmRendererDelegate,
    renderType: RenderType,
)
```

### Delegate Functions
A delegate should also be provided which allows you to handle specific events related to the Publication.
```kotlin
/**
 * Defines a delegate interface for handling Publication events
 *
 */
interface PublicationRendererDelegate {
    /**
     * Gets called once the Publication successfully loads
     * @param controller a controller to perform actions on the Publication
     * @param legacyIdMap a map from the legacy flyerItemIds to the new globalIds for each item in the Publication
     */
    fun onFinishLoad(controller: PublicationController, legacyIdMap: Map<Long, String>?)

    /**
     * Gets called if there is an error loading the Publication.
     * @param error the type of error that occurred along with a message describing it in detail
     */
    fun onFailedToLoad(error: PublicationError)

    /**
     * Gets called if the user taps on an Offer from within a Publication.
     * @param offer the [Offer] the user tapped on
     */
    fun onTap(offer: Offer)

    /**
     * Gets called if there was an error after tapping an Offer.
     * Note: this function does not get called if the user taps on something other than an Offer
     * @param error a message describing the error
     */
    fun onTapError(error: String)

    /**
     * Gets called if the user long-pressing on an Offer from within a Publication.
     * @param offer the [Offer] the user tapped on
     */
    fun onLongPress(offer: Offer)

    /**
     * Gets called if there was an error after long-pressing an Offer.
     * Note: this function does not get called if the user long-taps on something other than an Offer
     * @param error a message describing the error
     */
    fun onLongPressError(error: String)

    /**
     * Gets called when the user scrolls within the Publication.
     * @param flyerHeightPx The height of the flyer in pixels.
     * @param viewportBottomOffsetPx The offset from the bottom of the viewport.
     */
    fun onScroll(flyerHeightPx: Int, viewportBottomOffsetPx: Int)

    /**
     * Gets called after the host-app has requested a scroll-to action to occur and it is completed
     * @param offer the offer of the item scrolled to if applicable
     */
    fun onScrollToFinished(offer: Offer?)

    /**
     * Gets called when the user sees more than 50% of an Offer on screen
     * @param offers The the offers seen
     */
    fun onImpression(offers: List<String>)

    /**
     * Gets called when a user interacts with the publication or 6 seconds pass
     */
    fun onEngagedVisit()
}
```
### Publication Controller
A `PublicationController` is a way to perform actions on a `Publication`, it is provided in `PublicationDelegate.onFinishLoad`. 
```kotlin
/**
 * Interface defining a set of publication actions.
 */
interface PublicationController {
    /**
     * Scrolls to an item given a [globalId].
     * @param globalId The identifier of the item to scroll to.
     */
    fun scrollTo(globalId: String) = Unit

    /**
     * Registers a list of annotations to be used later.
     * @param annotations The list of annotations to register.
     */
    fun registerAnnotations(annotations: List<Annotation>) = Unit

    /**
     * Adds annotations of a specific type to the given global IDs.
     * @param type The type of the annotations.
     * @param globalIds The set of global IDs to add the annotations to.
     */
    fun addAnnotations(
        type: AnnotationType,
        globalIds: Set<String>,
    ) = Unit

    /**
     * Removes annotations of a specific type from the given global IDs.
     * @param type The type of the annotations.
     * @param globalIds The set of global IDs to remove the annotations from.
     */
    fun removeAnnotations(
        type: AnnotationType,
        globalIds: Set<String>,
    ) = Unit

    /**
     * Notifies that the Publication is or is not visible - if so it resets the ev timer
     * @param setVisibility True if the Publication is visible to the user
     */
    fun setVisibility(isVisible: Boolean) = Unit
}
```
