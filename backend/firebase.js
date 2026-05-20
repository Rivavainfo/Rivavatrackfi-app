const admin = require('firebase-admin');
const path = require('path');

let serviceAccount;
try {
  serviceAccount = require(path.join(__dirname, 'serviceAccountKey.json'));
} catch (error) {
  console.warn("Warning: serviceAccountKey.json not found in backend directory. Make sure to place it there before running the server.");
}

if (serviceAccount) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} else {
  // Fallback for initializing without specific cert if environment variables are used
  admin.initializeApp();
}

const db = admin.firestore();

module.exports = { admin, db };
