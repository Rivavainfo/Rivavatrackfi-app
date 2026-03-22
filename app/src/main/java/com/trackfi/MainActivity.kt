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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.theme.glassMorphism
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
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
    object RivavaPortfolio : Screen("rivava_portfolio", "Portfolio", Icons.Outlined.AccountBalanceWallet)
    object StockDetail : Screen("stock_detail", "Stock Detail", Icons.Outlined.AccountBalanceWallet)
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
                Surface(
                    color = androidx.compose.ui.graphics.Color(0xFF1B1B1B).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route,
            modifier = Modifier.padding(bottom = 0.dp), // Fixes the extra space at the bottom/top
            enterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            exitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popEnterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popExitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
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
                HomeScreen()
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen()
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
                RivavaPortfolioScreen(onNavigateToDetail = { ticker ->
                    navController.navigate("${Screen.StockDetail.route}/$ticker")
                })
            }
            composable("${Screen.StockDetail.route}/{ticker}") { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
                com.trackfi.ui.portfolio.StockPortfolioDetailScreen(
                    ticker = ticker,
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.9f else 1f,
        animationSpec = tween(200),
        label = "iconScale"
    )

    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .bounceClick { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = screen.title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.sp
                ),
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
