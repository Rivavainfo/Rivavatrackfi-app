The user wants to ensure:
1. The OTP is sent properly (already handled in previous PR and current state: using backend `RetrofitClient` with try/catch and logging, updating `PhoneAuthState` directly to `CODE_SENT`).
2. The OTP input stays above the keyboard. Currently in Compose, `Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, ...)` will push the content up when the keyboard appears *if* `imePadding()` is added, or if `WindowCompat.setDecorFitsSystemWindows(window, false)` + `imePadding()` is used. Alternatively, changing `Arrangement.Center` to a scrolling view `verticalScroll` or adding `imePadding()` ensures the keyboard doesn't overlap.
Let's add `Modifier.imePadding()` to the `Column` in `OtpActivity` and ensure we add `Modifier.verticalScroll(rememberScrollState())` to prevent the UI from being pushed completely off-screen and becoming unscrollable on smaller screens.

Wait, `OtpActivity` uses:
```kotlin
        setContent {
            TrackFiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    OtpScreenContent(...)
```
I can add `imePadding()` to `OtpScreenContent`'s `Column` and `verticalScroll` to ensure it pushes up when the keyboard is open and remains fully accessible.
