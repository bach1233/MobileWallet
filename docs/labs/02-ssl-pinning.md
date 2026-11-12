# SSL Pinning Lab

## Objective
Inspect local HTTPS traffic and the deliberate vulnerable pin.

## App Function
`HomeActivity` uses `PinnedHttpClient` to call the local HTTPS `/status` endpoint.

## Protection or Weakness
Vulnerable has a fake readable pin; hardened has the local certificate SPKI pin and normal hostname validation.

## Static Analysis
Locate `NetworkConfig` and `PinnedHttpClient`.

## Dynamic Analysis
Start the local server, then compare the `/status` connectivity toast before and after `bypass_ssl_pinning.js` in the vulnerable flavor.

## Frida Script
`frida-scripts/bypass_ssl_pinning.js`

## Run and Evidence
Capture fake request/response data only. Client pinning does not replace server authorization.
