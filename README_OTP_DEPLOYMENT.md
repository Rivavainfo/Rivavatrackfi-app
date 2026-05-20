# Custom SMS OTP Setup & Deployment

## 1. Firebase Console Setup
1. Go to the Firebase Console -> Project Settings -> Service accounts.
2. Click "Generate new private key".
3. Save the downloaded JSON file.

## 2. Backend Setup
1. Copy the JSON file downloaded above into the `backend/` directory and rename it to `serviceAccountKey.json`.
2. Ensure you have Node.js installed.
3. Open a terminal and navigate to the backend directory:
   ```bash
   cd backend
   npm install
   ```
4. Create a `.env` file in the `backend/` directory:
   ```env
   PORT=3000
   FAST2SMS_API_KEY=your_fast2sms_api_key_here
   ```
5. Start the local server:
   ```bash
   node server.js
   ```

## 3. Android Setup
1. Ensure your `google-services.json` from the Firebase Console is placed inside the `app/` directory (`app/google-services.json`).
2. Add your local backend URL to `local.properties` (or set an environment variable) to test on an emulator:
   ```properties
   backend.url=http://10.0.2.2:3000/
   ```
   *(For physical devices, replace 10.0.2.2 with your machine's local IP address).*

## 4. Production Deployment
1. Deploy the `backend/` folder to a service like Render, Heroku, or Google Cloud Run.
2. Make sure to configure the `FAST2SMS_API_KEY` and the `serviceAccountKey.json` on the production server.
3. Update `backend.url` in your Android `local.properties` to point to the new production URL (e.g., `https://your-api.onrender.com/`).
