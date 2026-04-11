1. **Update Security & Privacy Link:**
   - In `app/src/main/java/com/rivavafi/universal/ui/profile/ProfileScreen.kt`, update the `Security & Privacy` QuickActionItem click listener to open the URL `https://www.rivava.in/privacy-policy.html` using an Intent instead of showing a toast.

2. **Update Notification Settings to Website:**
   - In `app/src/main/java/com/rivavafi/universal/ui/profile/ProfileScreen.kt`, change the `title` of the Notifications `QuickActionItem` to "Website".
   - Update its click listener to open the URL `https://www.rivava.in/` using an Intent.

3. **Implement Help Center Chatbot UI:**
   - Create `HelpCenterScreen.kt` in `app/src/main/java/com/rivavafi/universal/ui/help/`.
   - Implement the chat UI using `Scaffold` taking inner padding, a `LazyColumn` passing the inner padding to display chat messages as a list, and a bottom input section using `OutlinedTextField` and `IconButton` with a send icon to input text.

4. **Implement Help Center Chatbot State & Logic:**
   - Define a `data class Message(val text: String, val isUser: Boolean)` in `HelpCenterScreen.kt`.
   - Use `remember { mutableStateListOf<Message>() }` to store the chat history. Add an initial greeting message.
   - Use `remember { mutableStateOf("") }` for the current input text.
   - Write a simple helper function `getBotResponse(query: String): String` to handle keywords like "app", "help", "hello", "hi" returning mocked responses, and a default AI fallback response.
   - Upon clicking send, add the user message, clear input text, and launch a short delay (e.g., coroutine with `delay(500)`) before appending the bot's response.

5. **Update Navigation for Help Center:**
   - In `app/src/main/java/com/rivavafi/universal/MainActivity.kt`, add `object HelpCenter : Screen("help_center", "Help Center", Icons.Outlined.Info)` to the `Screen` sealed class.
   - Add `composable(Screen.HelpCenter.route) { com.rivavafi.universal.ui.help.HelpCenterScreen(onBack = { navController.popBackStack() }) }` inside the `NavHost` setup in `MainActivity.kt`.
   - In `app/src/main/java/com/rivavafi/universal/MainActivity.kt`, update the `com.rivavafi.universal.ui.profile.ProfileScreen` invocation inside `composable(Screen.Profile.route)` to pass `onNavigateToHelpCenter = { navController.navigate(Screen.HelpCenter.route) }`.
   - In `app/src/main/java/com/rivavafi/universal/ui/profile/ProfileScreen.kt`, add the `onNavigateToHelpCenter: () -> Unit = {}` callback parameter to the `ProfileScreen` composable function and invoke it inside the Help Center `QuickActionItem` click listener.

6. **Verify Build:**
   - Run `./gradlew assembleDebug` to confirm the project builds successfully with the new screen and navigation route.

7. **Verify Tests:**
   - Run unit tests using `./gradlew test` to ensure changes are correct and no regressions were introduced.

8. **Pre-commit Steps:**
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

9. **Commit & Submit:**
   - Submit the changes with descriptive branch and commit message.
