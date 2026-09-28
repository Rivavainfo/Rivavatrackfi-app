const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");
const { sendVerificationEmail } = require("./services/emailService");

// 1. sendVerificationEmail
exports.sendVerificationEmail = functions.https.onRequest(async (req, res) => {
    // Basic Request Validation
    if (req.method !== 'POST') {
        return res.status(405).send({ error: 'Method Not Allowed' });
    }

    if (!req.is('application/json')) {
        return res.status(415).send({ error: 'Unsupported Media Type, expected application/json' });
    }

    try {
        const { email, uid } = req.body;
        if (!email || !uid) {
            return res.status(400).send({ error: 'Missing email or uid' });
        }

        const db = admin.firestore();

        // Rate Limiting: Check if we sent an email in the last 60 seconds
        // Use a transaction to ensure concurrency safety
        const verificationRef = db.collection('email_verifications').doc(uid);

        const token = crypto.randomBytes(32).toString('hex');
        const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

        const expiresAt = new Date();
        expiresAt.setMinutes(expiresAt.getMinutes() + 15);

        await db.runTransaction(async (transaction) => {
            const existingDoc = await transaction.get(verificationRef);
            if (existingDoc.exists) {
                const data = existingDoc.data();
                const now = new Date();
                const createdAt = data.createdAt ? data.createdAt.toDate() : new Date(0);
                const timeDiff = (now.getTime() - createdAt.getTime()) / 1000;

                if (timeDiff < 60) {
                    throw new Error('RATE_LIMIT_EXCEEDED');
                }
            }

            transaction.set(verificationRef, {
                uid,
                email,
                tokenHash,
                expiresAt,
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
        });

        const verifyLink = `https://rivava.in/verify?token=${token}`;
        const sent = await sendVerificationEmail(email, verifyLink);

        if (!sent) {
            return res.status(500).send({ error: 'Failed to deliver verification email' });
        }

        return res.status(200).send({ success: true, message: 'Verification email sent' });
    } catch (error) {
        if (error.message === 'RATE_LIMIT_EXCEEDED') {
            return res.status(429).send({ error: 'Please wait 60 seconds before requesting another email.' });
        }
        console.error('Error sending verification email:', error);
        return res.status(500).send({ error: 'Failed to send email' });
    }
});

// 2. verify
exports.verify = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'GET') {
        return res.status(405).send({ error: 'Method Not Allowed' });
    }

    try {
        const token = req.query.token;
        if (!token) {
            return res.status(400).send({ error: 'Missing token' });
        }

        const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

        const db = admin.firestore();
        const snapshot = await db.collection('email_verifications')
            .where('tokenHash', '==', tokenHash)
            .get();

        if (snapshot.empty) {
            return res.status(400).send({ error: 'Invalid or expired token' });
        }

        const doc = snapshot.docs[0];
        const data = doc.data();

        if (data.expiresAt.toDate() < new Date()) {
            await doc.ref.delete();
            return res.status(400).send({ error: 'Token expired' });
        }

        await db.collection('users').doc(data.uid).set({
            isVerified: true,
            email_verified: true,
            email: data.email,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        await admin.auth().updateUser(data.uid, {
            emailVerified: true
        });

        await doc.ref.delete();

        // Welcome email is automatically handled via onUserWrite trigger

        // Render sleek success page
        return res.status(200).send(`
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Verified - Rivava</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            background-color: #0F172A;
                            color: #F8FAFC;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            background-color: #1E293B;
                            padding: 40px;
                            border-radius: 12px;
                            box-shadow: 0 10px 25px rgba(0,0,0,0.3);
                            text-align: center;
                            max-width: 480px;
                            border: 1px solid #334155;
                        }
                        .icon {
                            font-size: 48px;
                            margin-bottom: 16px;
                        }
                        h1 {
                            color: #38BDF8;
                            margin: 0 0 12px 0;
                            font-size: 24px;
                        }
                        p {
                            color: #94A3B8;
                            margin: 0 0 24px 0;
                            line-height: 1.6;
                            font-size: 15px;
                        }
                        .badge {
                            display: inline-block;
                            background: rgba(56, 189, 248, 0.1);
                            color: #38BDF8;
                            padding: 8px 16px;
                            border-radius: 9999px;
                            font-size: 14px;
                            font-weight: 600;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="icon">✨</div>
                        <h1>Email Verified Successfully!</h1>
                        <p>Your Rivava account has been verified. You're all set to take control of your financial tracking.</p>
                        <div class="badge">You can now open the Rivava app to log in</div>
                    </div>
                </body>
            </html>
        `);
    } catch (error) {
        console.error('Error verifying email:', error);
        return res.status(500).send({ error: 'Internal Server Error' });
    }
});

// 3. checkVerification
exports.checkVerification = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'GET') {
        return res.status(405).send({ error: 'Method Not Allowed' });
    }

    try {
        const uid = req.query.uid;
        if (!uid) {
            return res.status(400).send({ error: 'Missing uid' });
        }

        const db = admin.firestore();
        const userDoc = await db.collection('users').doc(uid).get();
        if (!userDoc.exists) {
            return res.status(404).send({ error: 'User not found' });
        }

        const userData = userDoc.data();
        const isVerified = userData.isVerified === true || userData.email_verified === true;
        return res.status(200).send({ isVerified });
    } catch (error) {
        console.error('Error checking verification:', error);
        return res.status(500).send({ error: 'Internal Server Error' });
    }
});