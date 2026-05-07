const functions = require("firebase-functions");
const admin = require("firebase-admin");
const Razorpay = require('razorpay');
const crypto = require('crypto');

admin.initializeApp();

const verificationFunctions = require('./verificationFunctions');
const userLifecycleFunctions = require('./userLifecycleFunctions');

exports.sendVerificationEmail = verificationFunctions.sendVerificationEmail;
exports.verify = verificationFunctions.verify;
exports.checkVerification = verificationFunctions.checkVerification;
exports.onUserWrite = userLifecycleFunctions.onUserWrite;

exports.createRazorpayOrder = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const amount = data.amount;

    if (!amount || typeof amount !== 'number' || amount <= 0 || !Number.isInteger(amount)) {
        throw new functions.https.HttpsError('invalid-argument', 'Amount must be a positive integer (in paise).');
    }

    // Initialize Razorpay
    const keyId = process.env.RAZORPAY_KEY_ID || functions.config().razorpay?.key_id;
    const keySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.key_secret;

    if (!keyId || !keySecret) {
        console.error('Razorpay keys are not configured properly.');
        throw new functions.https.HttpsError('internal', 'Server configuration error.');
    }

    const razorpay = new Razorpay({
        key_id: keyId,
        key_secret: keySecret,
    });

    try {
        const receiptId = `receipt_${uid}_${Date.now()}`;

        const options = {
            amount: amount,
            currency: "INR",
            receipt: receiptId,
            notes: {
                userId: uid,
                purpose: "rivava_premium"
            }
        };

        const order = await razorpay.orders.create(options);

        // Save initial payment document
        const paymentRef = admin.firestore().collection('payments').doc();
        await paymentRef.set({
            userId: uid,
            orderId: order.id,
            amount: amount,
            currency: "INR",
            status: "created",
            purpose: "rivava_premium",
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        return {
            order_id: order.id,
            amount: order.amount,
            currency: order.currency,
            key_id: keyId // Returning public key is safe and sometimes useful for Android
        };
    } catch (error) {
        console.error('Error creating Razorpay order:', error);
        throw new functions.https.HttpsError('internal', 'Unable to create order.');
    }
});

exports.verifyRazorpayPayment = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const razorpay_order_id = data.razorpay_order_id;
    const razorpay_payment_id = data.razorpay_payment_id;
    const razorpay_signature = data.razorpay_signature;

    if (!razorpay_order_id || typeof razorpay_order_id !== 'string' ||
        !razorpay_payment_id || typeof razorpay_payment_id !== 'string' ||
        !razorpay_signature || typeof razorpay_signature !== 'string') {
        throw new functions.https.HttpsError('invalid-argument', 'Missing or invalid Razorpay fields.');
    }

    const keySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.key_secret;

    if (!keySecret) {
         console.error('Razorpay secret is not configured.');
         throw new functions.https.HttpsError('internal', 'Server configuration error.');
    }

    try {
        // Find matching payment doc
        const paymentsSnapshot = await admin.firestore().collection('payments')
            .where('orderId', '==', razorpay_order_id)
            .where('userId', '==', uid)
            .limit(1)
            .get();

        if (paymentsSnapshot.empty) {
            throw new functions.https.HttpsError('not-found', 'Payment record not found.');
        }

        const paymentDoc = paymentsSnapshot.docs[0];
        const paymentData = paymentDoc.data();

        if (paymentData.status === 'success') {
            return { success: true, isPremium: true, message: 'Already processed.' };
        }

        // Verify signature
        const payload = razorpay_order_id + "|" + razorpay_payment_id;
        const expectedSignature = crypto.createHmac('sha256', keySecret)
                                        .update(payload.toString())
                                        .digest('hex');

        // Timing-safe comparison
        const isValid = crypto.timingSafeEqual(Buffer.from(expectedSignature), Buffer.from(razorpay_signature));

        if (isValid) {
            // Update payment doc
            await paymentDoc.ref.update({
                status: "success",
                paymentId: razorpay_payment_id,
                signatureVerified: true,
                verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // Update user premium status
            await admin.firestore().collection('users').doc(uid).set({
                is_premium: true,
                premium_status: "active",
                premium_source: "razorpay",
                premium_txn_id: razorpay_payment_id,
                premium_unlocked_at: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });

            return { success: true, isPremium: true };
        } else {
             // Invalid signature
             await paymentDoc.ref.update({
                status: "failed",
                paymentId: razorpay_payment_id,
                signatureVerified: false,
                failureReason: "invalid_signature",
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            return { success: false, isPremium: false };
        }
    } catch (error) {
        console.error('Error verifying Razorpay payment:', error);
        throw new functions.https.HttpsError('internal', 'Unable to verify payment.');
    }
});
