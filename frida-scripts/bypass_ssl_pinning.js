"use strict";

var pinInstalled = false;
var trustInstalled = false;

function useFromAnyLoader(name, callback) {
  var delivered = false;
  function tryFactory(factory) {
    if (delivered) return;
    try { callback(factory.use(name)); delivered = true; } catch (e) {}
  }
  tryFactory(Java.classFactory);
  if (delivered) return;
  try {
    Java.enumerateClassLoaders({
      onMatch: function (loader) { tryFactory(Java.ClassFactory.get(loader)); },
      onComplete: function () {}
    });
  } catch (e) {}
}

function installPinHook() {
  if (pinInstalled) return;
  useFromAnyLoader("okhttp3.CertificatePinner", function (Pinner) {
    try {
      Pinner.check.overload("java.lang.String", "java.util.List").implementation = function (host, certificates) {
        console.log("[MOBILAB] SSL pinning bypass: " + host);
        return;
      };
      pinInstalled = true;
      console.log("[MOBILAB] OkHttp CertificatePinner hooked");
    } catch (e) { console.log("[MOBILAB] CertificatePinner hook failed: " + e); }
  });
}

function installTrustHook() {
  if (trustInstalled) return;
  try {
    var TrustManager = Java.use("com.android.org.conscrypt.TrustManagerImpl");
    TrustManager.verifyChain.implementation = function () {
      console.log("[MOBILAB] Conscrypt verifyChain bypass");
      return arguments[0];
    };
    trustInstalled = true;
  } catch (e) {}
}

setInterval(function () {
  Java.perform(function () { installPinHook(); installTrustHook(); });
}, 400);
