1. **Fix Email Verification Login Bug:**
   - In `AuthViewModel.kt`, within `onEmailLogin`, add `repository.auth.currentUser?.reload()?.await()` immediately after `signInWithEmailAndPassword`. This refreshes the token to ensure the most recent `isEmailVerified` status is retrieved from Firebase, preventing a bug where newly verified emails still act as unverified.

2. **Fix Phone Authentication Formatting:**
   - In `AuthScreen.kt`, inside the `PhoneSignInSection` button `onClick` handler, strip out any non-digit characters from the input `phoneNumber` and check if it already starts with "91" (to prevent "+9191...") before correctly formatting it to exactly `+91<10-digits>`.
   - Apply the same validation check in `AuthViewModel.startPhoneVerification` or ensure the ViewModel relies entirely on the formatted string. The frontend validation ensures we pass exactly what is needed.

3. **Verify and Pre-commit:**
   - Run tests and assemble builds to ensure no regression.
   - Complete pre-commit actions.
   - Submit.
