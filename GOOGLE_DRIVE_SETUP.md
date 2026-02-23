# Google Drive Cloud Backup Setup Guide

## Overview
The Google Drive cloud backup feature requires OAuth 2.0 configuration to work properly. Without this setup, sign-in will fail even when you select a Google account.

## Setup Steps

### 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name: "RentTracker Backup"
4. Click "Create"

### 2. Enable Google Drive API

1. In your project, go to "APIs & Services" → "Library"
2. Search for "Google Drive API"
3. Click on it and click "Enable"

### 3. Configure OAuth Consent Screen

1. Go to "APIs & Services" → "OAuth consent screen"
2. Select "External" (unless you have a Google Workspace account)
3. Click "Create"
4. Fill in required fields:
   - App name: "RentTracker"
   - User support email: Your email
   - Developer contact: Your email
5. Click "Save and Continue"
6. On Scopes page, click "Add or Remove Scopes"
7. Add: `https://www.googleapis.com/auth/drive.file`
8. Click "Save and Continue"
9. Add test users (your Google account email)
10. Click "Save and Continue"

### 4. Create OAuth 2.0 Client ID

1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "OAuth client ID"
3. Select "Android" as application type
4. Enter name: "RentTracker Android"
5. Get your app's SHA-1 fingerprint:

#### Debug SHA-1 (for testing):
```bash
# Windows (PowerShell)
cd C:\Users\YourUsername\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android

# Look for SHA-1 certificate fingerprint
```

#### Release SHA-1 (for production):
```bash
# Use your release keystore
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

6. Enter SHA-1 fingerprint in the form
7. Enter package name: `com.renttracker.app`
8. Click "Create"

### 5. Download Configuration (Optional)

The OAuth client ID is automatically linked to your app via package name and SHA-1.
No additional configuration file is needed in the app.

## Testing

1. Build and install the app with the same keystore used for SHA-1
2. Go to Settings → Google Drive Cloud Backup
3. Click "Sign in to Google Drive"
4. Select your Google account
5. Grant permissions when prompted
6. Sign-in should now succeed

## Troubleshooting

### Sign-in fails with "Sign-in cancelled or failed"

**Cause**: OAuth client ID not configured or SHA-1 mismatch

**Solutions**:
1. Verify OAuth client ID is created in Google Cloud Console
2. Ensure SHA-1 fingerprint matches your keystore
3. Ensure package name is exactly `com.renttracker.app`
4. Wait 5-10 minutes after creating OAuth client (propagation delay)

### Sign-in works but backup fails

**Cause**: Google Drive API not enabled

**Solution**: Enable Google Drive API in Google Cloud Console

### "API key not valid" error

**Cause**: Wrong API restrictions

**Solution**: OAuth client ID should have no restrictions for Android apps

## Alternative: Local Backup Only

If you don't want to set up Google Cloud:
1. The app still has local Export/Import backup in Settings
2. Export creates a ZIP file you can manually save to cloud storage
3. Import restores from the ZIP file

## Security Notes

- OAuth 2.0 is secure and industry-standard
- The app only requests `drive.file` scope (access to files it creates)
- Your Google account password is never shared with the app
- You can revoke access anytime in Google Account settings

## Support

If you encounter issues:
1. Check Logcat for detailed error messages
2. Verify all setup steps are completed
3. Ensure you're using the correct keystore for SHA-1
4. Try signing out and signing in again
