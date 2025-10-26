# Rent Tracker

A comprehensive Android application for managing rental properties, tenants, leases, and payments.

## Features

### Phase 1 (Implemented)

- **Owner Management**
  - Add, edit, and delete property owners
  - Store owner contact information (name, email, phone)
  - Phone number validation with country code support

- **Building Management**
  - Manage multiple buildings per owner
  - Property type classification (Commercial, Residential, Mixed, Industrial)
  - Address and notes support

- **Tenant Management**
  - Active and checked-out tenant tracking
  - Dual phone number support
  - Family members information
  - Email and notes support
  - Validation to prevent checkout with active leases

- **Lease Management**
  - Active and completed lease tracking
  - Start date, end date, and rent increase date
  - Rent and security deposit tracking
  - Date validation (end date > start date, rent increase date validation)
  - Link leases to buildings and tenants

- **Payment Tracking**
  - Record payments for active leases
  - Multiple payment methods (UPI, Cash, Bank Transfer)
  - Payment status (Full/Partial) with visual indicators
  - Payment history in reverse chronological order
  - Auto-fetch rent amount from lease

- **Reports**
  - Active and checked-out tenant reports
  - Active and completed lease reports
  - Payment reports with summaries
  - Total rent and deposit calculations

- **Settings**
  - Currency selection (USD, EUR, GBP, INR, JPY, CNY, AUD, CAD)
  - App lock with biometric authentication support
  - About section with version and author information

## Technical Specifications

- **Language**: Kotlin 1.9.22
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room with SQLite
- **Dependency Injection**: Manual (ViewModelFactory)
- **Async Operations**: Kotlin Coroutines and Flow
- **Build System**: Gradle with Kotlin DSL

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/renttracker/app/
│   │   │   ├── data/
│   │   │   │   ├── dao/           # Database access objects
│   │   │   │   ├── database/      # Room database configuration
│   │   │   │   ├── model/         # Data models
│   │   │   │   ├── preferences/   # DataStore preferences
│   │   │   │   └── repository/    # Repository layer
│   │   │   ├── ui/
│   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   ├── navigation/    # Navigation setup
│   │   │   │   ├── screens/       # Screen composables
│   │   │   │   ├── theme/         # Material Design theme
│   │   │   │   └── viewmodel/     # ViewModels
│   │   │   ├── MainActivity.kt
│   │   │   ├── RentTrackerApplication.kt
│   │   │   └── ViewModelFactory.kt
│   │   └── res/                   # Resources (strings, themes, etc.)
│   └── test/                      # Unit tests
└── build.gradle.kts               # App-level Gradle configuration
```

## Dependencies

### Core
- AndroidX Core KTX
- AndroidX Lifecycle Runtime KTX
- AndroidX Activity Compose

### UI
- Jetpack Compose (BOM 2023.08.00)
- Material Design 3
- Material Icons Extended
- Navigation Compose

### Data
- Room Database (2.5.2)
- DataStore Preferences
- Kotlin Coroutines

### Testing
- JUnit 4
- Mockito
- Robolectric
- Room Testing
- Coroutines Test
- Espresso
- Compose UI Test

## Building the Project

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17 or newer
- Android SDK with API level 34

### Build Instructions

#### Option 1: Using Android Studio (Recommended)

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd RentTracker5
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - Select "Open" and navigate to the `RentTracker5` folder
   - Wait for Gradle sync to complete

3. **Build the project:**
   - Menu: `Build > Make Project` (or press `Ctrl+F9`)
   - Or: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

4. **Run the app:**
   - Click the "Run" button (▶️) or press `Shift+F10`
   - Select your device/emulator

#### Option 2: Using Command Line

1. **Build the project:**
   
   **Windows:**
   ```bash
   .\gradlew.bat clean build
   ```
   
   **Linux/Mac:**
   ```bash
   ./gradlew clean build
   ```

2. **Run tests:**
   
   **Windows:**
   ```bash
   .\gradlew.bat test
   ```
   
   **Linux/Mac:**
   ```bash
   ./gradlew test
   ```

3. **Install on device/emulator:**
   
   **Windows:**
   ```bash
   .\gradlew.bat installDebug
   ```
   
   **Linux/Mac:**
   ```bash
   ./gradlew installDebug
   ```

4. **Build APK:**
   
   **Windows:**
   ```bash
   .\gradlew.bat assembleDebug
   ```
   
   **Linux/Mac:**
   ```bash
   ./gradlew assembleDebug
   ```
   
   APK location: `app\build\outputs\apk\debug\app-debug.apk`

## Testing

The project includes comprehensive unit tests for:
- Database operations (DAOs)
- Business logic (ViewModels)
- Date validation in leases
- Tenant checkout validation

Run all tests:

**Windows:**
```bash
.\gradlew.bat test
```

**Linux/Mac:**
```bash
./gradlew test
```

Run specific test:

**Windows:**
```bash
.\gradlew.bat test --tests OwnerDaoTest
```

**Linux/Mac:**
```bash
./gradlew test --tests OwnerDaoTest
```

## License

MIT License

Copyright (c) 2024

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Author

Email: no28.iot@gmail.com

## Future Enhancements (Phase 2)

- Expense tracking
- Vendor management
- Document upload functionality
- Google Drive sync
- Data export/import functionality
