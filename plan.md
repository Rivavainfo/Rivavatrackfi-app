1. **Fix Authentication Redirection on Logout:**
   - In `MainActivity.kt`, inside `TrackFiAppContent`, add a `LaunchedEffect(userName)` below its declaration (around line 150). If `userName.isNullOrBlank()` is true after the initial load, use `navController.navigate(Screen.Auth.route) { popUpTo(0) { inclusive = true } }` to log out securely and forcefully.
   - In `HomeViewModel.kt`, within the `logout()` method, add `com.google.firebase.auth.FirebaseAuth.getInstance().signOut()`.
   - In `SettingsViewModel.kt`, within the `logout()` method, add `com.google.firebase.auth.FirebaseAuth.getInstance().signOut()`.

2. **Remove Portfolio Lock Logic:**
   - In `MainActivity.kt`, around line 187, replace `val isLocked = screen == Screen.RivavaPortfolio && !isPremiumUser` with `val isLocked = false`.
   - In `MainActivity.kt`, remove the `showPremiumUnlockDialog` block (around lines 151-163) and its `var showPremiumUnlockDialog` declaration.

3. **Disable Screenshots in Rivava Portfolio:**
   - Inside `RivavaPortfolioScreen.kt` (at the top of `RivavaPortfolioScreen` composable) and `StockPortfolioDetailScreen.kt` (at the top of `StockPortfolioDetailScreen` composable), add a `DisposableEffect(Unit)` that unwraps the `Activity` from `LocalContext.current` using a while loop on `ContextWrapper`. Inside `onEffect`, set `WindowManager.LayoutParams.FLAG_SECURE` on the `window` and clear the flag inside `onDispose`.
   - Note: Needs `import android.view.WindowManager` and `import android.content.ContextWrapper`.

4. **Improve Portfolio Stock Card UI:**
   - In `PremiumComponents.kt`, modify `PortfolioStockCard`.
   - Change `marketPrice` `Text` style to `MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)`.
   - Change `percentageChange` `Text` style to `MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)`.
   - Adjust the `arrowIcon` `Icon` size to `16.dp` in `Row` below marketPrice.

5. **Verify Code Implementation:**
   - Use `read_file` to confirm changes were successfully applied to `MainActivity.kt`, `HomeViewModel.kt`, `SettingsViewModel.kt`, `PremiumComponents.kt`, `RivavaPortfolioScreen.kt`, and `StockPortfolioDetailScreen.kt`.

6. **Run Tests:**
   - Run `./gradlew assembleDebug` to make sure there are no compilation errors with the window flags.
   - Run `./gradlew test` to ensure existing tests pass.

7. **Pre-commit Steps:**
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

8. **Submit Changes:**
   - Submit the changes.
