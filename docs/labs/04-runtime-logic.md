# Runtime Logic Lab

## Objective
Demonstrate why client-side authorization is insufficient.

## App Function
`AuthManager` controls Premium, Admin, transfer limit, PIN, and Secret Vault UI access.

## Protection or Weakness
Vulnerable client decisions can be hooked and the weak `createTransferVulnerable` backend accepts the resulting request. Hardened mode routes the same UI action to `createTransfer`, a Firebase Callable Function that validates server-side account, PIN, amount, recipient, and balance before atomically updating Firestore.

## Static Analysis
Locate `isAdmin`, `isPremium`, `canTransfer`, `verifyTransactionPin`, and `isSecretVaultUnlocked`.

## Dynamic Analysis
Apply the script, then attempt a transfer above the normal client limit or with a wrong PIN. The vulnerable build demonstrates a successful bypass through the weak backend; the hardened build must receive a permission error while both successful transfers remain visible in the same Firestore UI.

## Frida Script
`frida-scripts/bypass_logic.js`

## Run and Evidence
Capture before/after UI and server denial for fake data only.
