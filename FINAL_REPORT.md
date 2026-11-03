# StudyMate final project report

## Purpose

StudyMate helps a student plan academic work in one place. It addresses the practical problem of keeping assignment dates, class times, subjects, and assessment results separate by bringing them into a single Android app. The dashboard summarizes upcoming deadlines and today's timetable, while the progress screen calculates subject averages.

## Design considerations

The project uses a small native Android interface written in Kotlin. The main navigation separates the dashboard, tasks, timetable, progress, and settings. Form fields are validated by the repository before data is stored, and invalid values return readable errors. The app has a blue and green visual palette, a custom calendar/check launcher icon, and English, Afrikaans, and isiZulu labels for common navigation and form actions.

The app uses an on-device key-value NoSQL store. Each signed-in local user has separate keys, and collection values are JSON documents. This makes the principal CRUD flows available offline and preserves them between launches. When a Firebase project is supplied, a Firestore adapter mirrors those collections to a user-scoped document; Firestore's local cache queues writes while offline and sends them when the connection returns. The Settings action reads the document after writing. The security rules limit the remote path to the authenticated anonymous Firebase UID. The local app password fallback stores a salted PBKDF2 hash. The data model is organized around users, subjects, assignments, timetable entries, and grades.

The dashboard calls the ZenQuotes daily REST endpoint using a background thread and applies connection/read timeouts. If the endpoint fails, it displays a local fallback quote. This means the API failure path does not prevent the dashboard from opening.

## Automated testing and GitHub Actions

`StudyRulesTest` covers email/password checks, required assignment fields and date format, ordering by due date and priority, and subject average calculation. `.github/workflows/android.yml` checks out the repository, installs JDK 17 and Android SDK 34, then runs `testDebugUnitTest` and `assembleDebug` on pushes and pull requests to `main` and `master`.

GitHub Actions results are available in the repository's Actions tab after the workflow has run on GitHub. The workflow file is present in the source. A successful remote run and push cannot be evidenced until the final project revision is pushed.

## Use of GitHub

Git provides the project history. GitHub hosts the Kotlin source and CI workflow; Actions automates the unit-test and Android debug-build checks. Commits should describe one coherent change, and the final release commit should be tagged `final-PoE` after CI is green.

## Requirement status and evidence

| Assignment item | Current project evidence/status |
| --- | --- |
| Working Android app, phone demonstration | Android source and debug build target are present. Demonstrate on a physical phone and record the requested video. |
| NoSQL database, storing and reading data | Local key-value store with JSON documents; repository methods implement reads and writes for each collection. |
| REST API | ZenQuotes daily endpoint and local fallback are implemented. |
| User registration/login | Local demonstration authentication; PBKDF2 password hashing. |
| Settings | Reminder preference and language setting are persisted locally. |
| App icon | Vector launcher icon is included. |
| Automated tests and GitHub Actions | Unit tests and workflow source are included. Verify the run in GitHub Actions after pushing. |
| README, report, release notes | README, this report, and `RELEASE_NOTES.md` are included. Add the video link after recording. |
| GitHub commit, push, final tag | Repository has an `origin` remote. Push the reviewed final commit and tag `final-PoE` after CI succeeds. |
| Cloud sync / online database | Firestore sync adapter, offline write queue, and rules are included. Live cloud behavior still requires the owner's Firebase configuration and Anonymous Authentication setup. |
| Single sign-on | Not implemented. Requires an identity provider and OAuth credentials. Marked PoE-only in the brief. |
| Real-time push notifications | Not implemented. Requires FCM project configuration and a sending service. Marked PoE-only in the brief. |
| Publication evidence | No Play Console access, production signing key, signed release APK, or store screenshots are supplied. These must be produced by the account owner. |
| Demo video and voice-over | Must be recorded from the app on a physical phone and linked here. |
| AI use note | Included in README; adapt it to the actual tools used for the final submission. |

## Release preparation

The debug APK is for testing only. Before Play Store publication, configure the app's Firebase project if cloud features are required, create a protected production signing key, prepare store listing assets and privacy details, build a signed release APK/AAB, and capture genuine screenshots from the published or console-uploaded app. Do not commit signing secrets or Firebase service-account credentials.

## AI assistance disclosure

AI tools helped with code scaffolding and review, validation/test structure, and documentation. The project owner should check this statement against the actual tools and assistance used and update it before submission.
