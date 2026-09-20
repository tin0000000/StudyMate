# StudyMate

StudyMate is a Kotlin Android prototype for the OPSC Part 2 app submission. It is based on the supplied planning and research reports: a student planner that helps users manage assignments, timetable entries, study progress, and marks.

## Implemented Prototype Features

- Register, log in, and log out.
- Passwords in the demo fallback store are protected with salted PBKDF2 hashes.
- Dashboard with upcoming deadlines, today's timetable, weekly progress, and a daily motivational quote from ZenQuotes.
- Assignment CRUD fields: title, subject, description, due date, priority, completion status, and reminder setting.
- Subject management.
- Timetable entries with subject, day, start time, end time, and location.
- Mark capture and subject average calculation.
- Settings screen with reminder preference and account logout.
- Invalid inputs are handled with validation messages instead of crashes.
- Unit tests cover validation, date sorting, and average calculation.
- Firebase Auth and Firestore SDK dependencies are declared for the hosted production version.

## Data Stored

The local prototype stores data under the signed-in user. The structure mirrors the planned Firestore layout:

- `users/{userId}`: email, display name, salted password hash for the local fallback only.
- `users/{userId}/subjects/{subjectId}`: name and colour.
- `users/{userId}/assignments/{assignmentId}`: title, subject, description, due date, priority, completion status, and reminder flag.
- `users/{userId}/timetable/{entryId}`: subject, day, start time, end time, and location.
- `users/{userId}/grades/{gradeId}`: assessment name, mark, total, and date.

For a real Firebase submission, add the Firebase project configuration file from the Firebase console and replace `LocalStudyRepository` with a Firestore-backed repository that writes to the same paths.

## External API

The dashboard calls:

`https://zenquotes.io/api/today`

If the request fails, StudyMate shows a local fallback quote so the app remains usable offline or during API errors.

## Build and Test

Open the project in Android Studio, allow Gradle sync to download dependencies, then run:

```bash
gradle testDebugUnitTest assembleDebug
```

The GitHub Actions workflow in `.github/workflows/android.yml` runs the same test and debug build commands.

## Demo Video Checklist

Show these flows in the video:

- Register a user, log out, then log back in.
- Explain that the prototype fallback stores a salted password hash, while the production SDK dependencies are included for Firebase Auth and Firestore.
- Open Settings and change the reminder setting.
- Open Dashboard and point out the ZenQuotes REST API quote plus the fallback behaviour.
- Add an assignment, mark it complete, and delete it.
- Add a timetable item.
- Add a mark and show the calculated subject average.

## AI Tool Usage Write-Up

AI assistance was used to help scaffold the Kotlin Android prototype, structure the README, create validation and calculation unit tests, and align the implementation with the planning report. The generated code was reviewed and adjusted to use a compact native Android UI, local validation, password hashing, a REST API client, and a data structure that matches the planned Firebase/Firestore paths.
