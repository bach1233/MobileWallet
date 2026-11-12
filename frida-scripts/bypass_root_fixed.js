// Root-bypass cho MobileWallet lab.
// Chỉ thay đổi kết quả kiểm tra root ở phía client; không bypass Firebase Auth
// và không bypass FridaDetector.

"use strict";

var rootHookInstalled = false;
var loginHookInstalled = false;
var bannerTimerStarted = false;
var ROOT_CLASS = "vn.hvktmm.bach.securitylab.security.RootDetector";
var LOGIN_CLASS = "vn.hvktmm.bach.securitylab.ui.LoginActivity";

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

function installRootHook() {
  if (rootHookInstalled) return;
  Java.perform(function () {
    useFromAnyLoader(ROOT_CLASS, function (Root) {
      if (rootHookInstalled) return;
      try {
        Root.isDeviceRooted.overload("android.content.Context").implementation = function (context) {
          console.log("[MOBILAB] Root detection bypass");
          return false;
        };
      } catch (e) {
        Root.isDeviceRooted.implementation = function (context) {
          console.log("[MOBILAB] Root detection bypass");
          return false;
        };
      }
      rootHookInstalled = true;
      console.log("[MOBILAB] RootDetector.isDeviceRooted(Context) hooked");
    });
  });
}

// Fallback cho trường hợp LoginActivity đã đi vào nhánh từ chối trước khi
// RootDetector được hook. Không đụng tới FridaDetector.
function installLoginFallbackHook() {
  if (loginHookInstalled) return;
  Java.perform(function () {
    useFromAnyLoader(LOGIN_CLASS, function (Login) {
      if (loginHookInstalled) return;
      try {
        Login.rejectRootAndExit.overload("boolean").implementation = function (isHooked) {
          console.log("[MOBILAB] Root rejection bypass");
          this.completeLogin(!!isHooked);
        };
        loginHookInstalled = true;
        console.log("[MOBILAB] LoginActivity.rejectRootAndExit(Boolean) hooked");
      } catch (e) {}
    });
  });
}

function hideExistingRootBanner() {
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

function startBannerCleanup() {
  if (bannerTimerStarted) return;
  bannerTimerStarted = true;
  setInterval(function () {
    Java.perform(function () { hideExistingRootBanner(); });
  }, 300);
}

Java.perform(function () { startBannerCleanup(); });

// Source dùng source-set vulnerable/hardened nên class có thể được nạp sau
// lúc Frida attach. Retry giúp script hoạt động ở cả spawn và attach.
setInterval(function () {
  installRootHook();
  installLoginFallbackHook();
}, 400);

