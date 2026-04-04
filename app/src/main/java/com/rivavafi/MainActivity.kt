package com.rivavafi

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.offset
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
import com.rivavafi.ui.theme.bounceClick
import com.rivavafi.ui.theme.glassMorphism
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
import com.rivavafi.data.preferences.UserPreferencesRepository
import com.rivavafi.ui.analytics.AnalyticsScreen
import com.rivavafi.ui.history.TransactionsScreen
import com.rivavafi.ui.home.HomeScreen
import com.rivavafi.ui.onboarding.GreetingScreen
import com.rivavafi.ui.onboarding.SmsScanningScreen
import com.rivavafi.ui.onboarding.SmsOptInScreen
import com.rivavafi.ui.onboarding.WelcomeScreen
import com.rivavafi.ui.settings.SettingsScreen
import com.rivavafi.ui.portfolio.PremiumUnlockDialog
import com.rivavafi.ui.portfolio.RivavaPortfolioScreen
import com.rivavafi.ui.profile.ProfileScreen
import com.rivavafi.ui.theme.TrackFiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.fillMaxSize
import javax.inject.Inject

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Auth : Screen("auth", "Auth", Icons.Outlined.Home)
    object Welcome : Screen("welcome", "Welcome", Icons.Outlined.Home)
    object Greeting : Screen("greeting", "Greeting", Icons.Outlined.Home)
    object SmsOptIn : Screen("sms_opt_in", "SmsOptIn", Icons.Outlined.Home)
    object Scanning : Screen("scanning", "Scanning", Icons.Outlined.Home)
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Transactions : Screen("transactions", "History", Icons.AutoMirrored.Outlined.ListAlt)
    object Analytics : Screen("analytics", "Insights", Icons.Outlined.Analytics)
    object AiReview : Screen("ai_review", "AI Review", Icons.Outlined.AutoAwesome)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)
    object RivavaPortfolio : Screen("rivava_portfolio", "Rivava Portfolio", Icons.Outlined.AccountBalanceWallet)
    object StockDetail : Screen("stock_detail", "Stock Detail", Icons.Outlined.AccountBalanceWallet)
    object TransactionDetail : Screen("transaction_detail", "Transaction Detail", Icons.AutoMirrored.Outlined.ListAlt)
}

val BaseBottomNavigationItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Analytics,
    Screen.Profile
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

    // Always include RivavaPortfolio, even if not premium, so we can show the lock icon and trigger the unlock flow
    val bottomNavigationItems = listOf(Screen.Home, Screen.Transactions, Screen.RivavaPortfolio, Screen.Analytics, Screen.Profile)

    val isBottomBarVisible = currentRoute in bottomNavigationItems.map { it.route }

    var showPremiumUnlockDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val userName by (preferencesRepository?.userNameFlow ?: kotlinx.coroutines.flow.flowOf("")).collectAsState(initial = "")

    if (showPremiumUnlockDialog) {
        PremiumUnlockDialog(
            userName = userName ?: "",
            onDismiss = { showPremiumUnlockDialog = false },
            onUnlockSuccess = {
                coroutineScope.launch {
                    preferencesRepository?.setPremiumUserForCurrent(true)
                }
                showPremiumUnlockDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isBottomBarVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFF161616).copy(alpha = 0.85f))
                            .glassMorphism(cornerRadius = 999f, alpha = 0.05f, strokeAlpha = 0.05f)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavigationItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val isLocked = screen == Screen.RivavaPortfolio && !isPremiumUser

                            CustomBottomNavItem(
                                screen = screen,
                                isSelected = isSelected,
                                isLocked = isLocked,
                                onClick = {
                                    if (isLocked) {
                                        showPremiumUnlockDialog = true
                                    } else if (!isSelected) {
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
        containerColor = androidx.compose.ui.graphics.Color(0xFF0A0A0A)
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Screen.Auth.route,
            modifier = Modifier.fillMaxSize(),
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
            composable(Screen.Auth.route) {
                com.rivavafi.ui.auth.AuthScreen(
                    onAuthSuccess = {
                        val route = if (hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route
                        navController.navigate(route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
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
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToTransactionDetail = { transactionId ->
                        navController.navigate("${Screen.TransactionDetail.route}/$transactionId")
                    }
                )
            }
            composable(Screen.Profile.route) {
                com.rivavafi.ui.profile.ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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
                com.rivavafi.ui.history.TransactionDetailScreen(
                    transactionId = transactionId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AiReview.route) {
                com.rivavafi.ui.aireview.AiReviewScreen()
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
                com.rivavafi.ui.portfolio.StockPortfolioDetailScreen(
                    ticker = ticker,
                    onBack = { navController.popBackStack() },
                    onNavigateToPdfViewer = { assetName ->
                        navController.navigate("pdf_viewer/$assetName")
                    }
                )
            }
            composable(
                "pdf_viewer/{assetName}",
                arguments = listOf(navArgument("assetName") { type = NavType.StringType })
            ) { backStackEntry ->
                val assetName = backStackEntry.arguments?.getString("assetName") ?: "portfolio_ireda.pdf"
                com.rivavafi.ui.portfolio.PdfViewerScreen(
                    assetName = assetName,
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
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val iconColor by animateColorAsState(
        targetValue = if (isLocked && !isSelected) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        } else if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(300),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .selectable(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = 24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
                if (isLocked) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = com.rivavafi.ui.theme.SecondaryPink,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = 6.dp, y = (-2).dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .padding(1.dp)
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
