const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const verificationFunctions = require('./verificationFunctions');
const userLifecycleFunctions = require('./userLifecycleFunctions');

exports.sendVerificationEmail = verificationFunctions.sendVerificationEmail;
exports.verify = verificationFunctions.verify;
exports.checkVerification = verificationFunctions.checkVerification;
exports.onUserWrite = userLifecycleFunctions.onUserWrite;
