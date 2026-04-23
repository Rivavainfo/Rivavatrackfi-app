const functions = require("firebase-functions");
const sgMail = require("@sendgrid/mail");

sgMail.setApiKey(functions.config().sendgrid.key);

const sendWelcomeEmail = async (email) => {
    const msg = {
        to: email,
        from: 'no-reply@rivava.in',
        subject: 'Welcome to Rivava 🚀',
        html: `
            <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #333333; text-align: center;">Welcome to Rivava 🚀</h2>
                    <p style="color: #666666; font-size: 16px; line-height: 1.5;">Hello! We are thrilled to have you here.</p>
                    <p style="color: #666666; font-size: 16px; line-height: 1.5;">Your account with email ${email} has been successfully created.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://rivava.in" style="background-color: #3B82F6; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-size: 16px; font-weight: bold; display: inline-block;">Explore the App</a>
                    </div>
                </div>
            </div>
        `,
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
