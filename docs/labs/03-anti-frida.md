# Anti-Frida Lab

## Objective
Observe anti-hook signals and a controlled bypass.

## App Function
`security.FridaDetector` checks Frida ports and `/proc/self/maps` in the hardened flavor.

## Protection or Weakness
The vulnerable Java checks are readable and hookable; hardened deployments should combine independent signals.

## Static Analysis
Locate `isFridaPresent` and its hardened port/maps checks.

## Dynamic Analysis
Attach in an authorized emulator and compare the dashboard state and transfer gate. In hardened mode, bypassing the detector must not bypass the server-side transfer policy.

## Frida Script
`frida-scripts/bypass_frida.js`

## Run and Evidence
Do not use on unowned devices. Anti-hook controls are detection signals, never sole protection.
