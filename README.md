Happy Pet 🐾
Happy Pet is a modern mobile app designed to help pet owners manage their pets' care. From biometric authentication to weight tracking, it integrates secure and user-friendly features using cutting-edge Android development practices.

📱 Features
Biometric Authentication: Secure access with Android Biometric API.
User Registration & Login: Firebase-based email/password and Google Sign-In support.
Appointment Management: Schedule and track pet care appointments in real-time.
Pet Weight Tracking: Log weight with optional photos, view historical data.
Modern UI: Built with Jetpack Compose and Material Design 3 compliance.
🛠 Tools & Technologies
Android Studio, Jetpack Compose, Firebase, GitHub, Trello.
🚀 Development Process
Developed in 5 Agile sprints, focusing on feature implementation, UI/UX refinement, and bug fixes.

🌐 Highlights
Professional: Follows Android development best practices.
Secure: GDPR-compliant data handling and encrypted communication.
Social Impact: Empowers better pet care for informed pet owners.
📊 Future Enhancements
Advanced analytics, offline support, and improved accessibility.
*************************************************************************
Report: Technologies Powering the Pet Medication Appointment App

1. Biometrics
- Purpose: Provides secure and user-friendly authentication.
- Contribution: Enables users to log in using fingerprint or face recognition, ensuring that sensitive data, such as pet medication records, remains secure.
  Biometrics Integration
  The app relies on the Android Biometrics API to handle secure user authentication.
  During setup, the app registers with the device's biometric hardware to determine compatibility and availability.
  On login, biometric data is validated locally using cryptographic keys stored securely on the device, ensuring no sensitive data leaves the device.

2. Firebase Authentication
- Purpose: Manages user sign-ins across devices.
- Contribution: Simplifies the authentication process by integrating Google, email, or other authentication methods, ensuring a smooth and secure user experience.
  Firebase Authentication
  Firebase SDK is integrated to simplify user authentication processes.
  Authentication methods such as email/password, Google Sign-In, or phone number verification are configured via the Firebase Console.
  Once a user is authenticated, Firebase issues a token that can be used to validate secure backend requests and access other Firebase services like the database.

3. Jetpack Compose
- Purpose: Facilitates the building of modern, responsive UI with minimal effort.
- Contribution: Powers the app's dynamic and user-friendly interface, enabling quick navigation between features like appointment scheduling, reminders, and medication tracking.
  Jetpack Compose is used to build a declarative UI that dynamically adapts to user actions.
  Composable functions are designed for each screen (e.g., appointment details, reminders) and interact with ViewModels.
  The Compose framework handles state changes automatically, ensuring a responsive and smooth user interface.

4. Splashscreen Library
- Purpose: Enhances app startup experience.
- Contribution: Provides a professional first impression with a customizable splash screen that smoothly transitions into the app’s main content.
  The splash screen is implemented using Android's Splashscreen API.
  During app launch, the splash screen displays branding or loading indicators while critical app components (e.g., database connections, authentication) initialize in the background.
  Once initialization completes, the app transitions seamlessly to the home screen.

5. Edge-to-Edge
- Purpose: Utilizes the full screen space on modern devices.
- Contribution: Ensures that the app's UI extends to the device's edges, creating a more immersive user experience.
  Edge-to-edge design is achieved by configuring system window insets.
  The app adjusts its layout to accommodate notches and navigation bars while maximizing the usable screen area, enhancing visual appeal on modern devices.

Firebase Database
- Purpose: Stores and syncs data in real-time.
- Contribution: Keeps medication schedules, reminders, and pet details updated across devices, even when offline, syncing changes once internet connectivity is restored.
  Firebase Database is set up with structured data to store appointment details, medication schedules, and pet records.
  Realtime listeners are attached to database references to automatically sync changes to the app UI when data updates.
  Offline persistence is enabled, allowing the app to cache data locally and synchronize once internet connectivity is restored.

7. Calendar Integration
- Purpose: Manages scheduling and reminders.
- Contribution: Allows users to view and manage medication appointments through a clear, intuitive calendar interface, with reminders for upcoming tasks.
  The app interacts with the Android Calendar Provider to access and manage calendar events.
  Medication appointments are converted into calendar events, complete with titles, descriptions, and reminders.
  The app uses system permissions to read/write calendar data, ensuring compliance with user privacy.

8. Camera
- Purpose: Captures images and scans data.
- Contribution: Lets users upload pet images or scan prescriptions, enabling better record-keeping for pet health management.
  The app uses the CameraX API to provide a smooth user experience for capturing photos.
  The captured images are processed and either stored locally or uploaded to Firebase Storage for cloud backup and synchronization.
  Advanced use cases like scanning text (e.g., prescriptions) involve integrating Optical Character Recognition (OCR) libraries.

9. Internet
- Purpose: Facilitates data access and app functionality.
- Contribution: Enables features like Firebase syncing, updates, and integration with online services for enhanced functionality.
  Internet connectivity is managed using the Android Connectivity Manager.
  The app actively monitors network status to provide real-time feedback on synchronization capabilities, ensuring users are informed when offline.

10. Photo Gallery**
- Purpose**: Provides access to stored images.
- Contribution**: Allows users to upload pet photos from their gallery, personalizing their records and making the app more engaging.
  The app integrates with the Android MediaStore to allow users to select images from their gallery.
  Once selected, the app processes the image (e.g., compressing or resizing) before saving it locally or uploading it to Firebase.

11. Datastore
- Purpose: Manages lightweight and secure local data storage.
- Contribution: Saves user preferences, such as notification settings or app theme, ensuring a consistent experience even without internet access.
  Datastore
  Datastore uses Kotlin Coroutines to handle asynchronous data storage and retrieval.
  The app stores key-value pairs like user preferences and settings, ensuring a lightweight and fast data management solution.
  The data is stored securely, replacing older storage solutions like SharedPreferences, with built-in support for handling structured data schemas.

