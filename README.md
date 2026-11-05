# StudyMate — final PoE project

StudyMate is a Kotlin Android student planner for assignment deadlines, weekly timetables, subjects, marks, and study progress. The final project builds on the Part 2 prototype and keeps core features usable without a network connection.

## Features

- Register, log in, and log out. The local demonstration account store hashes passwords with salted PBKDF2; it does not save plain-text passwords.
- Create, complete, and delete assignments; manage subjects and timetable entries; save marks and view subject averages.
- View upcoming work, today's timetable, weekly completion, and a daily quote from the ZenQuotes REST API. A bundled quote is shown when the service is offline.
- Keep user data in an on-device key-value NoSQL store. Collection values are JSON documents under per-user keys (`subjects`, `assignments`, `timetable`, and `grades`), so the CRUD flows continue to work offline and after restarting the app.
- Choose English, Afrikaans, or isiZulu for the main navigation, forms, and common actions.
- Change the assignment reminder preference in Settings.
- Read the privacy policy from Settings and delete the local account and its associated Firebase documents/files.
- Attach a file to an assignment and open it from the app using Firebase Storage when configured; uploads are limited to 10 MB by the supplied storage rules.
- Validate user input and show errors in the app rather than crashing.
- Use the StudyMate launcher icon in `app/src/main/res/drawable/ic_launcher_foreground.xml`.

The app can mirror the per-user JSON collections to Firestore when Firebase is configured. Firestore's Android offline cache queues writes while offline; the Settings sync action also performs a read after writing. To enable it, create a Firebase Android project for this application ID, add `app/google-services.json` (do not commit this file), enable Anonymous Authentication, create the Firestore database, and deploy [firestore.rules](firestore.rules). To enable blob uploads, create a Storage bucket and deploy [storage.rules](storage.rules). The Google Services Gradle plugin activates only when the config file exists. The local app account is still used for the demo; Firebase uses a per-install anonymous account to protect the remote document path.

Google SSO and FCM push messages are not implemented in this build. SSO needs OAuth credentials; FCM push delivery also needs a Firebase project and a trusted server/sender. These are marked PoE-only in the brief and require project-specific credentials and setup.

## Build and automated checks

Requirements: Android Studio with JDK 17 and Android SDK 36.1. The Play release targets API 36 and uses Android Gradle Plugin 8.13.0.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

GitHub Actions runs the unit tests and debug build for pushes and pull requests to `main` or `master` using `.github/workflows/android.yml`. A successful CI run requires the repository to be pushed to GitHub.

The sync path can be verified after Firebase setup by creating records while offline, reconnecting, and checking the matching document under `users/{anonymousUid}/profiles/{localUserId}` in Firestore. The blob path can be verified by attaching a file to an assignment and checking Storage under `users/{anonymousUid}/{localUserId}/assignments/`. The demo login is local and the Firebase anonymous account is device-specific; this implementation does not provide cross-device account linking.

The generated debug APK is at `app/build/outputs/apk/debug/app-debug.apk`. The signed Play App Bundle is at `app/build/outputs/bundle/release/app-release.aab`. The AAB's signature was verified; the local release lint task was skipped after it stalled, and final GitHub CI passed unit tests and the debug build. The upload keystore and `signing.properties` are local ignored files; back them up securely and never commit them. The privacy policy is in `docs/privacy-policy.html`; enable GitHub Pages for this repository before using its public URL.

## Demo video outline

Record on a physical Android phone. Show registration, logout/login, adding and completing an assignment, adding a timetable entry, adding a mark, changing the language and reminder preference, then open the dashboard and demonstrate the quote/API fallback. Narrate the local NoSQL storage, offline behavior, REST API, and CI workflow accurately. Do not claim that cloud sync, Google SSO, or FCM push is implemented in this build.

Add the unlisted video URL to this README after recording. Keep the video voice-over enabled and show the app running on the phone.

## GitHub and release process

The Android source and workflow are in this repository. The final commit has been pushed to `main` and tagged `final-PoE` after CI passed. For a later release, update the version code, rebuild and sign the AAB with the same upload key, then upload it through Play Console.

See [FINAL_REPORT.md](FINAL_REPORT.md) for the design, implementation, CI, and current requirement status. See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the changes since the prototype.
See [docs/play-console-checklist.md](docs/play-console-checklist.md) for the upload steps and required account-specific items.

## AI use statement

AI assistance was used to scaffold and review parts of the Kotlin Android prototype, improve validation and documentation, and help prepare the automated test/build workflow. Generated code and text were reviewed and adapted for this project. No AI-generated image assets are included.
