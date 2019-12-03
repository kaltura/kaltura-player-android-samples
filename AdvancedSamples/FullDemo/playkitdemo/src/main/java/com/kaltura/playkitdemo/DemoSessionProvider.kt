package com.kaltura.playkitdemo

import com.kaltura.netkit.connect.response.PrimitiveResult
import com.kaltura.netkit.utils.OnCompletion
import com.kaltura.netkit.utils.SessionProvider

/**
 * @hide
 */

class DemoSessionProvider private constructor() : SessionProvider {

    private var ks: String? = null

    fun setKs(ks: String) {
        this.ks = ks
    }

    override fun baseUrl(): String {
        return MockParams.PhoenixBaseUrl
    }

    override fun getSessionToken(completion: OnCompletion<PrimitiveResult>?) {
        completion?.onComplete(PrimitiveResult(this.ks))
    }

    override fun partnerId(): Int {
        return MockParams.OttPartnerId
    }

    companion object {

        private var self: DemoSessionProvider? = null

        val sessionProvider: DemoSessionProvider
            get() {
                if (self == null) {
                    self = DemoSessionProvider()
                }
                return self!!
            }
    }
}
