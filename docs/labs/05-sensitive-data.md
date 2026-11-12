# Sensitive Data Lab

## Objective
Trace intentionally fake secrets through memory, storage, crypto, and local analytics.

## App Function
`SecretStore`, `SensitiveDataManager`, and `CryptoManager` implement the lab points.

## Protection or Weakness
Vulnerable mode keeps a fake token in SharedPreferences, fake PIN/password in memory, and fake transactions in SQLite. Hardened work should use Keystore and minimize retention.

## Static Analysis
Locate `saveToken`, `loadToken`, `saveTransaction`, `decryptSecret`, and `SensitiveStore`.

## Dynamic Analysis
Log in with fake credentials and attach the script to observe only synthetic values.

## Frida Script
`frida-scripts/dump_sensitive.js`

## Run and Evidence
Never collect real tokens, credentials, PINs, or PII. The vulnerable path uses synthetic values; the hardened path stores the demo secret through Android Keystore AES-GCM.
