# Play Console submission checklist

## Project preparation

- [x] Package ID: `com.st10457568.studymate`
- [x] Target Android 16 (API 36), the current Google Play requirement.
- [x] Release signing configuration reads the ignored `signing.properties`; the upload keystore is ignored by Git.
- [x] Privacy policy page and external account-deletion page are in `docs/`.
- [x] In-app privacy policy entry and account deletion action are implemented.
- [x] Play listing draft and store icon/feature graphic are in this project.
- [x] Signed Play App Bundle generated at `app/build/outputs/bundle/release/app-release.aab` (local release lint task skipped after it stalled; CI unit test and debug build passed).
- [ ] Build and install the signed release on a physical phone; capture real screenshots of the final app.
- [ ] Enable GitHub Pages under repository Settings → Pages → Build and deployment → GitHub Actions. Wait for the Pages workflow, then verify `https://tin0000000.github.io/StudyMate/privacy-policy.html` and `/delete-account.html` load publicly.
- [ ] Add the Firebase project's `app/google-services.json`, enable Anonymous Authentication, create Firestore, deploy `firestore.rules`, and rebuild the release if cloud sync is part of the submitted version. See [`firebase-setup.md`](firebase-setup.md).
- [ ] If assignment file uploads are included, create a Storage bucket, deploy `storage.rules`, and rebuild. Storage is optional for Firestore cloud sync.

## Play Console setup

1. Sign in to Play Console and create the app as a free app with worldwide distribution. Use the support email already configured in the developer account.
2. Enter the title and descriptions from `play-store-listing.md`; add the privacy policy and account-deletion URLs above.
3. Upload the signed Android App Bundle from `app/build/outputs/bundle/release/app-release.aab` to Internal testing first.
4. Complete the app content forms: privacy policy, Data safety, app access, ads, target audience, content rating, and any other declaration Play Console requests. Match the Data safety answers to the exact submitted bundle and its SDK behavior.
5. Test the uploaded release, fix any issues, then use the required closed/open testing tracks for the account.
6. Submit the production rollout only after the account and release checks are satisfied.

Personal developer accounts created after 13 November 2023 must complete a closed test with at least 12 testers opted in continuously for 14 days before applying for production access. Other account types may have different requirements; follow the dashboard for this account.

## Data safety notes for this build

- StudyMate account details and planner records are stored on-device in this build. The local password is stored as a salted PBKDF2 hash.
- No Firebase configuration is present, so Firebase Auth, Firestore, and Storage are not initialized in the Play bundle. If Firebase is later enabled, update the privacy policy and Data safety responses to disclose the Firebase identifiers, synced planner records, and attached files.
- The daily quote request goes to ZenQuotes. Review the service's handling of request metadata and answer Play's Data safety prompts based on the actual integration.
- StudyMate contains no ads or analytics SDKs.
