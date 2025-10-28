# Rent Tracker

Rent Tracker andriod app.

## Softwares
- Gradle : C:\tools\Gradle\bin
- Android Studio : C:\Program Files\Android\Android Studio

## Features

### About
- License: MIT
- Author: no28.iot@gmail.com

### Version
Use gitversion to generate the version and build number.

Major Version: 1
Minor Version: 0

### Phase 1 

#### Fields

- Owner
    - Name (Mandatory)
    - Email (Mandatory)
    - Phone (Validate format, Country code, Mobile number)
        - Mobile (Mandatory)
        - Mobile2
    - Address

- Buildings
    - Name (Mandatory)
    - Owner (Mandatory)
    - Address
    - Property Type
        - Commercial
        - Residential
        - Mixed
        - Industrial
    - Notes

- Tenant
    - Name (Mandatory)
    - Email
    - Phone (Validate format, Country code, Mobile number)
        - Mobile (Mandatory)
        - Mobile2
    - Family members Name (Large Text box)
    - Checkout (Checkout box)
    - Notes
    - Rent Increase Date (Optional)
    - End Date (Optional)
    - Rent (Mandatory)
        - In number format
    - Security Deposit (Mandatory)
        - In number format

- Payment
    - Date (Mandatory, Auto select current date from calendar)
    - Amount (Mandatory)
        - Fetch the Rent from the active lease
    - Type of Payments
        - UPI
        - Cash
        - Bank Transfer
            - Personal
            - HUF
            - Others
        - Transaction Details
    - Payment Type
        - Partial
        - Full
    - Notes

- Reports
    - Tenant Reports
        - Active Tenant
        - Checkout Tenant
    - Lease Reports
        - Active Lease
        - Checkout Lease
    - Payment Reports
    - Tenant Payment Reports (including active and checkout payments)

- Settings
    - Currency selection
    - App Lock
    - About
        - App Version
        - App Build
        - Author
        - License

#### Basic Rules
- Mark with * are mandatory fields
- Tenant screen should show 2 Tabs for Active and Checkout Tenants.
    - Validate there is no active Lease for Tenant.
- Lease screen should show 2 Tabs for Active and Checkout Leases.
    - End date should be greater than Start date.
    - Rent Increase Date should be greater than Start date.
    - Rent Increase Date should be less than End date.
    - Active Lease where the End date is Empty
    - Checkout Lease where the End date is not Empty

### Payment screen
- List all Active Lease Tenants
- When Selected, Show the Payment history of the Tenant in reverse chronological order.
- Show the payment record in Red color if the payment is partial.
- Button (+) to add new payment

#### Settings Screen
- Currency selection should be saved in preferences. 
- Selected currency should be used throughout the app including reports.

### Issues
On Main screen show all Icons for Owner, Building, Tenant, etc as per the image.
Show widget to show the count of active leases and total payment

- Owner screen
-- [x] Email id should not be mandatory
-- [x]Phone should be validated
-- [x]Country code should be selected based on currency as default

- Tenant screen
-- [x] Family members should be multi line text box
-- [x] Unable to edit the tenant added
-- [x] Add Rent increase date into the Tenant screen
-- [x] Add Security deposit into the Tenant screen
-- [x] Add checkout date
-- [x] Phone country code should be selected based on currency as default

- [x] Remove Lease screen and its associated functionality
- [x] Date fields in all screens should be editable
- [x] Widget should be in sync with currency selected.
- [x] All phone numbers should be validated to numbers only.

- Payment Screen
-- [x] List the active Tenant name (instead of dropdown)
-- [x] Selected tenant name it should show the payment history based on Month-Year
-- [x] Add payment record should be in this page
-- [x] Double click on the payment should open the payment details screen
-- [x] Add multiple transaction entries to the payment record to make it more flexible to mark it as full payment or partial payment.

- Settings
-- [x] App Lock is not working

- Dashboard
-- [x] 3 Icons in a row

-- [x] With 3 icons the text is not visible, alignment needs to be adjusted.

-- [x] The labels of the icons should be visible clearly.
-- [x] The widget of Total payment should be in sync with currency selected.

- [x] Date fields should be editable.
- [x] Payments screen : When clicked on the Tenant name it should open the Payment record screen for that tenant and show the payment history of that tenant.

- [x] Payment record of the tenant the widget shows total payments, total amount, partial payments, total partial payments.



- [x] Partial Payments and Total Partial Payments are not showing in the widget.
- [x] The widget of Total payment should be in sync with currency selected.

- [x] Payment record the Notes field should be multiple line text box.
- [x] Payment record entry have a field to track pending partial payments to be paid to that the value is shown in the widget.

- [x] There is no partial amount field in the payment record entry.
- [x] In the Payment screen, along with Tenant name and phone number, show the current rent amount towards right.
- [x] In the dashboard screen the Total payments the default $ icon should be replaced with currency icon based on the currency selected.
- [x] The date field edit should pop up the calendar to select the date.
- [x] Change the payment method to be customized in settings, in payment record screen in a single dropdown.
- [x] In settings the payment methods order should be adjustable.


## Phase 2 (Don't start the development yet, until the phase 1 is completed)

### Fields

- Document Upload to capture the rental documents
- Export and Import

### Issues
- [x] Update version number in app\build.gradle.kts in versionName each time you fix an issue or release.
- [x] The export didn't save the file in downloads folder.
- [x] The share button is not working.
- [x] When we click on the payments screen the tenant entry will flicker to calculate the amounts, still exists.
- [ ] Documents icon is not visible in dashboard, also verify that is working as expected.


## Phase 3 (Don't start the development yet, until the phase 1 is completed)

### Fields

- Expense Report
- Vendors
- GoogleDrive integration to back up the data



## Status of Phase development
- [x] Phase 1
- [ ] Phase 2
- [ ] Phase 3

## Icons

Use appropriate icons for each screen.
