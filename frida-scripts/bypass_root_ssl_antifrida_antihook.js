// MobileWallet lab only.
// Gộp: Root detection + SSL pinning + Anti-Frida + Anti-Hook.
// Không can thiệp Firebase Authentication.

"use strict";

var installed = {};

function useFromAnyLoader(className, callback) {
  var delivered = false;

  function deliver(factory) {
    if (delivered) return;
    try {
      callback(factory.use(className));
      delivered = true;
    } catch (e) {}
  }

  try { deliver(Java.classFactory); } catch (e) {}
  if (delivered) return;

  try {
    Java.enumerateClassLoaders({
      onMatch: function (loader) {
        if (delivered) return;
        try { deliver(Java.ClassFactory.get(loader)); } catch (e) {}
      },
      onComplete: function () {}
    });
  } catch (e) {}
}

function hookBoolean(className, methodName, signature, message) {
  var key = className + "." + methodName;
  if (installed[key]) return;

  useFromAnyLoader(className, function (Klass) {
    try {
      var method = signature
        ? Klass[methodName].overload.apply(Klass[methodName], signature)
        : Klass[methodName].overload();

      method.implementation = function () {
        console.log("[MOBILAB] " + message);
        return false;
      };
      installed[key] = true;
      console.log("[MOBILAB] Hooked " + key);
    } catch (e) {
      try {
        Klass[methodName].implementation = function () {
          console.log("[MOBILAB] " + message);
          return false;
        };
        installed[key] = true;
        console.log("[MOBILAB] Hooked " + key);
      } catch (fallbackError) {}
    }
  });
}

function installRootHooks() {
  hookBoolean(
    "vn.hvktmm.bach.securitylab.security.RootDetector",
    "isDeviceRooted",
    ["android.content.Context"],
    "Root detection bypass"
  );

  // Đề phòng các lớp cũ còn được giữ trong source set.
  hookBoolean(
    "vn.hvktmm.bach.securitylab.root.RootDetectionManager",
    "isDeviceRooted",
    ["android.content.Context"],
    "Legacy root detection bypass"
  );
}

function installAntiFridaHooks() {
  hookBoolean(
    "vn.hvktmm.bach.securitylab.security.FridaDetector",
    "isFridaPresent",
    [],
    "Frida detection bypass"
  );

  // Provider cũ trong source set vulnerable/hardened.
  hookBoolean(
    "vn.hvktmm.bach.securitylab.security.SecurityProvider",
    "isFridaDetected",
    [],
    "SecurityProvider Frida detection bypass"
  );
}

function installAntiHookHooks() {
  hookBoolean(
    "vn.hvktmm.bach.securitylab.anti.HookDetectionManager",
    "isHookDetected",
    [],
    "Hook detection bypass"
  );
  hookBoolean(
    "vn.hvktmm.bach.securitylab.anti.HookDetectionManager",
    "detectFridaServer",
    [],
    "Frida server detection bypass"
  );
  hookBoolean(
    "vn.hvktmm.bach.securitylab.anti.HookDetectionManager",
    "detectFridaLibraries",
    [],
    "Frida library detection bypass"
  );
  hookBoolean(
    "vn.hvktmm.bach.securitylab.anti.HookDetectionManager",
    "inspectMaps",
    [],
    "Runtime maps inspection bypass"
  );
}

function installLoginFallback() {
  var key = "LoginActivity.rejectRootAndExit";
  if (installed[key]) return;

  useFromAnyLoader("vn.hvktmm.bach.securitylab.ui.LoginActivity", function (Login) {
    try {
      Login.rejectRootAndExit.overload("boolean").implementation = function (isHooked) {
        console.log("[MOBILAB] Root rejection flow bypass");
        this.completeLogin(false);
      };
      installed[key] = true;
      console.log("[MOBILAB] Login root-rejection fallback hooked");
    } catch (e) {}
  });
}

function installSslHooks() {
  var key = "okhttp3.CertificatePinner.check";
  if (!installed[key]) {
    useFromAnyLoader("okhttp3.CertificatePinner", function (Pinner) {
      try {
        Pinner.check.overload("java.lang.String", "java.util.List").implementation = function (host, certificates) {
          console.log("[MOBILAB] SSL pinning bypass: " + host);
          return;
        };
        installed[key] = true;
        console.log("[MOBILAB] OkHttp CertificatePinner hooked");
      } catch (e) {}
    });
  }

  var trustKey = "TrustManagerImpl.verifyChain";
  if (!installed[trustKey]) {
    try {
      var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
      TrustManagerImpl.verifyChain.implementation = function () {
        console.log("[MOBILAB] TrustManagerImpl.verifyChain bypass");
        return arguments[0];
      };
      installed[trustKey] = true;
      console.log("[MOBILAB] TrustManagerImpl hooked");
    } catch (e) {}
  }
}

function hideRootBanner() {
  Java.choose("android.widget.TextView", {
    onMatch: function (view) {
      try {
        var text = String(view.getText());
        if (text.indexOf("ROOT") >= 0 || text.indexOf("Root") >= 0) view.setVisibility(8);
      } catch (e) {}
    },
    onComplete: function () {}
  });
}

Java.perform(function () {
  setInterval(function () {
    Java.perform(function () {
      installRootHooks();
      installAntiFridaHooks();
      installAntiHookHooks();
      installLoginFallback();
      installSslHooks();
      hideRootBanner();
    });
  }, 400);
});

