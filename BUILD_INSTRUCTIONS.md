# Build Instructions for Rent Tracker

## Prerequisites

1. **Android Studio** (Hedgehog or newer) - Recommended approach
2. **Java Development Kit (JDK) 17 or newer**
3. **Android SDK** with API level 33 installed

## Option 1: Build with Android Studio (Recommended)

This is the easiest and recommended method:

1. **Open the project in Android Studio**:
   - Launch Android Studio
   - Select "Open" from the welcome screen
   - Navigate to the `RentTracker5` folder and select it
   - Click "OK"

2. **Gradle Sync**:
   - Android Studio will automatically detect the Gradle configuration
   - It will download the Gradle wrapper and all dependencies
   - Wait for "Gradle sync finished" message in the status bar

3. **Configure local.properties** (if needed):
   - Open `local.properties` file
   - Uncomment and set your SDK path:
     ```
     sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
     ```
   - Or let Android Studio auto-configure it

4. **Build the project**:
   - From menu: `Build > Make Project` (or press `Ctrl+F9`)
   - Or from menu: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

5. **Run the app**:
   - Connect an Android device or start an emulator
   - Click the "Run" button (green triangle) or press `Shift+F10`
   - Select your device and click "OK"

6. **Run tests**:
   - Right-click on the `test` folder in Project view
   - Select "Run 'Tests in 'app'"
   - Or use menu: `Run > Run...` and select test configuration

## Option 2: Build with Command Line

If you prefer command line (requires Gradle installed):

1. **Install Gradle wrapper** (if not already present):
   ```bash
   gradle wrapper --gradle-version 8.0
   ```

2. **Configure SDK path**:
   - Edit `local.properties`
   - Set your Android SDK location:
     ```
     sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
     ```

3. **Build the project**:
   ```bash
   # Windows
   .\gradlew.bat assembleDebug
   
   # Linux/Mac
   ./gradlew assembleDebug
   ```

4. **Run tests**:
   ```bash
   # Windows
   .\gradlew.bat test
   
   # Linux/Mac
   ./gradlew test
   ```

5. **Install on device**:
   ```bash
   # Windows
   .\gradlew.bat installDebug
   
   # Linux/Mac
   ./gradlew installDebug
   ```

## Troubleshooting

### Missing Gradle Wrapper

If you see "Could not find or load main class org.gradle.wrapper.GradleWrapperMain":

**Solution A** (Recommended): Open in Android Studio - it will auto-generate the wrapper

**Solution B**: Generate wrapper manually:
```bash
gradle wrapper --gradle-version 8.0
```

### SDK Not Found

If you see "SDK location not found":

1. Create/edit `local.properties` file in project root
2. Add your SDK path:
   ```
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ```

### Compilation Errors

If you encounter compilation errors:

1. Ensure you have JDK 17 or newer installed
2. Verify Android SDK is properly installed with API level 33
3. Run "Invalidate Caches / Restart" in Android Studio
4. Clean and rebuild: `Build > Clean Project` then `Build > Rebuild Project`

## Build Outputs

After successful build, you'll find:

- **APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Test Results**: `app/build/reports/tests/testDebugUnitTest/index.html`

## System Requirements

- **OS**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: At least 8GB free space
- **JDK**: Version 17 or newer
- **Android SDK**: API level 21-33 installed

## Next Steps

Once the app is built successfully:

1. **Run on emulator** to test all features
2. **Run unit tests** to verify functionality
3. **Review test coverage** in build reports
4. **Generate signed APK** for release (if needed)

## Support

For issues or questions:
- Email: no28.iot@gmail.com
- Check `README.md` for feature documentation
- Review `CHANGELOG.md` for version history
