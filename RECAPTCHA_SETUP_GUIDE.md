# 🔐 reCAPTCHA Activation Guide - NutriLife

## 📋 Overview

This guide explains how to activate **Google reCAPTCHA v2** in your NutriLife application to protect registration and login forms against bots.

---

## 🎯 Step 1: Get reCAPTCHA Keys

### 1.1 Go to Google reCAPTCHA Site

🔗 **URL**: https://www.google.com/recaptcha/admin

### 1.2 Sign in with a Google Account

- Use your personal or professional Google account
- If you don't have an account, create one

### 1.3 Create a New Site

Click the **"+"** or **"Register a new site"** button

### 1.4 Fill out the Form

```
┌─────────────────────────────────────────────────────────────┐
│  Label                                                     │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ NutriLife Application                                 │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  reCAPTCHA Type                                            │
│  ○ reCAPTCHA v3                                           │
│  ● reCAPTCHA v2                                           │
│    ● "I'm not a robot" checkbox                          │
│    ○ Invisible badge                                      │
│                                                             │
│  Domains                                                   │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ localhost                                             │ │
│  │ 127.0.0.1                                             │ │
│  │ nutrilife.com (your production domain)               │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  Owners                                                    │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ your-email@gmail.com                                  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ☑ Accept reCAPTCHA Terms of Service                      │
│                                                             │
│  [Submit]                                                  │
└─────────────────────────────────────────────────────────────┘
```

### 1.5 Get the Keys

After validation, you will get:

```
┌─────────────────────────────────────────────────────────────┐
│  🔑 Site Key                                               │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX │ │
│  └───────────────────────────────────────────────────────┘ │
│  [Copy]                                                    │
│                                                             │
│  🔐 Secret Key                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 6LcYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY │ │
│  └───────────────────────────────────────────────────────┘ │
│  [Copy]                                                    │
└─────────────────────────────────────────────────────────────┘
```

**⚠️ IMPORTANT**: 
- The **Site Key** is public (used in HTML/JavaScript)
- The **Secret Key** is private (used in Java backend)

---

## 🎯 Step 2: Configure Keys in the Application

### 2.1 Create a Configuration File

Create the file: `src/main/resources/recaptcha.properties`

```properties
# reCAPTCHA Configuration
# Replace these values with your real keys

# Site key (public) - used in frontend
recaptcha.site.key=6LcXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

# Secret key (private) - used in backend
recaptcha.secret.key=6LcYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

# Google verification URL
recaptcha.verify.url=https://www.google.com/recaptcha/api/siteverify
```

### 2.2 Update RecaptchaService.java

Open: `src/main/java/tn/esprit/projet/services/RecaptchaService.java`

Replace the content with:

```java
package tn.esprit.projet.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RecaptchaService {

    private static String SECRET_KEY;
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = RecaptchaService.class
                .getClassLoader()
                .getResourceAsStream("recaptcha.properties")) {
            
            if (input == null) {
                System.err.println("[reCAPTCHA] Config file not found, using test key");
                SECRET_KEY = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"; // Test key
                return;
            }

            Properties prop = new Properties();
            prop.load(input);
            SECRET_KEY = prop.getProperty("recaptcha.secret.key");
            
            System.out.println("[reCAPTCHA] Configuration loaded successfully");
            
        } catch (IOException e) {
            System.err.println("[reCAPTCHA] Error loading config: " + e.getMessage());
            SECRET_KEY = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"; // Fallback
        }
    }

    public boolean verify(String token) {
        if (token == null || token.isBlank()) return false;

        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "secret=" + URLEncoder.encode(SECRET_KEY, StandardCharsets.UTF_8)
                        + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            String json = response.toString();
            System.out.println("[reCAPTCHA] Response: " + json);

            return json.contains("\"success\": true") || json.contains("\"success\":true");

        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Verification error: " + e.getMessage());
            return false; // Fail closed in production
        }
    }

    public boolean hasToken(String token) {
        return token != null && !token.isBlank();
    }
}
```

---

## 🎯 Step 3: Integrate reCAPTCHA in Frontend

### 3.1 Add Google Script in HTML

If you use FXML files with WebView, add:

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>reCAPTCHA</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>
    <div class="g-recaptcha" 
         data-sitekey="YOUR_SITE_KEY_HERE"
         data-callback="onRecaptchaSuccess">
    </div>
    
    <script>
        function onRecaptchaSuccess(token) {
            // Send token to Java backend
            window.recaptchaToken = token;
            console.log('reCAPTCHA validated:', token);
        }
    </script>
