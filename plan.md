1. **Update Resend Timer to 60 Seconds:**
   - In `AuthScreen.kt`, within the `CODE_SENT` block of `AuthMethodsSection`, change the initialization of `timer` from `30` to `60`.

2. **Implement 6-Digit OTP UI in AuthScreen.kt:**
   - In `AuthScreen.kt`, add a new composable `OtpInputField` that takes `otpText: String` and `onOtpChange: (String) -> Unit`.
   - The `OtpInputField` should use a `BasicTextField` (transparent) overlaid on a `Row` of 6 `Box` composables that render the individual digits. This will naturally support auto-moving the cursor as the user types.
   - Replace the existing `OutlinedTextField` for OTP input within the `CODE_SENT` block with this new `OtpInputField` composable.
   - Ensure the "Verify OTP" button logic calls `viewModel.verifyOtp(otp)`.

3. **Verify Implementation:**
   - Use `read_file` on `app/src/main/java/com/rivavafi/universal/ui/auth/AuthScreen.kt` to verify that the OTP UI and timer modifications were successfully applied.

4. **Run Tests:**
   - Execute `./gradlew assembleDebug` to ensure there are no compilation errors.
   - Execute `./gradlew test` to ensure existing tests pass.

5. **Pre-commit Steps:**
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

6. **Submit:**
   - Submit the changes with a descriptive commit message.
