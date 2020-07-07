package com.prasetyanurangga.authfirebase

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prasetyanurangga.authfirebase.databinding.ActivityLayoutMainBinding
import kotlinx.android.synthetic.main.activity_layout_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLayoutMainBinding

    private lateinit var gSignInClient: GoogleSignInClient

    private lateinit var progrssDialog: ProgressDialog

    private lateinit var callbackManager: CallbackManager

     val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressDialog()

        val gSign = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_klien_web_google))
            .requestEmail()
            .build()

        gSignInClient = GoogleSignIn.getClient(this@MainActivity, gSign)

        auth = Firebase.auth

        btn_sig.setOnClickListener {
            signIn()
        }

        btn_so.setOnClickListener {
            signOut()
        }

        callbackManager = CallbackManager.Factory.create()

        btn_sif.setReadPermissions("email", "public_profile")
        btn_sif.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("Hasil", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("Hasil", "facebook:onCancel")
                // [START_EXCLUDE]
                updateUI("","Facebook", false)
                // [END_EXCLUDE]
            }

            override fun onError(error: FacebookException) {
                Log.d("Hasil", "facebook:onError", error)
                // [START_EXCLUDE]
                updateUI("","Facebook", false)
                // [END_EXCLUDE]
            }
        })
    }

    override fun onStart() {
        super.onStart()


    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("Hasil", "handleFacebookAccessToken:$token")
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]
        progrssDialog.show()
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Hasil", "signInWithCredential:success")
                        val user = auth.currentUser
                        updateUI(user?.displayName!!, "Facebook", true)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("hasil", "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI("","Facebook", false)
                    }

                    // [START_EXCLUDE]
                    // [END_EXCLUDE]

                    progrssDialog.hide()
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                authWithGoogle(account?.idToken!!)
            }
            catch (e: ApiException)
            {
                updateUI("","Google", false)
            }
        }
    }

    private fun authWithGoogle(idToken : String){
        val crd = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(crd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    Log.d("Hasil Auth", task.result?.user?.displayName!!)
                    updateUI(task.result?.user?.displayName!!,"Google", true)

                }
                else{
                    updateUI("","Google", false)
                }
            }
    }

    private fun signIn(){
        progrssDialog.show()
        val signInIntent = gSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut(){
        progrssDialog.show()
        auth.signOut()
        gSignInClient.signOut().addOnCompleteListener {
            updateUI("","Google", false)

        }
    }

    private fun updateUI(name: String, from : String, isLogin: Boolean){
        txt_name.text = name
        txt_from.text = from
        txt_is_login.text = if (isLogin)  "User Login" else "User Not Login"
        progrssDialog.hide()
    }

    private fun showProgressDialog()
    {
        progrssDialog = ProgressDialog(this@MainActivity)
        progrssDialog.setCancelable(false)
        progrssDialog.setMessage("Please Wait......")
    }
}
