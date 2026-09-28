const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { sendWelcomeEmail } = require("./services/emailService");

/**
 * Firestore Trigger: Send professional Welcome / Thank You email when user profile is created/updated.
 * Conditions:
 * - Valid email is present
 * - User email or account is verified (or authenticated via Google/OAuth)
 * - welcomeEmailSentAt does not already exist (idempotent)
 */
exports.onUserWrite = functions.firestore.document('users/{userId}').onWrite(async (change, context) => {
    // If the document was deleted, do nothing
    if (!change.after.exists) {
        return null;
    }

    const newValue = change.after.data() || {};
    const email = newValue.email;
    const isVerified = newValue.isVerified === true ||
        newValue.email_verified === true ||
        newValue.auth_provider === 'google' ||
        newValue.loginProvider === 'google';
    const welcomeEmailSentAt = newValue.welcomeEmailSentAt;
    const name = newValue.full_name || newValue.display_name || newValue.name || newValue.username || '';

    if (email && isVerified && !welcomeEmailSentAt) {
        console.log(`Sending welcome email to ${email} for user ${context.params?.userId || 'unknown'}`);
        const success = await sendWelcomeEmail(email, name);

        if (success) {
            // Update welcomeEmailSentAt to ensure strict idempotency
            return change.after.ref.set({
                welcomeEmailSentAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
        }
    }

    return null;
});

/**
 * Firebase Auth Trigger: Trigger welcome email when a new user signs up with pre-verified email (e.g. Google OAuth)
 */
exports.onAuthUserCreated = functions.auth.user().onCreate(async (user) => {
    if (!user || !user.email) {
        return null;
    }

    const db = admin.firestore();
    const userRef = db.collection('users').doc(user.uid);

    try {
        const docSnap = await userRef.get();
        const data = docSnap.exists ? docSnap.data() : {};

        if (!data.welcomeEmailSentAt && (user.emailVerified || (user.providerData && user.providerData.some(p => p.providerId === 'google.com')))) {
            const name = user.displayName || data.full_name || data.display_name || '';
            console.log(`Auth trigger: Sending welcome email to verified user ${user.email}`);
            const success = await sendWelcomeEmail(user.email, name);

            if (success) {
                await userRef.set({
                    email: user.email,
                    isVerified: true,
                    email_verified: true,
                    welcomeEmailSentAt: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
        }
    } catch (error) {
        console.error(`Error in onAuthUserCreated for ${user.uid}:`, error);
    }

    return null;
});

