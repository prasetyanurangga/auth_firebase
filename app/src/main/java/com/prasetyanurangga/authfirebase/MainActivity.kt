package com.prasetyanurangga.authfirebase

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
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

    private lateinit var providerGithub : OAuthProvider.Builder

    private lateinit var providerTwitter : OAuthProvider.Builder



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inizializaze Firebase Auth
        auth = Firebase.auth

        //  build Google Sign Option
        val gSign = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_klien_web_google))
                .requestEmail()
                .build()

        // Inizilaizae Provider Github For Github Auth
        providerGithub = OAuthProvider.newBuilder("github.com")

        // Iniziliza Progress Dialog
        showProgressDialog()

        //Inizializae Google Sign In Client
        gSignInClient = GoogleSignIn.getClient(this@MainActivity, gSign)

        // Inizilizaze CallBack Manager For Facebook Auth
        callbackManager = CallbackManager.Factory.create()



        // On Button Login With Google Click
        btn_sig.setOnClickListener {
            signIn()
        }

        // On Button Login With Github Click
        btn_sigit.setOnClickListener {
            signInGithub()
        }

        btn_sitwit.setOnClickListener {
            Toast.makeText(baseContext, "Comming Soon",
                Toast.LENGTH_SHORT).show()
        }



        // setPermission and register callback for Facebook Button
        btn_sif.setReadPermissions("email", "public_profile")
        btn_sif.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG_FACEBOOK, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.e(TAG_FACEBOOK, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG_FACEBOOK, "facebook:onError", error)
            }
        })
    }

    // For facebook Auth
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG_FACEBOOK, "handleFacebookAccessToken:$token")
        progrssDialog.show()
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_FACEBOOK, "signInWithCredential:success")
                        val user = auth.currentUser
                        val intent: Intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                            putExtra("data_name", user?.displayName!!)
                            putExtra("data_from", "Facebook")
                        }
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_FACEBOOK, "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }

                    progrssDialog.hide()
                }
    }

    // For get Result From Intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // For get Result from facebook login intent
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // for get result from google login intent
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                authWithGoogle(account?.idToken!!)
            }
            catch (e: ApiException)
            {
                Log.e(TAG_GOOGLE,"Google Error : ${e.message}")
            }
        }

    }

    // For facebook Auth
    private fun authWithGoogle(idToken : String){
        val crd = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(crd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    Log.d(TAG_GOOGLE, task.result?.user?.displayName!!)
                    val intent: Intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("data_name", task.result?.user?.displayName!!)
                        putExtra("data_from", "Google")
                    }
                    startActivity(intent)

                }
                else{
                    Log.e(TAG_GOOGLE,"Google Auth Error")
                }
            }
    }

    // func signin with google
    private fun signIn(){
        progrssDialog.show()
        val signInIntent = gSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // func signin with github
    private fun signInGithub(){
        progrssDialog.show()
        auth.startActivityForSignInWithProvider(this, providerGithub.build())
                .addOnSuccessListener{ result: AuthResult ->
                    val intent: Intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("data_name", result.user?.displayName!!)
                        putExtra("data_from", "Github")
                    }
                    startActivity(intent)

                }
                .addOnFailureListener {
                    exception -> Log.e(TAG_GITHUB, "Github Auth Error : ${exception.message}")
                }
    }




    private fun showProgressDialog()
    {
        progrssDialog = ProgressDialog(this@MainActivity)
        progrssDialog.setCancelable(false)
        progrssDialog.setMessage("Please Wait......")
    }

    companion object{
        const val RC_SIGN_IN = 9001
        const val TAG_GOOGLE = "MainActivity - Google"
        const val TAG_GITHUB = "MainActivity - Github"
        const val TAG_FACEBOOK = "MainActivity - Facebook"
    }
}
