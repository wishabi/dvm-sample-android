package com.flipp.dvm.sample.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipp.dvm.sdk.android.external.PublicationRepository
import com.flipp.dvm.sdk.android.external.models.Offer
import com.flipp.dvm.sdk.android.external.models.Publication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // Fetching and displaying publications
    private val publicationsRepository = PublicationRepository()
    private val _publicationListUiState: MutableStateFlow<UiState<Publication>> =
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
            publicationsRepository.getPublications(
                merchantId = _merchantId.value,
                storeCode = _storeCode.value,
            ).onSuccess {
                _publicationListUiState.value =
                    if (it.publications.isNotEmpty()) {
                        UiState.Success(data = it.publications)
                    } else {
                        UiState.Empty
                    }
            }.onFailure {
                when (it) {
                    is PublicationRepository.Companion.HttpNetworkException -> {
                        _publicationListUiState.value = UiState.Failed(error = "${it.statusCode}: ${it.message}")
                    }
                    else -> {
                        _publicationListUiState.value = UiState.Failed(error = "${it.message}")
                    }
                }
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
