# Configure Firebase cloud sync

StudyMate uses Firebase Anonymous Authentication to protect each installation's Firestore profile. The app's local email/password demo account remains on-device; it is not a Firebase account, and this design does not sync a local account across devices.

## Firebase Console setup

1. Create a Firebase project in the Firebase Console.
2. Add an Android app with package name `com.st10457568.studymate`.
3. Download the app's `google-services.json` and place it at `app/google-services.json` in this checkout. Keep the file local; it is ignored by Git.
4. In **Authentication → Sign-in method**, enable **Anonymous** sign-in.
5. Create a **Cloud Firestore** database. Choose the database location carefully because Google Cloud does not allow changing it after creation.
6. In Firestore **Rules**, deploy the contents of [`firestore.rules`](../firestore.rules). Do not use test mode or allow unauthenticated access.
7. To enable assignment attachments too, create a Firebase Storage bucket and deploy [`storage.rules`](../storage.rules). File uploads are optional for cloud sync; the Firestore database alone is enough to sync planner data.

## Build and use

1. Sync the Gradle project in Android Studio after placing the JSON file.
2. Build and install the app. The Google Services plugin is applied only when `app/google-services.json` exists.
3. Register or log in to the local StudyMate account while online once. The app signs into Firebase anonymously and stores the planner profile under `users/{anonymousUid}/profiles/{localUserId}`.
4. Check that profile in Firestore. Add or change an assignment, subject, timetable entry, or mark and confirm the `updatedAt` value and data change.
5. To check offline writes, first sign in while online, then switch off connectivity and make a change. Firestore's Android cache queues the write; reconnect and confirm the change reaches Firestore. The Settings **Sync with Firebase** action can also retry a sync.

The Firebase anonymous UID and StudyMate local user ID are installation-specific. Clearing app data or installing on a second device creates a different profile; this setup provides cloud-backed sync for that installation, not account linking or cross-device restore. To support cross-device accounts, replace the local demo login with a shared Firebase Authentication provider and key profiles by the authenticated account UID.

Firebase config contains client identifiers, not an Admin SDK credential. Never place a Firebase Admin service-account JSON or private key in the Android app or this repository.
