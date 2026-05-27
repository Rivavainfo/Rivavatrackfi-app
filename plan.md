So ProfileScreen was crashing. Why?
Could it be `userModel?.premiumStatus == true` causing issue? No, `userModel` defaults to `null` initially, so `premiumStatus` is null. `null == true` is false.
Wait, let's look at `isPremiumUser` which is `Flow<Boolean>`.
I changed: `val finalIsPremium = isPremiumUser || isPremiumPref` to `val finalIsPremium = isPremiumModel || isPremiumPref || isPremiumUser`.

Is it possible `userModel?.phone` is null and `.takeIf { it.isNotBlank() }` crashes?
`userModel?.phone` returns `String?`. `takeIf` on a string might throw NPE if not careful? No, Kotlin handles `takeIf` safely.

Could it be the injection?
`profileViewModel: ProfileViewModel = hiltViewModel()`
Yes! In `HomeActivity`, `NavHost` has `ProfileScreen()`. The `hiltViewModel()` extension requires Hilt to be able to create the `ProfileViewModel`.
But wait, `ProfileViewModel` is annotated with `@HiltViewModel`. I added `FirebaseModule` providing `FirebaseFirestore`.
Did I get an error? The build succeeded! But runtime crash usually means Hilt injection failed because `ProfileViewModel` was missing something? No, it has empty constructor in my tests previously but then I added `FirebaseFirestore` to constructor.
Let's see what else could cause the crash.
Is it the `AuthActivity` jumping to `SmsOptIn`? No, the user explicitly said "app keeps crashing when it goes to profile screen".
Ah! `ProfileViewModel` uses `FirebaseAuth.getInstance()`. Is it provided via constructor? No, I only injected `FirebaseFirestore`!
`val uid = FirebaseAuth.getInstance().currentUser?.uid` inside `startListeningToProfile()` is fine.
Wait, does `HiltViewModel` generation fail at runtime if not annotated correctly? It has `@HiltViewModel` and `@Inject constructor`.

Let's check `ProfileViewModel` imports:
```kotlin
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
```
Yes, this is valid.

What if `ProfileScreen` is used somewhere that doesn't have a `NavBackStackEntry` for Hilt to scope it? In `HomeActivity`, it's inside `composable(Screen.Profile.route)`. That provides a valid `ViewModelStoreOwner`.

Wait, could it be this:
```kotlin
    val profileState by profileViewModel.profileState.collectAsState()
    val userModel = profileState.userModel
```
This is all standard Compose state collection.

What about `updatePhone` function?
```kotlin
    fun updatePhone(phone: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) return
```
This is perfectly fine.

What if `ProfileViewModel` crashed because `FirebaseAuth.getInstance().currentUser` is null?
```kotlin
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _profileState.value = ProfileState(isLoading = false, error = "User not logged in")
            Log.e(TAG, "No logged in user found.")
            return
        }
```
This is also safe.

Could it be: `isPhoneVerified = snapshot.getBoolean("isPhoneVerified") ?: false`
If `snapshot.getBoolean` returns null, it defaults to false. This is safe.

What about `snapshot.getLong("createdAt") ?: System.currentTimeMillis()`?
If the field exists but is not a number (e.g. `Timestamp`), `getLong` will throw a `RuntimeException`!
Yes! `createdAt` in Firestore is saved as `com.google.firebase.Timestamp.now()`.
If we call `snapshot.getLong("createdAt")`, it throws `RuntimeException: value is not a number`.
Let's check how it's saved in `OnboardingViewModel`:
`"createdAt" to com.google.firebase.Timestamp.now()`
Yes! `Timestamp` is NOT a `Long`.
In `ProfileViewModel`:
`createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),`
This WILL crash when `getLong` is called on a `Timestamp` field!
Firestore provides `getTimestamp("createdAt")?.toDate()?.time`.
Let's check `FirebaseUserManager` in the original code, how did it parse it?
