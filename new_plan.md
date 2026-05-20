1. Implement a robust phone normalization in `AuthViewModel` (or a helper object) that reliably processes phone numbers to standard E.164 strings. Wait to remove previous ad-hoc formatting inside `AuthActivity.kt`.
2. Introduce explicitly valid/invalid outcomes so `AuthActivity` acts on accurate formats and rejects malformed inputs before calling the backend.
3. Keep the "India +91 fallback" logic consistent with the backend API `sanitizePhone`. Since the backend adds `+91` to 10-digit lengths and prepends `+` otherwise, replicate this precisely locally while enforcing minimum sanity checks.
4. Ensure the same normalized valid string is used consistently for the UI prompt, send OTP request, and verify OTP flow.
5. Create unit tests for the phone validation and normalizer (add a new Test file for AuthViewModel or PhoneNormalizer) to explicitly check edge cases.
6. Verify fixes compile and tests pass via `./gradlew testDebugUnitTest`.
7. Pre-commit check and submission.
