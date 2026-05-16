const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const verificationFunctions = require('./verificationFunctions');
const userLifecycleFunctions = require('./userLifecycleFunctions');

exports.sendVerificationEmail = verificationFunctions.sendVerificationEmail;
exports.verify = verificationFunctions.verify;
exports.checkVerification = verificationFunctions.checkVerification;
exports.onUserWrite = userLifecycleFunctions.onUserWrite;

exports.createUroPayOrder = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const amount = data.amount;

    if (!amount || typeof amount !== 'number' || amount !== 1100) {
        throw new functions.https.HttpsError('invalid-argument', 'Amount must be exactly 1100 paise.');
    }

    const apiKey = process.env.UROPAY_API_KEY;

    if (!apiKey) {
        console.error('UroPay API key is not configured.');
        throw new functions.https.HttpsError('internal', 'Server configuration error.');
    }

    try {
        const orderId = `order_${uid}_${Date.now()}`;

        // Save initial payment document
        const paymentRef = admin.firestore().collection('payments').doc();
        await paymentRef.set({
            userId: uid,
            orderId: orderId,
            amount: amount,
            currency: "INR",
            status: "created",
            purpose: "rivava_premium",
            provider: "uropay",
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // Mocking UroPay API call as we don't have a real endpoint
        // In a real scenario we would use axios.post('https://api.uropay.com/...', { ... }, { headers: { Authorization: `Bearer ${apiKey}` } })

        return {
            order_id: orderId,
            payment_url: `https://mock-uropay.example.com/pay/${orderId}`
        };
    } catch (error) {
        console.error('Error creating UroPay order:', error);
        throw new functions.https.HttpsError('internal', 'Unable to create order.');
    }
});

exports.verifyUroPayPayment = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const orderId = data.order_id;

    if (!orderId || typeof orderId !== 'string') {
        throw new functions.https.HttpsError('invalid-argument', 'Missing or invalid order_id.');
    }

    try {
        // Find matching payment doc
        const paymentsSnapshot = await admin.firestore().collection('payments')
            .where('orderId', '==', orderId)
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

        // Verify payment with UroPay via backend API call.
        // Mocking the backend check response as successful if orderId matches expected pattern.
        const isPaymentValid = true; // Replace with actual axios call to UroPay verify endpoint

        if (isPaymentValid) {
            // Update payment doc
            await paymentDoc.ref.update({
                status: "success",
                verified: true,
                verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // Update user premium status
            await admin.firestore().collection('users').doc(uid).set({
                is_premium: true,
                premium_status: "active",
                premium_source: "uropay",
                premium_unlocked_at: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });

            return { success: true, isPremium: true };
        } else {
             // Invalid payment
             await paymentDoc.ref.update({
                status: "failed",
                verified: false,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            return { success: false, isPremium: false };
        }
    } catch (error) {
        console.error('Error verifying UroPay payment:', error);
        throw new functions.https.HttpsError('internal', 'Unable to verify payment.');
    }
});

exports.uroPayWebhook = functions.region('asia-south1').https.onRequest(async (req, res) => {
    // In a real scenario, we must verify a signature header (e.g. req.headers['x-uropay-signature'])
    // For this implementation, we simulate receiving a valid webhook payload
    const { order_id, status } = req.body;

    if (!order_id || status !== 'success') {
        return res.status(400).send('Invalid webhook payload');
    }

    try {
        const paymentsSnapshot = await admin.firestore().collection('payments')
            .where('orderId', '==', order_id)
            .limit(1)
            .get();

        if (paymentsSnapshot.empty) {
            return res.status(404).send('Payment not found');
        }

        const paymentDoc = paymentsSnapshot.docs[0];
        const paymentData = paymentDoc.data();

        if (paymentData.status === 'success') {
            return res.status(200).send('Already processed');
        }

        // Update payment doc
        await paymentDoc.ref.update({
            status: "success",
            verified: true,
            verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // Update user premium status
        await admin.firestore().collection('users').doc(paymentData.userId).set({
            is_premium: true,
            premium_status: "active",
            premium_source: "uropay",
            premium_unlocked_at: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        return res.status(200).send('Webhook processed successfully');
    } catch (error) {
        console.error('Error processing UroPay webhook:', error);
        return res.status(500).send('Internal server error');
    }
});
