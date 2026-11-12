Java.perform(function () {
  var Auth = Java.use("vn.hvktmm.bach.securitylab.logic.AuthManager");
  Auth.isPremium.implementation = function () { console.log("[MOBILAB] Premium forced"); return true; };
  Auth.isAdmin.implementation = function () { console.log("[MOBILAB] Admin forced"); return true; };
  Auth.canTransfer.implementation = function (amount) { console.log("[MOBILAB] Limit bypass: " + amount); return true; };
  Auth.verifyTransactionPin.implementation = function (pin) { console.log("[MOBILAB] PIN bypass"); return true; };
  Auth.isSecretVaultUnlocked.implementation = function () { return true; };
  console.log("[MOBILAB] Runtime logic hooks installed");
});
