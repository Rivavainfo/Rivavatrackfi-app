const functions = require("firebase-functions");
const { Resend } = require("resend");
const sgMail = require("@sendgrid/mail");
const {
    getWelcomeEmailHtml,
    getWelcomeEmailText,
    getVerificationEmailHtml,
    getVerificationEmailText,
    getLoginNotificationEmailHtml,
    getLoginNotificationEmailText
} = require("../mailTemplates");

const getResendKey = () => {
    if (process.env.RESEND_API_KEY) {
        return process.env.RESEND_API_KEY;
    }
    try {
        if (typeof functions.config === 'function') {
            const config = functions.config();
            if (config && config.resend && config.resend.key) {
                return config.resend.key;
            }
        }
    } catch (e) {
        // Ignore
    }
    return null;
};

const getSendGridKey = () => {
    if (process.env.SENDGRID_API_KEY) {
        return process.env.SENDGRID_API_KEY;
    }
    try {
        if (typeof functions.config === 'function') {
            const config = functions.config();
            if (config && config.sendgrid && config.sendgrid.key) {
                return config.sendgrid.key;
            }
        }
    } catch (e) {
        // Ignore
    }
    return null;
};

const getSenderEmail = (isResend = true) => {
    if (isResend) {
        return process.env.RESEND_FROM_EMAIL || 'Rivava TrackFi <onboarding@resend.dev>';
    }
    return process.env.SENDGRID_FROM_EMAIL || 'no-reply@rivava.in';
};

/**
 * Universal internal dispatcher: Tries Resend first, fallbacks to SendGrid
 */
const sendMailInternal = async ({ to, subject, text, html }) => {
    const resendKey = getResendKey();
    if (resendKey) {
        try {
            const resend = new Resend(resendKey);
            const data = await resend.emails.send({
                from: getSenderEmail(true),
                to: Array.isArray(to) ? to : [to],
                subject: subject,
                text: text,
                html: html
            });

            if (data && data.data && data.data.id) {
                console.log(`[Resend] Email delivered successfully to ${to}, ID: ${data.data.id}`);
                return true;
            } else if (data && data.error) {
                console.warn(`[Resend] Error response:`, data.error);
            }
        } catch (resendError) {
            console.error(`[Resend] Exception sending email to ${to}:`, resendError?.message || resendError);
        }
    }

    // Fallback: SendGrid
    const sgKey = getSendGridKey();
    if (sgKey) {
        try {
            sgMail.setApiKey(sgKey);
            await sgMail.send({
                to: to,
                from: getSenderEmail(false),
                subject: subject,
                text: text,
                html: html
            });
            console.log(`[SendGrid] Email sent successfully to ${to}`);
            return true;
        } catch (sgError) {
            console.error(`[SendGrid] Exception sending email to ${to}:`, sgError?.response?.body || sgError?.message || sgError);
        }
    }

    return false;
};

/**
 * Sends a welcome and thank you email to newly registered or verified users.
 */
const sendWelcomeEmail = async (email, name = '') => {
    if (!email) return false;
    return await sendMailInternal({
        to: email,
        subject: 'Welcome to Rivava TrackFi 🎉',
        text: getWelcomeEmailText(email, name),
        html: getWelcomeEmailHtml(email, name)
    });
};

/**
 * Sends an email verification link during signup or confirmation.
 */
const sendVerificationEmail = async (email, verifyLink) => {
    if (!email || !verifyLink) return false;
    return await sendMailInternal({
        to: email,
        subject: 'Verify your email for Rivava TrackFi',
        text: getVerificationEmailText(email, verifyLink),
        html: getVerificationEmailHtml(email, verifyLink)
    });
};

/**
 * Sends a login notification email if security alerts are enabled.
 */
const sendLoginNotificationEmail = async (email, name = '', details = {}) => {
    if (!email) return false;
    return await sendMailInternal({
        to: email,
        subject: 'Rivava Security Alert: New Login Detected',
        text: getLoginNotificationEmailText(email, name, details),
        html: getLoginNotificationEmailHtml(email, name, details)
    });
};

module.exports = {
    sendWelcomeEmail,
    sendVerificationEmail,
    sendLoginNotificationEmail
};
