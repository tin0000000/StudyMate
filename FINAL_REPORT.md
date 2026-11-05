# StudyMate final project report

## Purpose

StudyMate helps a student plan academic work in one place. It addresses the practical problem of keeping assignment dates, class times, subjects, and assessment results separate by bringing them into a single Android app. The dashboard summarizes upcoming deadlines and today's timetable, while the progress screen calculates subject averages.

## Design considerations

The project uses a small native Android interface written in Kotlin. The main navigation separates the dashboard, tasks, timetable, progress, and settings. Form fields are validated by the repository before data is stored, and invalid values return readable errors. The app has a blue and green visual palette, a custom calendar/check launcher icon, and English, Afrikaans, and isiZulu labels for common navigation and form actions.

The app uses an on-device key-value NoSQL store. Each signed-in local user has separate keys, and collection values are JSON documents. This makes the principal CRUD flows available offline and preserves them between launches. When a Firebase project is supplied, a Firestore adapter mirrors those collections to a user-scoped document; Firestore's local cache queues writes while offline and sends them when the connection returns. The Settings action reads the document after writing. Assignment documents can include a Firebase Storage attachment URL; the app's document picker uploads the selected file to a UID-scoped path. Supplied Firestore and Storage rules limit access to the authenticated anonymous Firebase UID, and uploads are limited to 10 MB. The local app password fallback stores a salted PBKDF2 hash. Users can delete their local account and, when online Firebase is configured, the matching cloud profile and stored files. The data model is organized around users, subjects, assignments, timetable entries, and grades.

The dashboard calls the ZenQuotes daily REST endpoint using a background thread and applies connection/read timeouts. If the endpoint fails, it displays a local fallback quote. This means the API failure path does not prevent the dashboard from opening.

## Automated testing and GitHub Actions

`StudyRulesTest` covers email/password checks, required assignment fields and date format, ordering by due date and priority, and subject average calculation. `.github/workflows/android.yml` checks out the repository, installs JDK 17 and Android SDK 36.1, then runs `testDebugUnitTest` and `assembleDebug` on pushes and pull requests to `main` and `master`. The final tagged commit passed this workflow.

The final Android CI run is available in the repository's Actions tab. It passed `testDebugUnitTest` and `assembleDebug` for the final app code.

## Use of GitHub

Git provides the project history. GitHub hosts the Kotlin source and CI workflow; Actions automates the unit-test and Android debug-build checks. The final source commit is pushed to `main` and tagged `final-PoE` after CI passed.

## Requirement status and evidence

| Assignment item | Current project evidence/status |
| --- | --- |
| Working Android app, phone demonstration | Android source and debug build target are present. Physical-phone screenshots of login, dashboard, tasks, timetable, progress, and settings are included in `docs/screenshots/`; record the requested narrated video. |
| NoSQL database, storing and reading data | Local key-value store with JSON documents; repository methods implement reads and writes for each collection. |
| Blob storage | Optional assignment file uploads use Firebase Storage when the owner's project and bucket are configured; `storage.rules` enforces owner scope and a 10 MB upload limit. |
| REST API | ZenQuotes daily endpoint and local fallback are implemented. |
| User registration/login | Local demonstration authentication; PBKDF2 password hashing. |
| Settings | Reminder preference and language setting are persisted locally. |
| App icon | Vector launcher icon is included. |
| Automated tests and GitHub Actions | Unit tests and workflow are included. The final commit's Android CI run passed. |
| README, report, release notes | README, this report, and `RELEASE_NOTES.md` are included. Add the video link after recording. |
| GitHub commit, push, final tag | Final source is pushed to `main` and tagged `final-PoE`; CI passed. |
| Cloud sync / online database | Firestore sync adapter, offline write queue, and rules are included. Live cloud behavior still requires the owner's Firebase configuration and Anonymous Authentication setup. |
| Single sign-on | Not implemented. Requires an identity provider and OAuth credentials. Marked PoE-only in the brief. |
| Real-time push notifications | Not implemented. Requires FCM project configuration and a sending service. Marked PoE-only in the brief. |
| Publication evidence | A signed release AAB has been generated and verified. Physical-phone screenshots are included in `docs/screenshots/`. Play Console upload/review and the narrated video are still outstanding. |
| Android API target | Configured to target Android 16 (API 36) for current Play submissions. |
| Demo video and voice-over | Must be recorded from the app on a physical phone and linked here. |
| AI use note | Included in README; adapt it to the actual tools used for the final submission. |

## Release preparation

The signed Play App Bundle is at `app/build/outputs/bundle/release/app-release.aab`. It was generated with the local upload keystore and verified; the local Gradle release lint task was skipped after it stalled. The final GitHub CI run passed unit tests and the debug build. Phone screenshots are included in `docs/screenshots/`; before production, the owner must enable GitHub Pages, verify the public policy/deletion URLs, install and exercise the signed release on a physical phone, record and link the narrated demo, and complete Play Console declarations and review. Firebase-backed features require the owner's Firebase project configuration. Do not commit signing secrets, `google-services.json`, or Firebase service-account credentials.

## AI assistance disclosure

AI tools helped with code scaffolding and review, validation/test structure, and documentation. The project owner should check this statement against the actual tools and assistance used and update it before submission.
