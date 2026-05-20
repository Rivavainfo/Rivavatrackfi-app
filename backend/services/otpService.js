const crypto = require('crypto');
const axios = require('axios');

/**
 * Generates a random 6-digit OTP.
 * @returns {string} The generated OTP.
 */
function generateOTP() {
  return crypto.randomInt(100000, 999999).toString();
}

/**
 * Sends an SMS OTP using the Fast2SMS API.
 * @param {string} phone The destination phone number.
 * @param {string} otp The OTP to send.
 * @returns {Promise<boolean>} True if the SMS was sent successfully, false otherwise.
 */
async function sendSmsOTP(phone, otp) {
  try {
    const message = `Your Rivava OTP is ${otp}`;
    const url = 'https://www.fast2sms.com/dev/bulkV2';

    const response = await axios.get(url, {
      headers: {
        authorization: process.env.FAST2SMS_API_KEY
      },
      params: {
        variables_values: otp,
        route: 'otp',
        numbers: phone,
      }
    });

    if (response.data && response.data.return === true) {
      console.log(`Successfully sent OTP to ${phone}`);
      return true;
    } else {
      console.error(`Failed to send OTP to ${phone}:`, response.data);
      return false;
    }
  } catch (error) {
    console.error(`Error sending SMS to ${phone}:`, error.message);
    if (error.response) {
      console.error('Fast2SMS Error Response:', error.response.data);
    }
    return false;
  }
}

module.exports = {
  generateOTP,
  sendSmsOTP
};
