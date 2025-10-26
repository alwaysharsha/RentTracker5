# Changelog

All notable changes to the Rent Tracker project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4] - 2024-10-25

### Fixed - UserInput.md Issues Resolution

#### Dashboard
- **Changed grid layout from 4 columns to 3 columns** for better icon visibility and text alignment
- Icons and labels are now clearly visible
- Improved spacing and alignment of dashboard cards

#### Date Fields
- **Made all date fields editable across all screens**
- Added `EditableDateField` component that supports:
  - Manual date entry in "dd MMM yyyy" format
  - Calendar button for quick date selection
  - Clear button to remove dates
  - Real-time date parsing and validation
- Updated TenantDetailScreen with editable date fields
- Updated AddPaymentScreen with editable date field

#### Lease Functionality Removal
- **Completely removed Lease screen and all associated functionality**
- Deleted `LeaseScreen.kt` and `LeaseDetailScreen.kt`
- Deleted `Lease.kt` model class
- Deleted `LeaseDao.kt` interface
- Deleted `LeaseViewModel.kt`
- Removed `LeaseDao` from database (version 2 → 3)
- Removed Lease entity from database entities list
- Removed Lease operations from `RentTrackerRepository`
- Updated `RentTrackerApplication` to remove `leaseDao` initialization
- Removed `LeaseViewModel` from `MainActivity` and `ViewModelFactory`
- Updated `RentTrackerNavigation` to remove Lease routes
- Removed Lease screens from bottom navigation
- Updated `ReportsScreen` to remove Active Leases and Checkout Leases reports
- Deleted `LeaseDaoTest.kt` and `LeaseViewModelTest.kt`
- Updated `TenantViewModel` to remove `hasActiveLease` check
- Simplified tenant checkout process

#### Owner Screen
- Email is now optional (not mandatory) - already implemented
- Phone validation enforced (7-15 digits) - already implemented
- Country code defaults based on selected currency - already implemented

#### Tenant Screen  
- Family members field is multi-line text box - already implemented
- Tenant editing functionality working - already implemented
- Rent increase date added to Tenant screen - already implemented
- Security deposit added to Tenant screen - already implemented
- Checkout date added to Tenant screen - already implemented
- Phone country code defaults based on currency - already implemented
- Checkout dialog message updated (removed lease reference)

#### Payment Screen
- Active tenant list displays correctly - already implemented
- Payment history grouped by Month-Year - already implemented
- Add payment button available for selected tenant - already implemented
- **Click on payment record now opens payment details screen for editing**
- Created `PaymentEditScreen` for viewing and editing existing payments
- Added delete payment functionality in edit screen
- Payment cards are now clickable with navigation to edit screen
- **NEW: Click on tenant name opens dedicated Payment History screen**
- Created `TenantPaymentHistoryScreen` with:
  - Tenant information card
  - Payment summary (total payments count and amount)
  - Payment history grouped by Month-Year
  - Add payment button
  - Click payment to edit functionality
- Simplified main `PaymentScreen` to show tenant list with payment summary
- Each tenant card shows payment count and total amount paid
- Removed unused `onNavigateToDetail` parameter

#### Settings
- App Lock functionality available - implementation unchanged

#### Code Cleanup
- Removed all unused imports and variables
- Fixed compilation warnings
- Updated all affected ViewModels and repositories
- Updated database version to 3 with schema changes
- All tests passing successfully

### Payment Enhancements (Latest Updates)
- **Enhanced Payment Summary Widget** showing:
  - Total payments count and amount
  - Partial payments count and amount (now ALWAYS visible)
  - Total partial payments amount (renamed from "Partial Amount")
  - Total pending amount (highlighted in error color)
- **Always Show Partial Payment Stats**:
  - Partial payments section now always visible in payment summary
  - Shows 0 when no partial payments exist
  - Color-coded: tertiary color when > 0, muted when 0
- **Added Pending Amount Field** to track remaining balance for partial payments
  - Visible only when Payment Type is set to PARTIAL
  - Validation for numeric input
  - Stored in Payment model as `pendingAmount`
- **Notes Field is Multi-line** (already implemented)
  - 5 lines max in both Add and Edit payment screens
  - Better for detailed payment notes