</body>
</html>
```

### 3.2 Create HTML File for reCAPTCHA

Create: `src/main/resources/html/recaptcha.html`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>reCAPTCHA Verification</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 200px;
            margin: 0;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            text-align: center;
        }
        h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 20px;
        }
        .status {
            margin-top: 15px;
            padding: 10px;
            border-radius: 8px;
            font-size: 14px;
            display: none;
        }
        .status.success {
            background: #d1fae5;
            color: #065f46;
            display: block;
        }
        .status.error {
            background: #fee2e2;
            color: #991b1b;
            display: block;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>🔐 Security Verification</h2>
        <p style="color: #666; font-size: 14px; margin-bottom: 20px;">
            Prove you're not a robot
        </p>
        
        <div class="g-recaptcha" 
             data-sitekey="REPLACE_WITH_YOUR_SITE_KEY"
             data-callback="onRecaptchaSuccess"
             data-expired-callback="onRecaptchaExpired">
        </div>
        
        <div id="status" class="status"></div>
    </div>
    
    <script>
        function onRecaptchaSuccess(token) {
            const status = document.getElementById('status');
            status.className = 'status success';
            status.textContent = '✓ Verification successful!';
            
            // Send token to Java backend via JavaFX
            if (window.javaConnector) {
                window.javaConnector.setRecaptchaToken(token);
            }
            
            console.log('reCAPTCHA Token:', token);
        }
        
        function onRecaptchaExpired() {
            const status = document.getElementById('status');
            status.className = 'status error';
            status.textContent = '⚠ Verification expired. Please try again.';
        }
    </script>
</body>
</html>
```

**⚠️ Don't forget**: Replace `REPLACE_WITH_YOUR_SITE_KEY` with your real site key!

---

## 🎯 Step 4: Test reCAPTCHA

### 4.1 Google Test Keys

For development, you can use Google test keys:

```
Site Key (public):
6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI

Secret Key (private):
6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe
```

**These test keys**:
- ✅ Work on localhost
- ✅ Always pass validation
- ✅ Display reCAPTCHA widget
- ❌ Should NOT be used in production

### 4.2 Test Integration

1. **Launch the application**
   ```bash
   mvn javafx:run
   ```

2. **Go to registration page**

3. **Check that reCAPTCHA widget displays**

4. **Check the "I'm not a robot" box**

5. **Submit the form**

6. **Check in logs**:
   ```
   [reCAPTCHA] Response: {"success": true, ...}
   ```

---

## 🎯 Step 5: Production Deployment

### 5.1 Get Real Keys

1. Go back to https://www.google.com/recaptcha/admin
2. Create a new site with your **real domain**
3. Get the new keys

### 5.2 Update Configuration

In `recaptcha.properties`:

```properties
# PRODUCTION - Real keys
recaptcha.site.key=YOUR_REAL_SITE_KEY
recaptcha.secret.key=YOUR_REAL_SECRET_KEY
```

### 5.3 Update HTML

In `recaptcha.html`, replace:

```html
data-sitekey="YOUR_REAL_SITE_KEY"
```

### 5.4 Security

⚠️ **IMPORTANT**:
- ✅ NEVER commit real keys to Git
- ✅ Add `recaptcha.properties` to `.gitignore`
- ✅ Use environment variables in production
- ✅ Change `return false` in catch (fail closed)

---

## 📊 Verification and Monitoring

### reCAPTCHA Dashboard

Access: https://www.google.com/recaptcha/admin

You can see:
- 📈 Number of requests
- ✅ Success rate
- 🤖 Bot detection
- 📊 Statistics by domain

---

## 🐛 Troubleshooting

### Problem 1: Widget doesn't display

**Solution**:
- Check that Google script is loaded
- Check site key in HTML
- Check JavaScript console for errors

### Problem 2: Validation always fails

**Solution**:
- Check secret key in `recaptcha.properties`
- Check that domain is authorized
- Check logs: `[reCAPTCHA] Response: ...`

### Problem 3: "Invalid site key" error

**Solution**:
- Site key doesn't match domain
- Add `localhost` to authorized domains

### Problem 4: Network error

**Solution**:
- Check your internet connection
- Check verification URL is correct
- Increase timeout in code

---

## 📝 Complete Checklist

### Configuration
- [ ] Google account created
- [ ] reCAPTCHA site registered
- [ ] Keys retrieved
- [ ] `recaptcha.properties` file created
- [ ] Keys added to file
- [ ] `RecaptchaService.java` updated

### Frontend
- [ ] `recaptcha.html` file created
- [ ] Site key added to HTML
- [ ] Google script loaded
- [ ] JavaScript callback configured

### Tests
- [ ] Widget displays correctly
- [ ] Validation works
- [ ] Logs show success
- [ ] Form submits correctly

### Production
- [ ] Real keys obtained
- [ ] Configuration updated
- [ ] Keys secured (not in Git)
- [ ] Production tests performed

---

## 🎉 Final Result

After following this guide, you will have:

✅ **reCAPTCHA v2 activated** on your application  
✅ **Bot protection** on registration/login  
✅ **Secure configuration** with separate keys  
✅ **Monitoring** via Google dashboard  

---

## 📚 Resources

- 📖 [Official reCAPTCHA documentation](https://developers.google.com/recaptcha/docs/display)
- 🔧 [reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin)
- 💡 [reCAPTCHA FAQ](https://developers.google.com/recaptcha/docs/faq)

---

**🔐 Your application is now protected against bots! 🎊**

---

**Created by**: Kiro AI Assistant  
**Date**: April 24, 2026  
**Version**: 1.0
