const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { sendWelcomeEmail } = require("./services/emailService");

// Firestore Trigger: Send welcome email when user is created or updated
// Condition: isVerified === true AND email exists AND welcomeEmailSentAt does not exist.
exports.onUserWrite = functions.firestore.document('users/{userId}').onWrite(async (change, context) => {
    // If the document was deleted, do nothing
    if (!change.after.exists) {
        return null;
    }

    const newValue = change.after.data();
    const email = newValue.email;
    const isVerified = newValue.isVerified;
    const welcomeEmailSentAt = newValue.welcomeEmailSentAt;

    if (email && isVerified === true && !welcomeEmailSentAt) {
        // Send the welcome email
        const success = await sendWelcomeEmail(email);

        if (success) {
            // Update welcomeEmailSentAt to ensure idempotency
            return change.after.ref.update({
                welcomeEmailSentAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
    }

    return null;
});
