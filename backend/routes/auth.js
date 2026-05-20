const express = require('express');
const axios = require('axios');
const { db, auth } = require('../firebase');
const router = express.Router();

const OTP_EXPIRY_MINUTES = 5;
const MAX_ATTEMPTS = 3;
const MAX_RESENDS = 3;

function generateOTP() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

async function sendSMSViaFast2SMS(phone, otp) {
    const apiKey = process.env.FAST2SMS_API_KEY;
    if (!apiKey) {
        throw new Error("FAST2SMS_API_KEY is not configured.");
    }
    const url = 'https://www.fast2sms.com/dev/bulkV2';
    const cleanPhone = phone.replace(/^\+91/, '');
    const params = new URLSearchParams({
        authorization: apiKey,
        variables_values: otp,
        route: 'otp',
        numbers: cleanPhone
    });
    const response = await axios.get(`${url}?${params.toString()}`);
    if (response.data && response.data.return === false) {
        throw new Error(`Fast2SMS Error: ${response.data.message}`);
    }
    return response.data;
}

router.post('/send-otp', async (req, res) => {
    const { phone } = req.body;
    if (!phone) return res.status(400).json({ error: 'Phone number is required.' });

    try {
        const otpRef = db.collection('otps').doc(phone);
        const docSnap = await otpRef.get();
        let resendCount = 0;

        if (docSnap.exists) {
            const data = docSnap.data();
            const now = Date.now();
            const diffMinutes = (now - data.createdAt) / (1000 * 60);
            if (diffMinutes < 1) return res.status(429).json({ error: 'Please wait before requesting a new OTP.' });
            resendCount = data.resendCount || 0;
            if (resendCount >= MAX_RESENDS && diffMinutes < OTP_EXPIRY_MINUTES) {
                 return res.status(429).json({ error: 'Maximum resend limit reached. Try again later.' });
            }
        }

        const otp = generateOTP();
        const createdAt = Date.now();
        await otpRef.set({ otp: otp, createdAt: createdAt, attempts: 0, resendCount: resendCount + 1 });
        await sendSMSViaFast2SMS(phone, otp);
        res.status(200).json({ message: 'OTP sent successfully.' });
    } catch (error) {
        console.error("Error sending OTP:", error);
        res.status(500).json({ error: 'Failed to send OTP.' });
    }
});

router.post('/verify-otp', async (req, res) => {
    const { phone, otp } = req.body;
    if (!phone || !otp) return res.status(400).json({ error: 'Phone and OTP are required.' });

    try {
        const otpRef = db.collection('otps').doc(phone);
        const docSnap = await otpRef.get();
        if (!docSnap.exists) return res.status(400).json({ error: 'No active OTP found for this number.' });

        const data = docSnap.data();
        const now = Date.now();
        const diffMinutes = (now - data.createdAt) / (1000 * 60);
        if (diffMinutes > OTP_EXPIRY_MINUTES) {
            await otpRef.delete();
            return res.status(400).json({ error: 'OTP has expired.' });
        }
        if (data.attempts >= MAX_ATTEMPTS) {
            await otpRef.delete();
            return res.status(400).json({ error: 'Maximum attempts reached. Please request a new OTP.' });
        }
        if (data.otp !== otp) {
            await otpRef.update({ attempts: data.attempts + 1 });
            return res.status(400).json({ error: 'Invalid OTP.' });
        }

        await otpRef.delete();
        let uid = phone;
        if (!uid.startsWith('+')) uid = `+${uid}`;

        let userRecord;
        try {
            userRecord = await auth.getUserByPhoneNumber(uid);
        } catch (error) {
            if (error.code === 'auth/user-not-found') {
                userRecord = await auth.createUser({ phoneNumber: uid });
            } else {
                throw error;
            }
        }
        const customToken = await auth.createCustomToken(userRecord.uid);
        res.status(200).json({ token: customToken, message: 'OTP verified successfully.' });
    } catch (error) {
        console.error("Error verifying OTP:", error);
        res.status(500).json({ error: 'Internal server error during verification.' });
    }
});
module.exports = router;
