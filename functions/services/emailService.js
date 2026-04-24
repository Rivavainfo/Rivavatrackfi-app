const functions = require("firebase-functions");
const sgMail = require("@sendgrid/mail");
const { getWelcomeEmailHtml } = require("../mailTemplates");

// Ensure we don't crash when running locally without config
try {
    sgMail.setApiKey(functions.config().sendgrid.key);
} catch (e) {
    console.warn("Could not load functions.config().sendgrid.key, this is expected in tests.");
}

const sendWelcomeEmail = async (email) => {
    const msg = {
        to: email,
        from: 'no-reply@rivava.in',
        subject: 'Welcome to Rivava 🚀',
        html: getWelcomeEmailHtml(email),
    };

    try {
        await sgMail.send(msg);
        console.log(`Welcome email sent successfully to ${email}`);
        return true;
    } catch (error) {
        console.error('Error sending welcome email:', error);
        return false;
    }
};

module.exports = {
    sendWelcomeEmail
};