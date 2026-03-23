package com.trackfi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.core.view.WindowCompat
import com.trackfi.data.preferences.UserPreferencesRepository
import com.trackfi.ui.analytics.AnalyticsScreen
import com.trackfi.ui.history.TransactionsScreen
import com.trackfi.ui.home.HomeScreen
import com.trackfi.ui.onboarding.*
import com.trackfi.ui.settings.SettingsScreen
import com.trackfi.ui.portfolio.RivavaPortfolioScreen
import com.trackfi.ui.theme.TrackFiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// ---------------- SCREENS ----------------

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Outlined.Home)
    object Greeting : Screen("greeting", "Greeting", Icons.Outlined.Home)
    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)
    object Scanning : Screen("scanning", "Scanning", Icons.Outlined.Home)
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Transactions : Screen("transactions", "History", Icons.AutoMirrored.Outlined.ListAlt)
    object Analytics : Screen("analytics", "Insights", Icons.Outlined.Analytics)
    object AiReview : Screen("ai_review", "AI Review", Icons.Outlined.AutoAwesome)
    object Settings : Screen("settings", "Profile", Icons.Outlined.Settings)
    object RivavaPortfolio : Screen("rivava_portfolio", "Portfolio", Icons.Outlined.AccountBalanceWallet)
    object StockDetail : Screen("stock_detail", "Stock Detail", Icons.Outlined.AccountBalanceWallet)
    object TransactionDetail : Screen("transaction_detail", "Transaction Detail", Icons.AutoMirrored.Outlined.ListAlt)
}

val BaseBottomNavigationItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Analytics,
    Screen.Settings
)

// ---------------- MAIN ACTIVITY ----------------

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val hasCompletedOnboarding = runBlocking {
            preferencesRepository.hasCompletedOnboardingFlow.first()
        }

        setContent {
            val isPremiumUser = preferencesRepository.isPremiumUserFlow.collectAsState(initial = false).value
            TrackFiTheme(isPremium = isPremiumUser) {
                TrackFiAppContent(hasCompletedOnboarding, preferencesRepository)
            }
        }
    }
}

// ---------------- APP CONTENT ----------------

@Composable
fun TrackFiAppContent(
    hasCompletedOnboarding: Boolean,
    preferencesRepository: UserPreferencesRepository? = null
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val isPremiumUser = preferencesRepository
        ?.isPremiumUserFlow
        ?.collectAsState(initial = false)
        ?.value ?: false

    val bottomNavigationItems = if (isPremiumUser) {
        listOf(
            Screen.Home,
            Screen.Transactions,
            Screen.RivavaPortfolio,
            Screen.Analytics,
            Screen.Settings
        )
    } else BaseBottomNavigationItems

    val isBottomBarVisible = currentRoute in bottomNavigationItems.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isBottomBarVisible) {
                Surface(
                    color = Color(0xFF1B1B1B).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .padding(
                                bottom = WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                            ),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavigationItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            CustomBottomNavItem(
                                screen = screen,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(Screen.Welcome.route) {
                WelcomeScreen {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }

            composable(Screen.Greeting.route) {
                GreetingScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                }
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProfile = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToTransactionDetail = {
                        navController.navigate("${Screen.TransactionDetail.route}/$it")
                    }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen()
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.RivavaPortfolio.route) {
                RivavaPortfolioScreen(
                    onNavigateToDetail = { ticker ->
                        navController.navigate("${Screen.StockDetail.route}/$ticker")
                    }
                )
            }

            composable(
                route = "${Screen.StockDetail.route}/{ticker}",
                arguments = listOf(navArgument("ticker") { type = NavType.StringType })
            ) {
                val ticker = it.arguments?.getString("ticker") ?: ""

                com.trackfi.ui.portfolio.StockPortfolioDetailScreen(
                    ticker = ticker,
                    initialFocus = null,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ---------------- BOTTOM NAV ITEM ----------------

@Composable
fun CustomBottomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.92f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else Color.Transparent,
        animationSpec = tween(250),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(250),
        label = "contentColor"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .selectable(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(visible = isSelected) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = screen.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}