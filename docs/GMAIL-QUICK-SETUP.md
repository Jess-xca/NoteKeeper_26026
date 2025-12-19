# ⚡ Gmail Setup - Quick Reference

**Time:** 5 minutes | **File:** `notekeeper/src/main/resources/application.properties`

---

## 🎯 3 Steps to Working Emails

### 1️⃣ Enable 2-Step Verification
- **URL:** https://myaccount.google.com/security
- **Action:** Turn on 2-Step Verification

### 2️⃣ Generate App Password  
- **URL:** https://myaccount.google.com/apppasswords
- **Action:** Select "Mail" → "Other" → Type "NoteKeeper" → Generate
- **Copy:** The 16-character password (e.g., `abcd efgh ijkl mnop`)

### 3️⃣ Update Config File
**Edit:** `notekeeper/src/main/resources/application.properties`

**Replace these 2 lines:**
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_16_CHARACTER_APP_PASSWORD
```

**With your actual values:**
```properties
spring.mail.username=your.email@gmail.com
spring.mail.password=abcdefghijklmnop
```

---

## ✅ Done! Restart Backend

```bash
cd notekeeper
mvn spring-boot:run
```

**Full guide:** See `GMAIL-SETUP-GUIDE.md` for details and troubleshooting.

---

**You're all set for password reset & 2FA!** 🎉
