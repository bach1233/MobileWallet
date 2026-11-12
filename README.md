# MobiWallet Lab

Android Kotlin/XML project for a controlled Dynamic Instrumentation and Android security lab. It is a fictional wallet: every account, token, PIN, balance, transaction, certificate, and secret is fake. Do not use it against systems or devices you are not authorized to test.

## Components

- `app/`: Android app with `vulnerableDebug` and `hardenedDebug` flavors.
- `local-server/`: local-only HTTPS API and seed data.
- `functions/`: Firebase Callable backends; vulnerable and hardened transfer policies share the same Firestore database.
- `frida-scripts/`: one script per lab module.
- `docs/`: project-to-thesis map and lab workflow.
- `evidence/`: screenshot/evidence checklist.

## Accounts

| Account | Password | Balance | Role |
|---|---|---:|---|
| `alice` | `alice123` | 10,000,000 VND | regular |
| `admin` | `admin123` | 99,000,000 VND | admin/premium |

Recipients are `Bob`, `Charlie`, and `Test Merchant`; fake transaction PIN is `2468`.

## Setup

1. Install Android SDK Platform 35 and set `ANDROID_HOME` (or create `local.properties` with `sdk.dir=...`).
2. Start the local server following [local-server/README.md](local-server/README.md).
3. Install `local-server/certs/lab-server.pem` as a user CA on the emulator.
4. Deploy `functions/` and `firebase/firestore.rules.hardened` to the Firebase test project.
5. Build with `./gradlew.bat assembleVulnerableDebug assembleHardenedDebug`.
6. Install the desired APK from `app/build/outputs/apk/`.

Both flavors keep the same Firebase database and UI/UX. The vulnerable flavor calls `createTransferVulnerable`, whose backend deliberately omits PIN/role/limit enforcement. The hardened flavor calls `createTransfer`, whose Callable Function validates the server-side wallet state and atomically updates both balances and transaction histories. The local HTTPS API remains dedicated to the SSL-pinning experiment.

## Reset

Restart `local-server/server.py` to reset in-memory users and transactions. Clear Android app storage to reset local preferences and SQLite data.

## Labs

Read [docs/LAB_GUIDE.md](docs/LAB_GUIDE.md), then capture only fake-data evidence using [evidence/CHECKLIST.md](evidence/CHECKLIST.md).
