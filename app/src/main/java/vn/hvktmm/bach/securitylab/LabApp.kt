package vn.hvktmm.bach.securitylab

import android.app.Application
import vn.hvktmm.bach.securitylab.crypto.CryptoManager
import vn.hvktmm.bach.securitylab.storage.SecretStore

class LabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SecretStore.initialize(this)
        CryptoManager.initialize(this)
    }
}
