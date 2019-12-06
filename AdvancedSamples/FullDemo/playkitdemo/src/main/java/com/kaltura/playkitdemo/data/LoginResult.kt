package com.kaltura.playkitdemo.data

import com.kaltura.netkit.connect.response.BaseResult

/**
 * @hide
 */

class LoginResult : BaseResult() {

    internal var result: Result? = null

    val ks: String?
        get() = if (result != null) result!!.ks else null

    internal inner class Result {
        var loginSession: LoginSession? = null

        val ks: String?
            get() = if (loginSession != null) loginSession!!.ks else null
    }

    internal inner class LoginSession {
        var ks: String? = null
    }
}
