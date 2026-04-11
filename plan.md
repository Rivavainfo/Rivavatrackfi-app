1. Add `default_web_client_id` to `app/src/main/res/values/strings.xml`.
2. Update `app/src/main/java/com/rivavafi/universal/ui/auth/AuthScreen.kt` to use the `default_web_client_id` string resource instead of the hardcoded value.
3. Read the `app/build.gradle.kts` file to verify the presence of the `com.google.gms.google-services` plugin and `firebase-auth` dependencies.
4. Read the `AuthViewModel.kt` file to examine the `FirebaseAuth` and `GoogleAuthProvider` implementation.
5. Run all relevant tests to ensure the changes are correct and have not introduced regressions.
6. Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.
7. Submit changes.
