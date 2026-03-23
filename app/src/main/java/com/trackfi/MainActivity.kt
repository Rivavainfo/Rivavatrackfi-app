package com.trackfi

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.theme.glassMorphism
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import com.trackfi.data.preferences.UserPreferencesRepository
import com.trackfi.ui.analytics.AnalyticsScreen
import com.trackfi.ui.history.TransactionsScreen
import com.trackfi.ui.home.HomeScreen
import com.trackfi.ui.onboarding.GreetingScreen
import com.trackfi.ui.onboarding.SmsScanningScreen
import com.trackfi.ui.onboarding.SmsOptInScreen
import com.trackfi.ui.onboarding.WelcomeScreen
import com.trackfi.ui.settings.SettingsScreen
import com.trackfi.ui.portfolio.RivavaPortfolioScreen
import com.trackfi.ui.profile.ProfileScreen
import com.trackfi.ui.theme.TrackFiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.fillMaxSize
import javax.inject.Inject

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
    object RivavaPortfolio : Screen("rivava_portfolio", "Rivava Portfolio", Icons.Outlined.AccountBalanceWallet)
    object StockDetail : Screen("stock_detail", "Stock Detail", Icons.Outlined.AccountBalanceWallet)
        object TransactionDetail : Screen("transaction_detail", "Transaction Detail", Icons.AutoMirrored.Outlined.ListAlt)
}

val BaseBottomNavigationItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Analytics,
    Screen.Settings
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isPremiumUser = preferencesRepository.isPremiumUserFlow.collectAsState(initial = false).value
            val hasCompletedOnboardingState = preferencesRepository.hasCompletedOnboardingFlow.collectAsState(initial = null)
            val hasCompletedOnboarding = hasCompletedOnboardingState.value

            TrackFiTheme(isPremium = isPremiumUser) {
                if (hasCompletedOnboarding != null) {
                    TrackFiAppContent(hasCompletedOnboarding, preferencesRepository)
                } else {
                    // Show a minimal loading state or nothing while reading preferences
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun TrackFiAppContent(hasCompletedOnboarding: Boolean, preferencesRepository: UserPreferencesRepository? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isPremiumUser = preferencesRepository?.isPremiumUserFlow?.collectAsState(initial = false)?.value ?: false
    val bottomNavigationItems = if (isPremiumUser) {
        listOf(Screen.Home, Screen.Transactions, Screen.RivavaPortfolio, Screen.Analytics, Screen.Settings)
    } else {
        BaseBottomNavigationItems
    }

    val isBottomBarVisible = currentRoute in bottomNavigationItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .glassMorphism(cornerRadius = 24f, alpha = 0.2f)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.scaleIn(
                    initialScale = 0.95f,
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.scaleOut(
                    targetScale = 1.05f,
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            },
            popEnterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.scaleIn(
                    initialScale = 1.05f,
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            },
            popExitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.scaleOut(
                    targetScale = 0.95f,
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            }
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onNavigateNext = {
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Greeting.route) {
                GreetingScreen(onNavigateNext = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
            composable(Screen.SmsOptIn.route) {
                SmsOptInScreen(onNavigateNext = { optedIn ->
                    val alreadyScanned = runBlocking { preferencesRepository?.smsScanCompletedFlow?.first() ?: false }
                    if (optedIn && !alreadyScanned) {
                        navController.navigate(Screen.Scanning.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SmsOptIn.route) { inclusive = true }
                        }
                    }
                })
            }
            composable(Screen.Scanning.route) {
                SmsScanningScreen(onNavigateNext = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Scanning.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProfile = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTransactionDetail = { transactionId ->
                        navController.navigate("${Screen.TransactionDetail.route}/$transactionId")
                    }
                )
            }
            composable(Screen.Transactions.route) {
                // Not passing onNavigateToDetail to TransactionsScreen as it wasn't explicitly defined there.
                // We will navigate via view model effects or let the screen remain as a list.
                // If it was already defined, we would pass it. But compilation failed due to "Cannot find a parameter with this name: onNavigateToDetail".
                TransactionsScreen()
            }
            composable(
                route = "${Screen.TransactionDetail.route}/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
                com.trackfi.ui.history.TransactionDetailScreen(
                    transactionId = transactionId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AiReview.route) {
                com.trackfi.ui.aireview.AiReviewScreen()
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onRestartApp = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                })
            }
            composable(Screen.RivavaPortfolio.route) {
                RivavaPortfolioScreen(onNavigateToDetail = { ticker, focus ->
                    val focusParam = focus ?: "none"
                    navController.navigate("${Screen.StockDetail.route}/$ticker?focus=$focusParam")
                })
            }
            composable(
                route = "${Screen.StockDetail.route}/{ticker}?focus={focus}",
                arguments = listOf(
                    navArgument("ticker") { type = NavType.StringType },
                    navArgument("focus") {
                        type = NavType.StringType
                        defaultValue = "none"
                    }
                )
            ) { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
                val focus = backStackEntry.arguments?.getString("focus")?.takeIf { it != "none" }
                com.trackfi.ui.portfolio.StockPortfolioDetailScreen(
                    ticker = ticker,
                    initialFocus = focus,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "containerColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .selectable(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(visible = isSelected) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = iconColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
