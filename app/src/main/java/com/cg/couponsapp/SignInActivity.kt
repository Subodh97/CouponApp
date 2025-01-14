package com.cg.couponsapp


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient : GoogleSignInClient
    val RC_SIGN_IN :Int = 12  // constant value for google sign in response. Can be any independent constant value
    lateinit var gso : GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signInBtn.setOnClickListener {
            doLogin()
        }
        NewUserT.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
        googleLoginBtn.setOnClickListener {
            googleSignIn()
        }
    }
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun doLogin() {
        if(emailLoginE.text.isEmpty()){
            emailLoginE.error ="Please enter email"
            emailLoginE.requestFocus()
            return
        }
        if(passwordLoginE.text.isEmpty()){
            passwordLoginE.error ="Please enter password"
            passwordLoginE.requestFocus()
            return
        }

        //----LOGIN USER---
        auth.signInWithEmailAndPassword(emailLoginE.text.toString(), passwordLoginE.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                } else { //wrong details
                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun updateUI(currentUser : FirebaseUser?){
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                googleSignInFireBase(account.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,"\"Error in signing : ${e.message}\"",Toast.LENGTH_LONG).show()
//                MakeSnackBar(findViewById(android.R.id.content)).make("Error in signing : ${e.message}").show()
            }
        }
    }
    private fun googleSignInFireBase(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = auth.currentUser
//                val users = Users(user?.displayName!!,user.email!!, USER_TYPE,"")
//                if(task.result?.additionalUserInfo?.isNewUser!!) fDatabase.reference.child("users").child(user.uid).setValue(users)
                googleSignInStatus("Success",user)
            } else {
                // If sign in fails, display a message to the user.
                googleSignInStatus("Error : ${task.exception?.message}",null)
            }
        }
    }
    fun googleSignInStatus(msg: String, user: FirebaseUser?) {
        // Checking status of sign in
//        pBar.visibility = View.GONE
        if(msg=="Success")
        {
            updateUI(user)
        }
        else{
            Toast.makeText(this,"$msg",Toast.LENGTH_LONG).show()
        }
    }

}

