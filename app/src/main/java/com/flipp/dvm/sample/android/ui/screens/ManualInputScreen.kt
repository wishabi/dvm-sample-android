package com.flipp.dvm.sample.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays a manual input screen where the user can enter a
 * merchant ID and store code and submit the input.
 *
 * @param modifier the modifier
 * @param merchantId the merchant ID
 * @param storeCode the store code
 * @param onMerchantIdChanged a callback function that is called when the merchant ID is changed
 * @param onStoreCodeChanged a callback function that is called when the store code is changed
 * @param onSubmitButtonClicked a callback function that is called when the submit button is clicked
 */
@Composable
fun ManualInputScreen(
    modifier: Modifier = Modifier,
    merchantId: String,
    storeCode: String,
    onMerchantIdChanged: (String) -> Unit = {},
    onStoreCodeChanged: (String) -> Unit = {},
    onSubmitButtonClicked: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .imePadding()
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = merchantId,
            onValueChange = onMerchantIdChanged,
            label = { Text("Merchant") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = storeCode,
            onValueChange = onStoreCodeChanged,
            label = { Text("Store Code") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSubmitButtonClicked) {
            Text("Load Publications")
        }
    }
}
