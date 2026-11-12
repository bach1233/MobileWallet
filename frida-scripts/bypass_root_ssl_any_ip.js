'use strict';

var rootHooked = false, loginFallback = false;
var pinHooked = false, trustHooked = false, hostHooked = false;
var getterHooked = false, newCallHooked = false;
var bypassVerifier = null, emptyPinner = null;
// TLS hooks are host/IP agnostic for this app's OkHttp + Android Conscrypt path.
// Route the emulator-only host alias to the local Burp/backend loopback; keep Host 10.0.2.2:8443.

function appUse(name) {
  try {
    var Api = Java.use('vn.hvktmm.bach.securitylab.network.WalletApiClient');
    return Java.ClassFactory.get(Api.class.getClassLoader()).use(name);
  } catch (_) { return Java.use(name); }
}

function useClass(name, callback) {
  var done = false;
  function tryFactory(factory) {
    if (done) return;
    try { callback(factory.use(name)); done = true; } catch (_) {}
  }
  tryFactory(Java.classFactory);
  if (!done) try {
    Java.enumerateClassLoaders({
      onMatch: function (loader) { tryFactory(Java.ClassFactory.get(loader)); },
      onComplete: function () {}
    });
  } catch (_) {}
}

function installRoot() {
  if (!rootHooked) useClass('vn.hvktmm.bach.securitylab.security.RootDetector', function (Root) {
    Root.isDeviceRooted.overload('android.content.Context').implementation = function (_) {
      console.log('[ROOT] detection bypass');
      return false;
    };
    rootHooked = true;
    console.log('[ROOT] RootDetector hooked');
  });
  if (!loginFallback) useClass('vn.hvktmm.bach.securitylab.ui.LoginActivity', function (Login) {
    try {
      Login.rejectRootAndExit.overload('boolean').implementation = function (hooked) {
        console.log('[ROOT] rejection fallback');
        this.completeLogin(!!hooked);
      };
      loginFallback = true;
      console.log('[ROOT] login fallback hooked');
    } catch (_) {}
  });
}

function installTls() {
  if (!pinHooked) try {
    var Pinner = Java.use('okhttp3.CertificatePinner');
    ['check', 'check$okhttp'].forEach(function (name) {
      try { Pinner[name].overloads.forEach(function (method) {
        method.implementation = function () {
          console.log('[TLS] pin check bypass: ' + (arguments.length ? arguments[0] : '*'));
          return;
        };
      }); } catch (_) {}
    });
    emptyPinner = Java.use('okhttp3.CertificatePinner$Builder').$new().build();
    pinHooked = true;
    console.log('[TLS] CertificatePinner hooked');
  } catch (_) {}
  if (!trustHooked) try {
    var T = Java.use('com.android.org.conscrypt.TrustManagerImpl');
    T.verifyChain.overloads.forEach(function (method) {
      method.implementation = function () { console.log('[TLS] trust chain accepted'); return arguments[0]; };
    });
    trustHooked = true;
  } catch (_) {}
  if (!hostHooked) try {
    var H = Java.use('javax.net.ssl.HostnameVerifier');
    bypassVerifier = Java.registerClass({
      name: 'com.mobilelab.AllowAllHostnameVerifier',
      implements: [H],
      methods: { verify: function (host, _) { console.log('[TLS] hostname accepted: ' + host); return true; } }
    }).$new();
    Java.use('okhttp3.OkHttpClient').hostnameVerifier.overload().implementation = function () { return bypassVerifier; };
    hostHooked = true;
    console.log('[TLS] all-host hostname verifier installed');
  } catch (_) {}
}

function installRoute() {
  if (getterHooked) return;
  try {
    var Cached = Java.use('vn.hvktmm.bach.securitylab.network.PinnedHttpClient');
    var get = Cached.get.overload();
    get.implementation = function () {
      var client = get.call(this);
      if (!newCallHooked) try {
        var Factory = Java.ClassFactory.get(client.getClass().getClassLoader());
        var Client = Factory.use('okhttp3.OkHttpClient');
        var newCall = Client.newCall.overload('okhttp3.Request');
        newCall.implementation = function (request) {
          var raw = request.url().toString();
          var routed = request;
          try {
            var url = request.url();
            if (String(url.host()) === '10.0.2.2' && Number(url.port()) === 8443) {
              var upstream = url.newBuilder().host('127.0.0.1').build();
              routed = request.newBuilder()
                .url(upstream)
                .header('Host', '10.0.2.2:8443')
                .build();
              console.log('[ROUTE] backend target 10.0.2.2:8443 -> 127.0.0.1:8443; Host preserved');
            } else {
              console.log('[ROUTE] unchanged target: ' + raw);
            }
          } catch (e) { console.log('[ROUTE] rewrite failed, keeping ' + raw + ': ' + e); }
          return newCall.call(this, routed);
        };
        newCallHooked = true;
        console.log('[ROUTE] OkHttp newCall hook installed on client loader');
      } catch (e) { console.log('[ROUTE] setup failed: ' + e); }
      try {
        var cls = client.getClass();
        var host = cls.getDeclaredField('hostnameVerifier');
        var pin = cls.getDeclaredField('certificatePinner');
        host.setAccessible(true); pin.setAccessible(true);
        host.set(client, bypassVerifier); pin.set(client, emptyPinner);
        console.log('[TLS] cached OkHttp client patched');
      } catch (e) { console.log('[TLS] cached client patch failed: ' + e); }
      return client;
    };
    getterHooked = true;
    console.log('[ROUTE] PinnedHttpClient hook installed');
  } catch (e) { console.log('[ROUTE] setup failed: ' + e); }
}

setInterval(function () {
  Java.perform(function () { installRoot(); installTls(); installRoute(); });
}, 350);
