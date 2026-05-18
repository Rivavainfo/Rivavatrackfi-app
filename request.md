The goal is to create an ultra-premium Elite landing page to replace `EliteBottomSheet`.

Steps needed:
1. Redesign `HomeScreen` Elite card to be ultra-premium as per requirements.
2. Create `EliteLandingActivity` which will be a full-screen cinematic activity launched when the `HomeScreen` Elite card is tapped. We will bypass `EliteBottomSheet`.
3. Add `EliteLandingActivity` to `AndroidManifest.xml`.
4. Update `HomeScreen.kt` to launch `EliteLandingActivity` using an Intent instead of showing `EliteBottomSheet`.

The `EliteLandingActivity` must include:
- Premium hero section
- Live seat availability from Firestore (`EliteViewModel` / `eliteConfig` and `eliteSubscription`)
- Custom back button
- Animated benefit cards
- Timeline section
- Pricing section
- Apply flow + automated WhatsApp/SMS contact, tracking analytics with Firestore and custom messages.

The UI needs to be built with Compose using dark luxury themes, soft gold accents, animations (animateFloatAsState, AnimatedVisibility), and glassmorphism.

Wait, the prompt mentions `MotionLayout` and `Lottie`, but we are mostly using Compose. I'll make sure to use Compose's animation APIs and maybe Lottie for Compose (`lottie-compose`) if it's available, otherwise fallback to premium Compose animations. Let me check if `lottie-compose` is in `build.gradle.kts`.
