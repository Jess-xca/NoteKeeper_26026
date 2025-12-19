# 📧 Gmail SMTP Configuration Guide - Step by Step

**Time Required:** 5 minutes  
**Difficulty:** Easy

---

## ⚡ Quick Steps

### Step 1: Enable 2-Step Verification (2 minutes)

1. **Go to:** https://myaccount.google.com/security
2. **Find:** "2-Step Verification" section
3. **Click:** "Get Started" or "Turn On"
4. **Follow:** The prompts to set up 2-Step Verification
   - You'll need your phone number
   - You'll receive a verification code
   - Enter the code to confirm

### Step 2: Generate App Password (2 minutes)

1. **Go to:** https://myaccount.google.com/apppasswords
   - Or search "app passwords" in Google Account settings
2. **Click:** "Select app" dropdown → Choose "Mail"
3. **Click:** "Select device" dropdown → Choose "Other (Custom name)"
4. **Type:** "NoteKeeper" as the device name
5. **Click:** "Generate"
6. **IMPORTANT:** Copy the 16-character password shown
   - It will look like: `abcd efgh ijkl mnop`
   - **Save this immediately!** You can't see it again

### Step 3: Update application.properties (1 minute)

**File to edit:** `notekeeper/src/main/resources/application.properties`

**Find these lines:**
```properties
spring.mail.username=notekeeper.app@gmail.com
spring.mail.password=your-app-specific-password
```

**Replace with YOUR details:**
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

**Example:**
```properties
spring.mail.username=john.doe@gmail.com
spring.mail.password=xmpl qwer tyui asdf
```

### Step 4: Restart Backend

```bash
cd notekeeper
mvn spring-boot:run
```

**That's it!** Emails will now work! 🎉

---

## 🧪 Test It Works

### Test Password Reset:
1. Go to `http://localhost:3000/forgot-password`
2. Enter a test user's email (e.g., `alice@example.com`)
3. Click "Send Reset Link"
4. **Check your configured Gmail inbox**
5. You should see the reset email!

### Test 2FA:
1. Login to the app
2. System will send 6-digit code
3. **Check your Gmail inbox**
4. You should see the 2FA code!

---

## ❌ Troubleshooting

### "Authentication failed"
- ✅ Make sure you used the **app password**, not your regular Gmail password
- ✅ Copy the app password **without spaces**: `abcdefghijklmnop`
- ✅ Make sure 2-Step Verification is enabled

### "Username or password not accepted"
- ✅ Double-check the email address is correct
- ✅ Generate a new app password and try again

### Emails not sending
- ✅ Check backend console for error messages
- ✅ Verify `spring.mail.host=smtp.gmail.com` is correct
- ✅ Verify `spring.mail.port=587` is correct

---

## 📝 Complete Configuration Reference

**Your final application.properties should have:**

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

---

## ✅ Verification Checklist

- [ ] 2-Step Verification enabled on Gmail
- [ ] App password generated (16 characters)
- [ ] App password copied (no spaces)
- [ ] application.properties updated with your Gmail
- [ ] application.properties updated with app password
- [ ] Backend restarted
- [ ] Test email sent successfully

---

## 🔒 Security Notes

- ✅ **App passwords are safer** than using your main Gmail password
- ✅ You can revoke app passwords anytime from Google Account
- ✅ Each app should have its own app password
- ✅ Never commit passwords to Git (use environment variables in production)

---

## 🚀 Production Deployment

For production, use environment variables instead of hardcoding:

```properties
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

Then set environment variables:
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

---

## 📧 Email Templates Being Used

Your app will send these emails:

### 1. Password Reset Email
```
Subject: Password Reset Request - NoteKeeper

Hello,

You requested to reset your password for NoteKeeper.

Click the link below to reset your password:
http://localhost:3000/reset-password/{TOKEN}

This link will expire in 1 hour.

If you didn't request this, please ignore this email.

Best regards,
NoteKeeper Team
```

### 2. Two-Factor Authentication Email
```
Subject: Your 2FA Code - NoteKeeper

Hello,

Your two-factor authentication code is:

{6-DIGIT-CODE}

This code will expire in 5 minutes.

Best regards,
NoteKeeper Team
```

### 3. Welcome Email (Bonus)
```
Subject: Welcome to NoteKeeper!

Hello {FirstName},

Welcome to NoteKeeper!

Your account has been successfully created.

Start organizing your notes today!

Best regards,
NoteKeeper Team
```

---

## ⏱️ Timeline

- **2-Step Verification:** 2 minutes
- **App Password:** 2 minutes  
- **Update Config:** 1 minute
- **Restart & Test:** 2 minutes

**Total:** ~7 minutes to fully working email system

---

**Questions? Everything is ready to go - just follow the steps above!** 🎉
