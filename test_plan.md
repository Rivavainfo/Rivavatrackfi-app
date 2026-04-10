1. **Initialize Firebase in TrackFiApp.kt**:
   - Edit `app/src/main/java/com/rivavafi/universal/TrackFiApp.kt` to import `com.google.firebase.FirebaseApp` and call `FirebaseApp.initializeApp(this)` inside the `onCreate` method.
   - Verify the edit using `cat app/src/main/java/com/rivavafi/universal/TrackFiApp.kt`.

2. **Update GoogleSignInSection in AuthScreen.kt**:
   - Edit `app/src/main/java/com/rivavafi/universal/ui/auth/AuthScreen.kt` to modify `GoogleSignInSection`. Explicitly cast `LocalContext.current` to an `Activity` and initialize `GoogleSignInClient` securely before launching.
   - Verify the edit using `cat app/src/main/java/com/rivavafi/universal/ui/auth/AuthScreen.kt`.

3. **Improve Exception Handling in AuthViewModel.kt**:
   - Edit `app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt` to explicitly catch `com.google.firebase.auth.FirebaseAuthException` and `com.google.android.gms.common.api.ApiException` during sign-in, logging the status code and exception message upon failure.
   - Verify the edit using `cat app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt`.

4. **Verify Compilation**:
   - Run `./gradlew assembleDebug` to make sure all modifications compile without errors.

5. **Complete pre-commit steps**:
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

6. **Submit**:
   - Commit the changes using the `submit` tool.