- **Payment Cards** now show payment type status
- **Fixed Currency Display**:
  - Updated `formatCurrency()` function to use proper currency symbols
  - Supports USD, CAD, AUD ($), GBP (£), EUR (€), INR (₹), JPY, CNY (¥)
  - Number formatting with thousand separators (#,##0.00)
  - All payment amounts now sync with selected currency
  - Dashboard Total Payment widget syncs with currency
- **Fixed Payment Screen Flickering**:
  - Optimized TenantPaymentCard with `remember` for efficient calculations
  - Added initial empty list to prevent flickering during load
  - Introduced PaymentStats data class for better state management
  - Added stable keys to LazyColumn items (tenant.id) to prevent recomposition
  - Smooth rendering without visual glitches
- **Improved Pending Amount Field Discoverability**:
  - Added supporting text hint: "Select PARTIAL to track pending amount"
  - Appears below Payment Type dropdown when FULL is selected
  - Makes conditional field visibility clear to users
  - Applied to both Add and Edit payment screens
- **Enhanced Payment Screen Layout**:
  - Moved rent amount to right side for better visibility
  - Rent displayed prominently in large, bold text
  - Left side: Tenant name and phone number
  - Right side: Rent amount (top), payment count and total (bottom)
  - Cleaner visual hierarchy and information architecture

### Database
- **Database version upgraded from 3 to 4**
- Added `pendingAmount` field to Payment entity
- Removed Lease entity completely (in version 3)
- Maintained data integrity with tenant-based payment tracking

### Testing
- All unit tests passing
- Removed Lease-related tests
- Updated TenantViewModel tests
- Added PaymentViewModel tests

## [1.0.3] - 2024-10-13

### Changed - Major Architectural Updates

#### Tenant Management
- **Added lease-related fields directly to Tenant entity:**
  - Building selection (foreign key to Building)
  - Start Date
  - Rent Increase Date  
  - Rent amount
  - Security Deposit amount
  - Checkout Date
- Enhanced Tenant Detail screen with all new fields
- Date fields with calendar picker and clear functionality
- Building dropdown selection for tenant assignment
- Numeric input validation for rent and security deposit
- **Phone country code now defaults based on selected currency** (e.g., INR→91, USD→1, GBP→44)

#### Lease Management
- **Removed Lease screen and entity** - Lease functionality now integrated into Tenant management
- Simplified data model - tenants now directly track rental information
- Removed Lease routes from navigation

#### Payment Management
- Updated Payment entity to reference Tenant instead of Lease
- **Payment screen now lists active tenants instead of leases**
- Payments grouped by Month-Year for better organization
- Simplified payment creation flow - directly associated with tenants
- Auto-fetch rent amount from tenant's rental information

#### Dashboard
- Updated dashboard to show "Active Tenants" instead of "Active Leases"
- Removed Lease navigation option
- Streamlined dashboard with 6 main modules
- **Changed grid layout from 3 to 4 icons per row** for better space utilization

#### Database
- Updated database schema from version 1 to 2
- Added migration with fallbackToDestructiveMigration
- Payment.tenantId replaces Payment.leaseId
- Tenant entity expanded with rental fields

#### Navigation
- Removed bottom navigation bar from main UI
- Removed Lease and LeaseDetail routes
- Updated all navigation references from leases to tenants

### Technical Changes
- Updated `TenantDao`, `PaymentDao`, and `TenantViewModel`
- Modified `RentTrackerRepository` methods
- Added `formatSimpleDate` utility function to CommonComponents
- Updated all affected screens and view models
- **Added numeric-only validation for all phone number fields** (country code and mobile numbers)

## [1.0.2] - 2024-10-13

### Changed
#### Navigation
- Removed bottom navigation bar from main UI
- Users will need to use screen-specific navigation buttons or back navigation

## [1.0.1] - 2024-10-11

### Fixed - Issues from User Testing

#### Dashboard Screen (New)
- Added new Dashboard/Home screen with quick access to all modules
- Display active leases count widget
- Display total payments amount widget with currency symbol
- Grid layout with icons for Owner, Building, Tenant, Lease, Payment, Reports, Settings
- Set as default start screen in navigation

#### Owner Screen
- Made email field optional (no longer mandatory)
- Added phone number validation (7-15 digits, numbers only)
- Default country code now selected based on currency preference
- Added data loading for editing existing owners
- Fixed nullable email display in owner list

#### Tenant Screen  
- Changed family members field to multi-line text box (maxLines: 5)
- Implemented edit functionality - can now load and edit existing tenants
- Added data loading via Flow for real-time updates

#### Lease Screen
- Auto-set start date to today when tenant is selected
- Added clear (X) buttons to remove dates (allows blank end date and rent increase date)
- Rent and Security Deposit fields now enforce numeric-only input with decimal support
- Keyboard type set to Decimal for numeric fields
- Regex validation for numeric input (allows digits and optional decimal point)
- Implemented edit functionality - can now load and edit existing leases
- Added data loading via Flow for real-time updates

#### Database Layer
- Added Flow-based query methods to DAOs: `getOwnerByIdFlow`, `getTenantByIdFlow`, `getLeaseByIdFlow`
- Updated Repository to expose Flow methods for real-time data observation
- Updated ViewModels to use Flow for data loading in edit screens

#### Navigation
- Updated bottom navigation bar to include Dashboard as first item
- Added Dashboard route to navigation graph
- Changed startDestination to Dashboard screen

## [1.0.0] - 2024-10-11

### Build Configuration Updates
- Updated Android Gradle Plugin from 8.1.4 to 8.3.0 for JDK 21 compatibility
- Updated compileSdk from 33 to 34
- Updated targetSdk from 33 to 34
- Fixed launcher icon resource references (mipmap to drawable)
- Added JDK path configuration in gradle.properties
- Gradle wrapper version: 8.9

### Added - Phase 1 Initial Release

#### Owner Management
- Create, read, update, and delete owner records
- Owner fields: name (required), email (required), phone (required with validation)
- Support for primary and secondary phone numbers
- Address field for owner location
- Phone number validation with country code support

#### Building Management
- Create, read, update, and delete building records
- Building fields: name (required), address, property type (required), notes
- Property type options: Commercial, Residential, Mixed, Industrial
- Link buildings to owners with foreign key relationship
- Cascade delete when owner is removed

#### Tenant Management
- Create, read, update, and delete tenant records
- Tenant fields: name (required), email, phone (required), secondary phone, family members, notes
- Checkout functionality for tenants
- Two-tab interface: Active Tenants and Checked Out Tenants
- Validation prevents checkout if tenant has active lease
- Family members stored in large text field

#### Lease Management
- Create, read, update, and delete lease records
- Lease fields: name (required), start date (required), end date, rent increase date, rent (required), security deposit (required), building (required), tenant (required), notes
- Two-tab interface: Active Leases and Completed Leases
- Active lease: no end date specified
- Completed lease: end date is specified
- Date validation:
  - End date must be greater than start date
  - Rent increase date must be greater than start date
  - Rent increase date must be less than end date
- Numeric format for rent and security deposit
- Link leases to buildings and tenants

#### Payment Tracking
- Create, read, update, and delete payment records
- Select active lease from dropdown
- Display payment history in reverse chronological order
- Partial payments highlighted in red
- Payment fields: date (required, auto-selects current date), amount (required), payment method, transaction details, payment type, notes
- Payment methods: UPI, Cash, Bank Transfer (Personal, HUF, Others)
- Payment types: Partial, Full
- Auto-fetch rent amount from active lease

#### Reports
- Tenant Reports:
  - Active Tenant list
  - Checked Out Tenant list
- Lease Reports:
  - Active Lease list with summary (total leases, total monthly rent, total deposits)
  - Completed Lease list with summary
- Payment Reports:
  - All payments list with summary (total payments, full/partial count, total amount)
- Report filtering by category

#### Settings
- Currency selection (USD, EUR, GBP, INR, JPY, CNY, AUD, CAD)
- Currency preference persisted using DataStore
- Selected currency used throughout app including reports
- App Lock toggle for biometric authentication
- About section displaying:
  - App version (1.0)
  - Build number (1)
  - Author email (no28.iot@gmail.com)
  - License (MIT)

#### Technical Implementation
- MVVM architecture pattern
- Jetpack Compose for UI with Material Design 3
- Room database for local data persistence
- Kotlin Coroutines and Flow for async operations
- DataStore for settings/preferences
- Navigation Component for screen navigation
- Bottom navigation bar for main screens
- Form validation with error messages
- Mandatory fields marked with asterisk (*)

#### Testing
- Unit tests for all DAO operations (Owner, Building, Tenant, Lease, Payment)
- ViewModel tests for business logic
- Date validation tests for leases
- Tenant checkout validation tests
- Robolectric for Android component testing
- Mockito for mocking dependencies

#### Build Configuration
- Minimum SDK: 21 (Android 5.0)
- Target SDK: 34 (Android 14)
- Compile SDK: 34
- Kotlin version: 1.9.22
- Gradle version: 8.9
- Android Gradle Plugin: 8.3.0
- Java compatibility: 17 (JDK 21 tested and working)

### Dependencies Added
- AndroidX Core KTX 1.10.1
- AndroidX Lifecycle Runtime KTX 2.6.1
- Jetpack Compose BOM 2023.08.00
- Material Design 3
- Navigation Compose 2.7.0
- Room Database 2.5.2
- DataStore Preferences 1.0.0
- Biometric library 1.1.0
- JUnit 4.13.2
- Mockito 5.3.1
- Robolectric 4.10.3
- Coroutines Test 1.7.3

### Project Structure
- Organized package structure following Android best practices
- Separation of concerns: data, domain, and presentation layers
- Reusable UI components
- Centralized theme and styling
- Repository pattern for data access
- ViewModelFactory for dependency injection

### Known Issues
- None reported for initial release

### Notes
- All Phase 1 requirements have been implemented
- Phase 2 features (Expense Report, Vendors, Document Upload, Google Drive sync, Export/Import) are planned for future release
- UserInput.md maintained as reference only per requirements
