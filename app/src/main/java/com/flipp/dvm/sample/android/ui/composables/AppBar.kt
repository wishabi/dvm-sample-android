package com.flipp.dvm.sample.android.ui.composables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.flipp.dvm.sample.android.R
import com.flipp.dvm.sample.android.ui.theme.Typography

enum class AppBarIcon {
    NONE,
    COPY,
}

/**
 * The top bar of the app
 *
 * @param modifier the modifier
 * @param appBarTitle the main text in the app bar
 * @param appBarIcon the icon present
 * @param showBackButton True if the back button for navigation should be shown
 * @param navController the nav controller for navigating to other screens, and to pop the backstack
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    appBarTitle: String,
    appBarIcon: AppBarIcon,
    showBackButton: Boolean,
    navController: NavHostController,
) {
    val clipboard =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (appBarIcon) {
                    AppBarIcon.NONE -> {
                        Text(appBarTitle, style = Typography.titleMedium)
                    }

                    AppBarIcon.COPY -> {
                        Text(appBarTitle, style = Typography.titleSmall)
                        IconButton(onClick = {
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Publication Id",
                                    appBarTitle,
                                ),
                            )
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.content_copy),
                                contentDescription = "Copy to clipboard",
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back Button",
                    )
                }
            }
        },
    )
}
