package com.flipp.dvm.sample.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipp.content.v2.model.CorePublication
import com.flipp.content.v2.model.Offer
import com.flipp.content.v2.network.repository.PublicationRepository
import com.flipp.dvm.sdk.android.external.DvmSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // Fetching and displaying publications. The repository comes from com.flipp:content, which the
    // SDK exposes as part of its public API; point it at the SDK's configured curator endpoint.
    private val publicationsRepository =
        PublicationRepository(
            authorization = DvmSdk.config.clientToken,
            baseUrl = DvmSdk.config.endpoints.curator,
        )

    private val _publicationListUiState: MutableStateFlow<UiState<CorePublication>> =
        MutableStateFlow(UiState.Loading)
    val publicationListUiState get() = _publicationListUiState

    // Merchant id via user input or map
    private var _merchantId = MutableStateFlow("2018")
    val merchantId get() = _merchantId.asStateFlow()

    // Store code via user input or map
    private var _storeCode = MutableStateFlow("1174")
    val storeCode get() = _storeCode.asStateFlow()

    // The selected offer in the Publication
    val selectedOffer = MutableStateFlow<Offer?>(null)

    fun fetchPublications() {
        _publicationListUiState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                publicationsRepository.getPublicationsByStore(
                    merchantId = _merchantId.value,
                    storeCode = _storeCode.value,
                    language = "en",
                )
            }.onSuccess { publications ->
                _publicationListUiState.value =
                    if (publications.isNotEmpty()) {
                        UiState.Success(data = publications)
                    } else {
                        UiState.Empty
                    }
            }.onFailure {
                _publicationListUiState.value = UiState.Failed(error = "${it.message}")
            }
        }
    }

    fun onStoreCodeChanged(storeCode: String) {
        this._storeCode.value = storeCode.trim().filter { it.isDigit() }
    }

    fun onMerchantIdChanged(merchantId: String) {
        this._merchantId.value = merchantId.trim().filter { it.isDigit() }
    }
}

/**
 * Represents the state of a UI operation, such as fetching data.
 *
 * @param T The type of data that would be returned in the `Success` state.
 */
sealed class UiState<out T> {
    /**
     * Indicates that the operation is in progress
     */
    data object Loading : UiState<Nothing>()

    /**
     * Indicates that the operation completed successfully but returned no data.
     */
    data object Empty : UiState<Nothing>()

    /**
     * Indicates that the operation failed with an error.
     *
     * @property error The error message if the operation failed
     */
    data class Failed<Nothing>(
        val error: String,
    ) : UiState<Nothing>()

    /**
     * Indicates that the operation completed successfully and returned data.
     *
     * @param T the type of data that was returned
     * @property data the data that was returned in a list
     */
    data class Success<T>(
        val data: List<T>,
    ) : UiState<T>()
}
