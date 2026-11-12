# Firebase backend setup for the five experiments

Both APK flavors use the same Firebase Authentication, Firestore collections, and UI. Only the Callable Function changes:

- `vulnerableDebug` calls `createTransferVulnerable`, which intentionally omits server-side PIN, role, limit, and balance enforcement.
- `hardenedDebug` calls `createTransfer`, which reads the sender and recipient from Firestore and atomically validates and updates both wallets and transaction histories.

Deploy from the project root with the Firebase CLI after selecting the test project:

```text
cd functions
npm install
cd ..
firebase deploy --only functions,firestore
```

The hardened rules deny direct client writes to balances and transaction subcollections; the Admin SDK used by the Callable Functions can still perform the atomic transfer. The `firestore.rules.vulnerable` file is retained as an optional comparison configuration for a deliberately weak direct-client lab, not as the default production rule set.

## Expected evidence

Use the same Firebase account and recipient in both builds. After the client-side Frida bypass:

| Build | Backend function | Expected result |
|---|---|---|
| Vulnerable | `createTransferVulnerable` | Invalid client decision can reach Firestore and appear in the normal UI/history. |
| Hardened | `createTransfer` | The function rejects invalid PIN/role/limit/balance; valid transfers still update the same Firestore UI/history. |
