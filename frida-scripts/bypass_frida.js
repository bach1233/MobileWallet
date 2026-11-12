"use strict";

var installed = false;
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
function install() {
  if (installed) return;
  useFromAnyLoader("vn.hvktmm.bach.securitylab.security.FridaDetector", function (Detector) {
    try {
      Detector.isFridaPresent.overload().implementation = function () {
        console.log("[MOBILAB] Frida detection bypass");
        return false;
      };
      installed = true;
      console.log("[MOBILAB] FridaDetector.isFridaPresent() hooked");
    } catch (e) {}
  });
}
setInterval(function () { Java.perform(install); }, 400);
