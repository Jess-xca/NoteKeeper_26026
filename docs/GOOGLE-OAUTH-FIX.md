# 🔧 Google Sign In Error Fix - 401: invalid_client

**Error:** "The OAuth client was not found"  
**Cause:** You're using a placeholder Google Client ID that isn't registered with Google

---

## ⚡ Quick Fix (2 Options)

### Option 1: Hide Google Button (Fastest - 30 seconds)

If you don't need Google Sign-In for your demo:

**File:** `src/components/auth/GoogleSignIn.jsx`

**Add this at the top of the component:**
```javascript
const GoogleSignIn = ({ onSuccess, onError, disabled = false }) => {
  // Temporarily disable Google Sign In
  return null;  // ← Add this line to hide the button
  
  // ... rest of code
};
```

Or in **Login.jsx**, comment out the Google button:
```javascript
{/* Google Sign In */}
{/* <GoogleSignIn disabled={loading} /> */}
```

---

### Option 2: Get Real Google Client ID (5 minutes)

Follow these steps to get a working Google Client ID:

#### Step 1: Go to Google Cloud Console
**URL:** https://console.cloud.google.com

#### Step 2: Create/Select Project
1. Click **Select a project** (top left)
2. Click **NEW PROJECT**
3. Name: "NoteKeeper"
4. Click **CREATE**

#### Step 3: Enable Google+ API
1. Go to **APIs & Services** → **Library**
2. Search for "Google+ API"
3. Click it and press **ENABLE**

#### Step 4: Create OAuth Credentials
1. Go to **APIs & Services** → **Credentials**
2. Click **+ CREATE CREDENTIALS** → **OAuth client ID**
3. If prompted, configure the OAuth consent screen:
   - User Type: **External**
   - App name: **NoteKeeper**
   - User support email: Your email
   - Developer contact: Your email
   - Click **SAVE AND CONTINUE** through all steps

#### Step 5: Configure OAuth Client
1. Application type: **Web application**
2. Name: **NoteKeeper Web Client**
3. **Authorized JavaScript origins:**
   ```
   http://localhost:3000
   ```
4. **Authorized redirect URIs:**
   ```
   http://localhost:3000/auth/google/callback
   http://localhost:8080/api/auth/google-callback
   ```
5. Click **CREATE**

#### Step 6: Copy Client ID
You'll see a popup with:
- **Client ID** (looks like: `123456789-abcdefg.apps.googleusercontent.com`)
- **Client Secret** (not needed for frontend)

**Copy the Client ID!**

#### Step 7: Update Your Code

**File:** `src/components/auth/GoogleSignIn.jsx`

**Line 7 - Replace:**
```javascript
const clientId = "YOUR_GOOGLE_CLIENT_ID"; // ← REPLACE THIS
```

**With your actual Client ID:**
```javascript
const clientId = "123456789-abcdefg.apps.googleusercontent.com"; // Your real ID
```

#### Step 8: Update Backend (Optional)

**File:** `src/main/resources/application.properties`

**Update:**
```properties
google.client.id=YOUR_ACTUAL_CLIENT_ID_HERE
```

#### Step 9: Restart Frontend
```bash
# Stop current server (Ctrl+C)
npm start
```

---

## ✅ Verification

After updating the Client ID:

1. Go to login page
2. Click "Sign in with Google"
3. **Expected:** Google consent screen appears
4. Select your Google account
5. **Expected:** Redirects back to your app

---

## 🎯 What Each Part Does

**Client ID:**
- Identifies your app to Google
- Must be from Google Cloud Console
- Different for each project/domain

**Redirect URIs:**
- Where Google sends users after login
- Must match EXACTLY what's registered
- Case-sensitive!

**JavaScript Origins:**
- Domains allowed to use this Client ID
- For localhost: `http://localhost:3000`
- For production: `https://yourdomain.com`

---

## 🚨 Common Mistakes

### ❌ Using Placeholder ID
```javascript
const clientId = "YOUR_GOOGLE_CLIENT_ID"; // Won't work!
```

### ❌ Wrong Redirect URI
If registered: `http://localhost:3000/auth/google/callback`  
But code uses: `http://localhost:3000/callback` ← Error!

### ❌ HTTP vs HTTPS
Registered: `https://localhost:3000`  
Actual: `http://localhost:3000` ← Mismatch!

### ✅ Correct Configuration
```javascript
const clientId = "123456789-abc.apps.googleusercontent.com";
const redirectUri = `http://localhost:3000/auth/google/callback`;
```

---

## 🔍 Debugging

### Check Current Client ID
**File:** `src/components/auth/GoogleSignIn.jsx` (Line 7)

If it says `"YOUR_GOOGLE_CLIENT_ID"` → Not configured yet!

### Check Google Cloud Console
1. Go to Credentials page
2. Find your OAuth 2.0 Client ID
3. Click the pencil icon to edit
4. Verify redirect URIs match your code

### Check Browser Console
Press F12 → Console tab → Look for errors like:
- `invalid_client` → Wrong Client ID
- `redirect_uri_mismatch` → URI not registered
- `access_denied` → User cancelled login

---

## 💡 For Development/Demo

**If you don't have time to set up Google OAuth:**

1. **Option A:** Hide the Google button (see Option 1 above)
2. **Option B:** Use regular username/password login

**Test Users:**
- Username: `admin` Password: `password` (Admin role)
- Username: `ketsia` Password: `password` (Editor role)
- Username: `alice` Password: `password` (User role)

These work without any Google setup!

---

## 📝 Summary

**The Problem:**
```
"YOUR_GOOGLE_CLIENT_ID" ← Placeholder, not a real ID
```

**The Solution:**
```
"123456789-abc.apps.googleusercontent.com" ← Real ID from Google
```

**Time to Fix:**
- Hide button: 30 seconds
- Get real Client ID: 5 minutes

---

## 🎓 For Academic Projects

Most professors accept:
- ✅ Username/password login (already working!)
- ✅ 2FA with email (already working!)
- ⚠️ Google OAuth (optional bonus feature)

**You already have 2 working auth methods!** Google is just an extra.

---

**Quick Decision:**
- **Need Google for demo?** → Follow Option 2 (5 min setup)
- **Just demo auth features?** → Use username/password + 2FA (already working!)
- **No time now?** → Hide Google button (Option 1)

All your other features work perfectly without Google OAuth!
