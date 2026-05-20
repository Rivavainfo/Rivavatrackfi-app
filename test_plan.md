1. Remove stale Firebase PhoneAuth state logic (`_resendToken`) that is causing the `ClassCastException` / crash.
2. Verify all references to `resendToken` or `verificationId` inside `AuthViewModel` using `SavedStateHandle` are cleared since it uses custom backend OTP and these properties are no longer valid.
3. Validate OTP flows are still working by verifying backend OTP send & verify.
4. Call `pre_commit_instructions` as required before completion.
5. Submit changes.
