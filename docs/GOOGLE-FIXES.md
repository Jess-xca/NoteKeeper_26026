# 🔧 Quick Fix: Google OAuth Error 400 + 2FA + Auth Bug

**Issues:**
1. ❌ Error 400: redirect_uri_mismatch
2. ❌ 2FA not working with Google login
3. ❌ Dashboard keeps switching to login

---

## Issue 1: Fix redirect_uri_mismatch (2 minutes)

### The Problem
Google says: "redirect_uri doesn't match any authorized URIs"

### The Solution
Add this EXACT URI to Google Cloud Console:

```
http://localhost:3000/auth/google/callback
```

### Steps:

1. **Go to:** https://console.cloud.google.com/apis/credentials

2. **Click** on your OAuth 2.0 Client ID

3. **Scroll to:** "Authorized redirect URIs"

4. **Click:** "+ ADD URI"

5. **Paste EXACTLY:**
   ```
   http://localhost:3000/auth/google/callback
   ```
   
   ⚠️ **Important:** 
   - NO trailing slash `/`
   - MUST be `http` (not `https`) for localhost
   - MUST be exact match (case-sensitive)

6. **Click:** "SAVE"

7. **Wait:** 1-2 minutes for changes to propagate

8. **Test:** Click "Sign in with Google" again

---

## Issue 2: Enable 2FA for Google Login ✅ FIXED

I've updated the GoogleCallback to:
1. Check if user has 2FA enabled in database
2. Send 6-digit PIN to email
3. Show 2FA verification screen
4. Redirect to dashboard after verification

**How it works now:**
```
Google Sign In → User authenticated → 
Backend checks twoFactorEnabled field →
  IF true: Send PIN to email → User enters PIN → Dashboard
  IF false: Dashboard (direct)
```

---

## Issue 3: Fix Dashboard ↔ Login Switching ✅ FIXED

**Problem:** Auth state not persisting, causing redirects

**Fixed by:**
1. Properly storing token and user in localStorage
2. Checking token validity on app load
3. Not redirecting if already authenticated

---

## 📋 Summary of What I Fixed

### GoogleCallback.jsx
- ✅ Added 2FA check after Google login
- ✅ Sends PIN to email if 2FA enabled
- ✅ Redirects to 2FA verification page
- ✅ Proper error handling

### AuthContext.jsx  
- ✅ Fixed token persistence
- ✅ Improved authentication state management
- ✅ Prevents unnecessary redirects

---

## ✅ Final Checklist

- [ ] Add redirect URI to Google Console (you do this)
- [x] 2FA integration for Google login (done)
- [x] Fix auth state persistence (done)
- [ ] Test Google Sign In
- [ ] Test 2FA with Google login
- [ ] Verify dashboard stays stable

---

## 🧪 Testing Steps

### Test 1: Google OAuth
1. Go to login page
2. Click "Sign in with Google"
3. ✅ Should see Google consent screen (not error)
4. Select account
5. ✅ Should redirect back to app

### Test 2: Google + 2FA
1. Set `twoFactorEnabled = true` for a user in database:
   ```sql
   UPDATE users SET two_factor_enabled = true WHERE email = 'jessicairakoze4@gmail.com';
   ```
2. Sign in with Google
3. ✅ Should receive PIN via email
4. Enter PIN
5. ✅ Should reach dashboard

### Test 3: Dashboard Stability
1. Login successfully
2. ✅ Dashboard should stay stable (no redirects)
3. Refresh page
4. ✅ Should remain logged in

---

## 🎯 What You Need to Do

**Only ONE thing:**
1. Go to Google Cloud Console
2. Add this redirect URI: `http://localhost:3000/auth/google/callback`
3. Save and wait 1-2 minutes
4. Test!

Everything else is fixed in the code! 🎉
