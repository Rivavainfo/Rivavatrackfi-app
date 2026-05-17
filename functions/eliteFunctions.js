const functions = require("firebase-functions");
const admin = require("firebase-admin");

exports.createEliteOrder = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const amount = data.amount;

    if (!amount || typeof amount !== 'number' || amount !== 330000) {
        throw new functions.https.HttpsError('invalid-argument', 'Amount must be exactly 330000 paise.');
    }

    try {
        const orderId = `elite_order_${uid}_${Date.now()}`;

        // Save initial payment document
        const paymentRef = admin.firestore().collection('elite_payment_events').doc();
        await paymentRef.set({
            uid: uid,
            orderId: orderId,
            amount: 3300,
            currency: "INR",
            status: "created",
            plan: "elite_3300",
            rawEventType: "created",
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            verifiedAt: null
        });

        // Mocking payment url for test
        return {
            order_id: orderId,
            payment_url: `https://mock-uropay.example.com/pay/${orderId}`
        };
    } catch (error) {
        console.error('Error creating Elite order:', error);
        throw new functions.https.HttpsError('internal', 'Unable to create order.');
    }
});

exports.verifyElitePayment = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const orderId = data.order_id;

    if (!orderId || typeof orderId !== 'string') {
        throw new functions.https.HttpsError('invalid-argument', 'Missing or invalid order_id.');
    }

    const db = admin.firestore();

    try {
        const result = await db.runTransaction(async (transaction) => {
            // Find matching payment doc
            const paymentsQuery = db.collection('elite_payment_events')
                .where('orderId', '==', orderId)
                .where('uid', '==', uid)
                .limit(1);

            const paymentsSnapshot = await transaction.get(paymentsQuery);

            if (paymentsSnapshot.empty) {
                throw new functions.https.HttpsError('not-found', 'Payment record not found.');
            }

            const paymentDoc = paymentsSnapshot.docs[0];
            const paymentData = paymentDoc.data();

            if (paymentData.status === 'success') {
                return { success: true, isElite: true, message: 'Already processed.' };
            }

            // Verify payment
            const isPaymentValid = true; // Replace with actual axios call to UroPay verify endpoint

            if (!isPaymentValid) {
                 transaction.update(paymentDoc.ref, {
                    status: "failed",
                    rawEventType: "failed",
                    updatedAt: admin.firestore.FieldValue.serverTimestamp()
                });
                return { success: false, isElite: false, error: "Payment verification failed" };
            }

            // Check config
            const configRef = db.collection('elite_membership_meta').doc('config');
            const configDoc = await transaction.get(configRef);

            let occupiedSeats = 0;
            let totalSeats = 100;
            if (configDoc.exists) {
                const configData = configDoc.data();
                occupiedSeats = configData.occupiedSeats || 0;
                totalSeats = configData.totalSeats || 100;
            } else {
                transaction.set(configRef, { totalSeats: 100, occupiedSeats: 0, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
            }

            if (occupiedSeats >= totalSeats) {
                transaction.update(paymentDoc.ref, {
                    status: "failed_limit_reached",
                    rawEventType: "failed_limit_reached",
                    updatedAt: admin.firestore.FieldValue.serverTimestamp()
                });
                return { success: false, isElite: false, error: "Elite Membership Full" };
            }

            // Update config
            transaction.update(configRef, {
                occupiedSeats: admin.firestore.FieldValue.increment(1),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // Update payment doc
            transaction.update(paymentDoc.ref, {
                status: "success",
                paymentId: `txn_${Date.now()}`,
                verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
                rawEventType: "success"
            });

            // Update user subscription
            const subscriptionRef = db.collection('users').doc(uid).collection('subscription').doc('current');
            const now = new Date();
            const nextBilling = new Date(now);
            nextBilling.setMonth(now.getMonth() + 1);

            transaction.set(subscriptionRef, {
                isElite: true,
                plan: "elite_3300",
                startedAt: admin.firestore.FieldValue.serverTimestamp(),
                expiresAt: admin.firestore.Timestamp.fromDate(nextBilling),
                minutesRemaining: 600,
                monthlyMinutes: 600,
                autoRenew: true,
                paymentStatus: "active",
                mandateId: `mandate_${uid}`,
                recurringStatus: "active",
                nextBillingDate: admin.firestore.Timestamp.fromDate(nextBilling),
                lastPaymentId: `txn_${Date.now()}`,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });

            return { success: true, isElite: true };
        });

        return result;

    } catch (error) {
        console.error('Error verifying Elite payment:', error);
        throw new functions.https.HttpsError('internal', error.message || 'Unable to verify payment.');
    }
});

exports.bookEliteSession = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const { duration, date, time, slotId } = data;

    if (!duration || ![15, 30, 45, 60].includes(duration)) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid duration.');
    }

    if (!date || !time) {
        throw new functions.https.HttpsError('invalid-argument', 'Missing date or time.');
    }

    const db = admin.firestore();

    try {
        const result = await db.runTransaction(async (transaction) => {
            const subscriptionRef = db.collection('users').doc(uid).collection('subscription').doc('current');
            const subscriptionDoc = await transaction.get(subscriptionRef);

            if (!subscriptionDoc.exists) {
                throw new functions.https.HttpsError('failed-precondition', 'No active subscription found.');
            }

            const subscriptionData = subscriptionDoc.data();

            if (!subscriptionData.isElite || subscriptionData.paymentStatus !== 'active') {
                throw new functions.https.HttpsError('failed-precondition', 'Subscription is not active.');
            }

            if (subscriptionData.minutesRemaining < duration) {
                 throw new functions.https.HttpsError('failed-precondition', 'Not enough minutes remaining.');
            }

            // Deduct minutes
            transaction.update(subscriptionRef, {
                minutesRemaining: admin.firestore.FieldValue.increment(-duration),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // Create session
            const sessionRef = db.collection('elite_sessions').doc();
            transaction.set(sessionRef, {
                uid: uid,
                selectedDate: admin.firestore.Timestamp.fromMillis(date),
                selectedTime: time,
                slotId: slotId || `slot_${Date.now()}`,
                status: "pending",
                minutesBooked: duration,
                meetingLink: null,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                approvedAt: null,
                completedAt: null,
                cancelledAt: null
            });

            return { success: true, sessionId: sessionRef.id };
        });

        return result;

    } catch (error) {
        console.error('Error booking elite session:', error);
        throw new functions.https.HttpsError('internal', error.message || 'Unable to book session.');
    }
});

exports.cancelEliteSubscription = functions.region('asia-south1').https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const uid = context.auth.uid;
    const db = admin.firestore();

    try {
        const subscriptionRef = db.collection('users').doc(uid).collection('subscription').doc('current');

        await subscriptionRef.update({
            autoRenew: false,
            recurringStatus: "cancelled",
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        return { success: true };
    } catch (error) {
        console.error('Error cancelling elite subscription:', error);
        throw new functions.https.HttpsError('internal', 'Unable to cancel subscription.');
    }
});
