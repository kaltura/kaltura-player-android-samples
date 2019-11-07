package com.kaltura.kalturaplayertestapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kaltura.kalturaplayertestapp.models.User
import java.util.*

class SignInActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "SignInActivity"

    private var mFirestore: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private var mEmailField: EditText? = null
    private var mPasswordField: EditText? = null
    private var mSignInButton: Button? = null
    private var mSignUpButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mFirestore = FirebaseFirestore.getInstance()
        mFirestore = mFirestore
        mAuth = FirebaseAuth.getInstance()

        // Views
        mEmailField = findViewById(R.id.field_email)
        mPasswordField = findViewById(R.id.field_password)
        mSignInButton = findViewById(R.id.button_sign_in)
        mSignUpButton = findViewById(R.id.button_sign_up)

        // Click listeners
        mSignInButton?.setOnClickListener(this)
        mSignUpButton?.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()

        // Check auth on Activity start
        if (mAuth?.getCurrentUser() != null) {
            onAuthSuccess(mAuth?.getCurrentUser()!!)
        }
    }

    private fun signIn() {
        Log.d(TAG, "signIn")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = mEmailField?.getText().toString()
        val password = mPasswordField?.getText().toString()

        mAuth?.signInWithEmailAndPassword(email, password)!!
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result!!.user)
                    } else {
                        Toast.makeText(this@SignInActivity, "Sign In Failed: " + task.exception!!.message,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun signUp() {
        Log.d(TAG, "signUp")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = mEmailField?.getText().toString()
        val password = mPasswordField?.getText().toString()

        mAuth?.createUserWithEmailAndPassword(email, password)!!
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result!!.user)
                    } else {
                        Toast.makeText(this@SignInActivity, "Sign Up Failed: " + task.exception!!.message,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun onAuthSuccess(user: FirebaseUser) {
        val username = usernameFromEmail(user.email!!)

        // Write new user
        writeNewUser(user.uid, username, user.email)

        // Go to MainActivity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(mEmailField?.getText().toString())) {
            mEmailField?.setError("Required")
            result = false
        } else {
            mEmailField?.setError(null)
        }

        if (TextUtils.isEmpty(mPasswordField?.getText().toString())) {
            mPasswordField?.setError("Required")
            result = false
        } else {
            mPasswordField?.setError(null)
        }

        return result
    }

    private fun writeNewUser(userId: String, name: String, email: String?) {
        val (username, email1) = User(name, email)
        val users = mFirestore?.collection("users")
        val data = HashMap<String, Any>()
        data["name"] = email!!
        users?.document(userId)?.set(data)

        //        users.document(userId).collection("configurations").document().set(user);
        //        user.setEmail("zzzzzz@zzzzz.com");
        //        user.setUsername(user.getUsername() + "-xxx");
        //        users.document(userId).collection("configurations").document().set(user);
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.button_sign_in) {
            signIn()
        } else if (i == R.id.button_sign_up) {
            signUp()
        }
    }
}