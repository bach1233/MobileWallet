Java.perform(function () {
  var Root = Java.use("vn.hvktmm.bach.securitylab.security.RootDetector");
  Root.isDeviceRooted.implementation = function () { return false; };
  var Frida = Java.use("vn.hvktmm.bach.securitylab.security.FridaDetector");
  Frida.isFridaPresent.implementation = function () { return false; };
  var Auth = Java.use("vn.hvktmm.bach.securitylab.logic.AuthManager");
  Auth.isPremium.implementation = function () { return true; };
  Auth.isAdmin.implementation = function () { return true; };
  Auth.canTransfer.implementation = function () { return true; };
  Auth.verifyTransactionPin.implementation = function () { return true; };
  Auth.isSecretVaultUnlocked.implementation = function () { return true; };
  try { Java.use("okhttp3.CertificatePinner").check.overload("java.lang.String", "java.util.List").implementation = function () {}; } catch (e) { console.log(e); }
  console.log("[MOBILAB] all bypass hooks installed");
});
