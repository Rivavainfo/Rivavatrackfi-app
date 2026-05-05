const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const verificationFunctions = require('./verificationFunctions');
const userLifecycleFunctions = require('./userLifecycleFunctions');

exports.sendVerificationEmail = verificationFunctions.sendVerificationEmail;
exports.verify = verificationFunctions.verify;
exports.checkVerification = verificationFunctions.checkVerification;
exports.onUserWrite = userLifecycleFunctions.onUserWrite;

exports.verifyPayment = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const source = data.source || 'unknown';
    const txnId = data.txnId || 'none';

    // In a real application, you would verify the txnId with your payment provider (e.g., Stripe, Razorpay) here.
    // For this demonstration, we'll assume the payment is valid if a source is provided.

    try {
        await admin.firestore().collection('users').doc(uid).set({
            is_premium: true,
            premium_unlocked_at: admin.firestore.FieldValue.serverTimestamp(),
            premium_source: source,
            premium_txn_id: txnId,
            premium_status: 'active'
        }, { merge: true });

        return { success: true };
    } catch (error) {
        console.error('Error upgrading user to premium:', error);
        throw new functions.https.HttpsError('internal', 'Unable to upgrade user.');
    }
});
