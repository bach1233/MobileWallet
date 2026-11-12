# Project Mapping

| Thesis module | Android implementation | Lab script |
|---|---|---|
| Root detection | `security/RootDetector` | `bypass_root.js` |
| SSL pinning | `network/PinnedHttpClient`, `HomeActivity` status check | `bypass_ssl_pinning.js` |
| Anti-Frida | `security/FridaDetector` | `bypass_frida.js` |
| Runtime logic | `logic/AuthManager` | `bypass_logic.js` |
| Sensitive data | `security/SensitiveStore`, `storage/SecretStore`, `crypto/CryptoManager` | `dump_sensitive.js` |

The hardened flavor applies stricter client checks and hardened local storage/crypto. The local API is used for the SSL status experiment. Both flavors use the same Firebase UI/database; vulnerable calls `createTransferVulnerable`, while hardened calls the server-authoritative `createTransfer` Callable Function. All data is fake.
