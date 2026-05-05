# Rivava Premium Entitlement Flow

## Overview
The application handles premium entitlements across devices and sessions via a server-backed single source of truth in Firestore. This replaces the previous implementation that relied solely on local `SharedPreferences`.

## Components
- **`UserEntitlementRepository`**: Handles reading the entitlement state from Firestore (`is_premium`, `premium_status`, `premium_source`, `premium_unlocked_at`), verifying payment via a secure Cloud Function, and keeps a local `SharedPreferences` cache for fast access.
- **`PremiumViewModel`**: Exposes the `PremiumState` (`LOADING`, `UNLOCKED`, `LOCKED`, `ERROR`) via `StateFlow` to Jetpack Compose UI screens like `RivavaPortfolioScreen` and `ProfileScreen`.

## Lifecycle
1. **Login/App Launch**: `AuthViewModel` calls `syncEntitlement()` upon successful login. `PremiumViewModel` also triggers a sync on initialization.
2. **Payment Success**: The app calls `grantPremium(source, txnId)` which invokes the `verifyPayment` Firebase Cloud Function.
3. **Logout**: The `clearEntitlement()` function scrubs the local cache so the next user on the same device does not accidentally inherit a premium session.

## Security & Architecture
The client cannot write the `is_premium` flag directly to Firestore on payment success. A strict `firestore.rules` file has been added to restrict client writes to premium entitlement fields entirely.
The upgrade to premium status is securely handled via the `verifyPayment` Cloud Function, enforcing a trusted backend flow for entitlement activation.
