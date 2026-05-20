const express = require('express');
const router = express.Router();
const { admin, db } = require('../firebase');
const { generateOTP, sendSmsOTP } = require('../services/otpService');

// Constants
const OTP_EXPIRY_MINUTES = 5;
const MAX_ATTEMPTS = 3;
const RESEND_COOLDOWN_SECONDS = 60;

// Helper to format phone number to E.164 (simplistic check for 10 digits)
function sanitizePhone(phone) {
  if (!phone) return null;
  const digits = phone.replace(/\D/g, '');
  if (digits.length === 10) {
    return '+91' + digits; // Default to India country code if 10 digits
  } else if (digits.length > 10) {
    return '+' + digits;
  }
  return null;
}

// 1. POST /send-otp
router.post('/send-otp', async (req, res) => {
  try {
    let { phone } = req.body;

    if (!phone) {
      return res.status(400).json({ success: false, error: 'Phone number is required.' });
    }

    const sanitizedPhone = sanitizePhone(phone);
    if (!sanitizedPhone) {
      return res.status(400).json({ success: false, error: 'Invalid phone number format.' });
    }

    const otpRef = db.collection('otps').doc(sanitizedPhone);
    const docSnap = await otpRef.get();

    let attempts = 0;
    let resendCount = 0;

    if (docSnap.exists) {
      const data = docSnap.data();
      const now = Date.now();
      const createdAt = data.createdAt;

      // Check if within expiry
      if (now - createdAt < OTP_EXPIRY_MINUTES * 60 * 1000) {
        // Enforce cooldown
        if (now - createdAt < RESEND_COOLDOWN_SECONDS * 1000) {
          return res.status(429).json({
            success: false,
            error: `Please wait ${RESEND_COOLDOWN_SECONDS} seconds before requesting a new OTP.`
          });
        }
      }

      attempts = data.attempts || 0;
      resendCount = (data.resendCount || 0) + 1;
    }

    const otp = generateOTP();
    // In a real Fast2SMS account, the 'route: otp' requires a variables array or specific format.
    // The instructions say use: "Your Rivava OTP is XXXXX"
    // Using fast2sms bulkV2 we'll rely on the service implementation.

    const smsSent = await sendSmsOTP(sanitizedPhone.replace('+91', ''), otp); // Fast2SMS generally expects 10 digits without +91

    if (!smsSent) {
      return res.status(500).json({ success: false, error: 'Failed to send SMS.' });
    }

    await otpRef.set({
      otp: otp,
      createdAt: Date.now(),
      attempts: attempts,
      resendCount: resendCount
    });

    return res.status(200).json({ success: true, message: 'OTP sent successfully.' });

  } catch (error) {
    console.error('Error in /send-otp:', error);
    return res.status(500).json({ success: false, error: 'Internal server error.' });
  }
});

// 2. POST /verify-otp
router.post('/verify-otp', async (req, res) => {
  try {
    const { phone, otp } = req.body;

    if (!phone || !otp) {
      return res.status(400).json({ success: false, error: 'Phone number and OTP are required.' });
    }

    const sanitizedPhone = sanitizePhone(phone);
    if (!sanitizedPhone) {
      return res.status(400).json({ success: false, error: 'Invalid phone number format.' });
    }

    const otpRef = db.collection('otps').doc(sanitizedPhone);
    const docSnap = await otpRef.get();

    if (!docSnap.exists) {
      return res.status(400).json({ success: false, error: 'OTP not found or expired.' });
    }

    const data = docSnap.data();
    const now = Date.now();

    // Check expiry
    if (now - data.createdAt > OTP_EXPIRY_MINUTES * 60 * 1000) {
      await otpRef.delete();
      return res.status(400).json({ success: false, error: 'OTP expired.' });
    }

    // Check attempts
    if (data.attempts >= MAX_ATTEMPTS) {
      await otpRef.delete();
      return res.status(400).json({ success: false, error: 'Maximum attempts reached. Please request a new OTP.' });
    }

    // Verify OTP
    if (data.otp !== otp) {
      await otpRef.update({ attempts: admin.firestore.FieldValue.increment(1) });
      return res.status(400).json({ success: false, error: 'Invalid OTP.' });
    }

    // OTP verified successfully
    await otpRef.delete();

    // Create Firebase Custom Token
    // We use the sanitized phone as the UID. Or you can generate a new UID if needed.
    // For phone auth, standard Firebase uses the E.164 phone as the primary identifier if no other email exists.
    const customToken = await admin.auth().createCustomToken(sanitizedPhone);

    return res.status(200).json({
      success: true,
      message: 'OTP verified successfully.',
      token: customToken
    });

  } catch (error) {
    console.error('Error in /verify-otp:', error);
    return res.status(500).json({ success: false, error: 'Internal server error.' });
  }
});

module.exports = router;
