# Root Detection Lab

## Objective
Observe and bypass fake root detection in an authorized emulator.

## App Function
`security.RootDetector` reports root state on splash, login and dashboard.

## Protection or Weakness
The vulnerable build exposes Java methods with readable names; hardened checks are still advisory signals.

## Static Analysis
Locate `isDeviceRooted` in the vulnerable and hardened source sets; the hardened implementation also checks root packages and build properties.

## Dynamic Analysis
On the vulnerable build, verify that the root signal blocks the high-risk transfer flow, then apply `bypass_root.js` and verify that the client gate can be opened. On the hardened build, repeat the bypass and verify that the authoritative transfer service still enforces the account, PIN, amount, and balance policy.

## Frida Script
`frida-scripts/bypass_root.js`

## Run and Evidence
Use an authorized rooted emulator; capture only the fake lab UI. Root detection is not a complete defense.
