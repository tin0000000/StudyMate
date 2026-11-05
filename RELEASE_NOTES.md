# StudyMate — final PoE release notes

## Changes since the Part 2 prototype

- Added persisted app-language selection with English, Afrikaans, and isiZulu translations for core navigation, common actions, and form labels.
- Added an optional Firestore document sync path with per-user rules and offline queued writes, activated when the Firebase app configuration is supplied.
- Added assignment file attachments backed by Firebase Storage with UID-scoped access rules and a 10 MB size limit.
- Updated the Android build to target API 36, added system-bar insets for edge-to-edge layouts, and added a privacy policy entry point and account deletion flow.
- Added a GitHub Pages workflow for hosting the public privacy policy.
- Settings now explains that cloud sync and attachments are unavailable when Firebase is not configured, with Afrikaans and isiZulu translations.
- Retained offline-first assignment, timetable, subject, and mark management in a per-user JSON document store.
- Added documentation for the data model, REST API behavior, test coverage, and GitHub Actions workflow.
- Added a final project report and a physical-device demo outline to the README.
- Kept the app icon, input validation, local quote fallback, password hashing, and automated unit tests from the prototype.

## Known gaps

- Live Firestore sync requires the project owner's `google-services.json`, Firestore database, and Anonymous Authentication setup. The demo account is local; cloud profiles are device-specific.
- Google SSO and FCM push notifications need provider credentials and server setup and are not included.
- The signed release AAB is available at `app/build/outputs/bundle/release/app-release.aab`; its signing verifies. The local release lint task was skipped after it stalled, while final GitHub CI passed the unit tests and debug build.
- A physical-device demonstration video, real app screenshots, GitHub Pages activation, Play Console declarations, and store publication/review remain outstanding because they require the project owner's device or account.
