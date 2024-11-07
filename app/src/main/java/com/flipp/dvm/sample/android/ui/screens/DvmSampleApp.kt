package com.flipp.dvm.sample.android.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.flipp.dvm.sample.android.MainViewModel
import com.flipp.dvm.sample.android.navigation.Routes
import com.flipp.dvm.sample.android.ui.composables.AppBar
import com.flipp.dvm.sample.android.ui.composables.AppBarIcon
import com.flipp.dvm.sdk.android.external.PublicationIdentifiers
import kotlin.reflect.typeOf

@Composable
fun DvmSampleApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
) {
    // The select merchantId and storeCode - either through manual input or a map
    val merchantId by viewModel.merchantId.collectAsStateWithLifecycle()
    val storeCode by viewModel.storeCode.collectAsStateWithLifecycle()

    // Contains the states of the publication results list
    val publicationListUiState by viewModel.publicationListUiState.collectAsStateWithLifecycle()

    // States of the app bar at the top of all screen
    var appBarTitle by rememberSaveable { mutableStateOf("DVM Sample App") }
    var appBarIcon by rememberSaveable { mutableStateOf(AppBarIcon.NONE) }
    var showBackButton by rememberSaveable { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        AppBar(
            appBarTitle = appBarTitle,
            appBarIcon = appBarIcon,
            showBackButton = showBackButton,
            navController = navController,
        )
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Initial,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            composable<Routes.Initial> {
                appBarTitle = "DVM Sample App"
                showBackButton = false
                appBarIcon = AppBarIcon.NONE
                ManualInputScreen(
                    merchantId = merchantId,
                    storeCode = storeCode,
                    onMerchantIdChanged = viewModel::onMerchantIdChanged,
                    onStoreCodeChanged = viewModel::onStoreCodeChanged,
                    onSubmitButtonClicked = {
                        viewModel.fetchPublications()
                        navController.navigate(Routes.PublicationsList)
                    },
                )
            }
            composable<Routes.PublicationsList> {
                appBarTitle = "Merchant $merchantId, Store $storeCode"
                appBarIcon = AppBarIcon.NONE
                showBackButton = true
                PublicationListScreen(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    uiState = publicationListUiState,
                    storeCode = storeCode,
                )
            }
            composable<Routes.PublicationScreen>(
                typeMap = mapOf(typeOf<PublicationIdentifiers>() to Routes.PublicationScreen.identifiersNavType),
            ) {
                showBackButton = true
                val route = it.toRoute<Routes.PublicationScreen>()
                appBarTitle = "Publication"
                appBarIcon = AppBarIcon.NONE
                PublicationScreen(
                    identifiers = route.identifiers,
                    renderType = route.renderType,
                    viewModel = viewModel,
                )
            }
        }
    }
}
