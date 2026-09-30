package studio.appvero.bikecare.features.home.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.auth.ui.screen.HomeEvent
import studio.appvero.bikecare.features.auth.ui.screen.HomeUiState
import studio.appvero.bikecare.ui.theme.*

enum class HomeTab(val label: Int, val icon: Int) {
    Home(R.string.nav_home, R.drawable.ic_home),
    Care(R.string.nav_care, R.drawable.ic_care),
    Garage(R.string.nav_garage, R.drawable.ic_garage),
    More(R.string.nav_more, R.drawable.ic_more),
}

@Composable
fun HomeScreen(selectedTab: HomeTab, showBottomBar: Boolean, onSelectTab: (HomeTab) -> Unit,
    content: @Composable () -> Unit) {
    Scaffold(bottomBar = {
        if (showBottomBar) NavigationBar {
            HomeTab.entries.forEach { tab ->
                NavigationBarItem(selected = selectedTab == tab, onClick = { onSelectTab(tab) },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = { Text(localizedString(tab.label)) })
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
    }
}

@Composable
fun HomeTabScreen(tab: HomeTab, state: HomeUiState, onEvent: (HomeEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        Text(localizedString(tab.label), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(when (tab) {
            HomeTab.Home -> R.string.home_intro
            HomeTab.Care -> R.string.care_placeholder
            else -> R.string.more_intro
        }), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (tab == HomeTab.More) {
            state.form.error?.let { Text(localizedString(it), color = MaterialTheme.colorScheme.error) }
            Button(onClick = { onEvent(HomeEvent.Logout) }, enabled = !state.form.busy,
                modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.action, contentColor = AppTheme.colors.onAction)) {
                Text(localizedString(if (state.form.busy) R.string.auth_please_wait else R.string.auth_sign_out))
            }
        }
    }
}
