# Configure Firebase cloud sync

StudyMate uses Firebase Anonymous Authentication to protect each installation's Firestore profile. The app's local email/password demo account remains on-device; it is not a Firebase account, and this design does not sync a local account across devices.

## Firebase Console setup

1. Sign in to the [Firebase Console](https://console.firebase.google.com/) with the Google account that should own the project and create a project named `StudyMate`. Google Analytics is optional for this app; skip it if you do not need analytics.
2. In Project Overview, choose **Add app → Android** and enter the exact package name `com.st10457568.studymate`.
3. Download `google-services.json` and put it at `app/google-services.json` in this checkout. Keep the filename exact; the file is ignored by Git and must not be committed.
4. In **Authentication → Get started → Sign-in method**, enable **Anonymous** sign-in and save.
5. In **Firestore Database**, create a database. Select a location you can keep long-term. Choose production mode, then open the **Rules** tab and publish the contents of [`firestore.rules`](../firestore.rules). Do not use test mode or allow unauthenticated access.
6. To enable assignment attachments, create a default Storage bucket and publish [`storage.rules`](../storage.rules). **Cloud Storage for Firebase currently requires the Blaze pay-as-you-go billing plan.** It can incur charges if usage exceeds no-cost allowances. Set a budget alert and skip Storage until you are ready to enable billing. Firestore sync does not require Storage.

The project already includes `firebase.json` linking the Firestore and Storage rule files. If you install the Firebase CLI, select the new project with `firebase use --add`; deploy rules with `firebase deploy --only firestore:rules`, and deploy Storage rules only after enabling Storage with `firebase deploy --only storage`.

## Build and use

1. Sync the Gradle project in Android Studio after placing the JSON file. The Google Services plugin and Firebase initialization activate automatically when the file exists.
2. Build and install the app. The Google Services plugin is applied only when `app/google-services.json` exists.
3. Register or log in to the local StudyMate account while online once. The app signs into Firebase anonymously and stores the planner profile under `users/{anonymousUid}/profiles/{localUserId}`.
4. Check that profile in Firestore. Add or change an assignment, subject, timetable entry, or mark and confirm the `updatedAt` value and data change.
5. To check offline writes, first sign in while online, then switch off connectivity and make a change. Firestore's Android cache queues the write; reconnect and confirm the change reaches Firestore. The Settings **Sync with Firebase** action can also retry a sync.

The Firebase anonymous UID and StudyMate local user ID are installation-specific. Clearing app data or installing on a second device creates a different profile; this setup provides cloud-backed sync for that installation, not account linking or cross-device restore. To support cross-device accounts, replace the local demo login with a shared Firebase Authentication provider and key profiles by the authenticated account UID.

Firebase config contains client identifiers, not an Admin SDK credential. Never place a Firebase Admin service-account JSON or private key in the Android app or this repository.
