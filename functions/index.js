const functions = require("firebase-functions");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");
const crypto = require("crypto");

admin.initializeApp();
sgMail.setApiKey(functions.config().sendgrid.key);

const db = admin.firestore();

// 1. sendVerificationEmail
exports.sendVerificationEmail = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'POST') {
        return res.status(405).send('Method Not Allowed');
    }

    try {
        const { email, uid } = req.body;
        if (!email || !uid) {
            return res.status(400).send('Missing email or uid');
        }

        const token = crypto.randomBytes(32).toString('hex');
        const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

        const expiresAt = new Date();
        expiresAt.setMinutes(expiresAt.getMinutes() + 15);

        await db.collection('email_verifications').doc(uid).set({
            uid,
            email,
            tokenHash,
            expiresAt,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        const verifyLink = `https://rivava.in/verify?token=${token}`;

        const msg = {
            to: email,
            from: 'no-reply@rivava.in',
            subject: 'Verify your email for Rivava',
            html: `
                <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #333333; text-align: center;">Welcome to Rivava</h2>
                        <p style="color: #666666; font-size: 16px; line-height: 1.5;">Please verify your email address by clicking the button below.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="${verifyLink}" style="background-color: #3B82F6; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-size: 16px; font-weight: bold; display: inline-block;">Verify Email</a>
                        </div>
                        <p style="color: #999999; font-size: 14px; text-align: center;">If you didn't create an account, you can safely ignore this email.</p>
                    </div>
                </div>
            `,
        };

        await sgMail.send(msg);

        return res.status(200).send({ success: true, message: 'Verification email sent' });
    } catch (error) {
        console.error('Error sending verification email:', error);
        return res.status(500).send({ error: 'Failed to send email' });
    }
});

// 2. verify
exports.verify = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'GET') {
        return res.status(405).send('Method Not Allowed');
    }

    try {
        const token = req.query.token;
        if (!token) {
            return res.status(400).send('Missing token');
        }

        const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

        const snapshot = await db.collection('email_verifications')
            .where('tokenHash', '==', tokenHash)
            .get();

        if (snapshot.empty) {
            return res.status(400).send('Invalid or expired token');
        }

        const doc = snapshot.docs[0];
        const data = doc.data();

        if (data.expiresAt.toDate() < new Date()) {
            await doc.ref.delete();
            return res.status(400).send('Token expired');
        }

        await db.collection('users').doc(data.uid).update({
            isVerified: true
        });

        await doc.ref.delete();

        // Send Welcome Email
        const welcomeMsg = {
            to: data.email,
            from: 'no-reply@rivava.in',
            subject: 'Welcome to Rivava 🎉',
            html: `
                <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #333333; text-align: center;">Welcome to Rivava 🎉</h2>
                        <p style="color: #666666; font-size: 16px; line-height: 1.5;">Your email has been successfully verified! You can now access all features of the app.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="https://rivava.in" style="background-color: #3B82F6; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-size: 16px; font-weight: bold; display: inline-block;">Open App</a>
                        </div>
                    </div>
                </div>
            `,
        };

        await sgMail.send(welcomeMsg);

        // Render success page
        return res.status(200).send(`
            <html>
                <head>
                    <title>Email Verified</title>
                    <style>
                        body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f4f4f4; margin: 0; }
                        .container { background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); text-align: center; }
                        h1 { color: #34D399; }
                        p { color: #666; margin-bottom: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Email Verified!</h1>
                        <p>Your email has been successfully verified.</p>
                        <p>You can now return to the Rivava app and log in.</p>
                    </div>
                </body>
            </html>
        `);
    } catch (error) {
        console.error('Error verifying email:', error);
        return res.status(500).send('Internal Server Error');
    }
});

// 3. checkVerification
exports.checkVerification = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'GET') {
        return res.status(405).send('Method Not Allowed');
    }

    try {
        const uid = req.query.uid;
        if (!uid) {
            return res.status(400).send('Missing uid');
        }

        const userDoc = await db.collection('users').doc(uid).get();
        if (!userDoc.exists) {
            return res.status(404).send('User not found');
        }

        const userData = userDoc.data();
        return res.status(200).send({ isVerified: userData.isVerified === true });
    } catch (error) {
        console.error('Error checking verification:', error);
        return res.status(500).send({ error: 'Internal Server Error' });
    }
});
