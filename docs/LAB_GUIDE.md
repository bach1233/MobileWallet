# MobiWallet Lab Guide

## Objective
Study dynamic instrumentation against a deliberately vulnerable Android wallet simulation in an isolated lab.

## App Functions
Local HTTPS login, wallet balance, fake transfers, history, Premium, Admin, and Secret Vault.

## Static Analysis
Use JADX to locate the five classes in `docs/PROJECT_MAPPING.md`. Names are intentionally readable in the vulnerable variant.

## Dynamic Analysis
Use a rooted emulator authorized for the lab. Start the local API, install the lab CA, then attach Frida to the vulnerable APK.

## Frida Scripts
The five scripts are in `frida-scripts/`; `all_bypass.js` is a combined demonstration script.

## Before and After
Record client checks before a hook and the altered client behavior after it. The hardened API should still deny invalid transfers.

## Evidence
Use `evidence/CHECKLIST.md`. Never record real credentials, accounts, tokens, or personal data.

## Risks and Limits
Client controls can be changed at runtime. Root and anti-hook checks are signals, not standalone security controls. This project does not model a production financial system.
