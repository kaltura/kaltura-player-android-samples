package com.kaltura.kalturaplayertestapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog?.setCancelable(false)
            mProgressDialog?.setMessage("Loading...")
        }

        mProgressDialog?.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog?.isShowing()!!) {
            mProgressDialog?.dismiss()
        }
    }

    fun getUid() : String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

}