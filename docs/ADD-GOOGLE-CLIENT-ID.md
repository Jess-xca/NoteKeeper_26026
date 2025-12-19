# 🔑 Add Your Google Client ID - Quick Guide

**You're almost there!** Just need to paste your Client ID in one place.

---

## ✅ Step 1: Copy Your Client ID from Google Cloud

Go to: https://console.cloud.google.com/apis/credentials

Your Client ID looks like:
```
123456789-abcdefghijklmnop.apps.googleusercontent.com
```

**Copy it to clipboard!**

---

## ✅ Step 2: Paste in Frontend Code

**File:** `notekeeper-frontend/src/components/auth/GoogleSignIn.jsx`

**Line 9** - Replace this:
```javascript
const clientId = "YOUR_GOOGLE_CLIENT_ID"; // ⚠️ REPLACE THIS!
```

**With your actual Client ID:**
```javascript
const clientId = "123456789-abcdefghijklmnop.apps.googleusercontent.com";
```

**Full context (lines 3-12):**
```javascript
const GoogleSignIn = ({ onSuccess, onError, disabled = false }) => {
  const handleGoogleLogin = async () => {
    try {
      // IMPORTANT: Replace with YOUR actual Google Client ID
      // Paste your Client ID from Google Cloud Console here:
      const clientId = "PASTE_YOUR_CLIENT_ID_HERE";
      
      // Redirect to Google OAuth consent screen
      const redirectUri = `${window.location.origin}/auth/google/callback`;
```

---

## ✅ Step 3: Save and Reload

1. Save the file
2. Your browser will auto-reload (or refresh manually)
3. Google Sign In button will now work!

---

## 🎯 How Google OAuth + 2FA Works in Your App

**Important:** The 2FA in your app is **email-based 2FA**, not Google's 2FA!

**Flow:**
```
1. Click "Sign in with Google"
   ↓
2. Google login (uses Google's security)
   ↓
3. Google redirects back to your app
   ↓
4. Your backend checks: Does user have 2FA enabled?
   ├─ YES → Send 6-digit code to user's EMAIL → User enters code → Login
   └─ NO  → Login immediately
```

**So:**
- **Google OAuth** = Login method (instead of username/password)
- **Your 2FA** = Additional security layer via EMAIL after Google login
- They work together, but are separate features!

---

## 📝 Correct Test Credentials (from your backend)

**File:** `DataLoader.java`

### Admin User:
```
Username: admin
Password: password123
Role: ADMIN
```

### Editor User (Ketsia):
```
Username: ketsia_keza
Password: password123
Role: EDITOR
```

### Regular User:
```
Username: alice
Password: password123
Role: USER
```

---

## 🧪 Testing Plan

### Test 1: Regular Login
1. Username: `ketsia_keza`
2. Password: `password123`
3. ✅ Should login successfully

### Test 2: Google Sign In (after adding Client ID)
1. Click "Sign in with Google"
2. Select your Google account
3. Grant permissions
4. ✅ Should redirect to dashboard (or 2FA if enabled)

### Test 3: Google OAuth + 2FA
1. Enable 2FA for a user in database
2. Sign in with Google
3. ✅ Should receive 6-digit code via email
4. Enter code
5. ✅ Should login to dashboard

---

## 📂 Files to Edit

### Frontend (paste Client ID):
```
notekeeper-frontend/
└── src/
    └── components/
        └── auth/
            └── GoogleSignIn.jsx  ← LINE 9: Paste Client ID here
```

### Backend (already configured):
```
notekeeper/
└── src/
    └── main/
        └── resources/
            └── application.properties  ← Already has Client ID (line 21)
```

---

## ✅ Verification

**Before adding Client ID:**
- ❌ Error 401: invalid_client

**After adding Client ID:**
- ✅ Google consent screen appears
- ✅ Can login with Google account
- ✅ Redirects back to app successfully

---

## 🎯 Summary

**What you need to do:**
1. Open `GoogleSignIn.jsx`
2. Go to line 9
3. Replace `"YOUR_GOOGLE_CLIENT_ID"` with your actual ID
4. Save
5. Test!

**Your Google Cloud Console is already configured correctly** (you said you added origins and redirect URIs) ✅

Just need to paste the Client ID in the frontend code! 🚀

---

**Example:**

**Before:**
```javascript
const clientId = "YOUR_GOOGLE_CLIENT_ID";
```

**After (your real Client ID):**
```javascript
const clientId = "948888888782-bc8raen6gbf9lm72l3gtlfu270n49j98.apps.googleusercontent.com";
```

That's it! 🎉
