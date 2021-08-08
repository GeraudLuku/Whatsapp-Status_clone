package com.jibee.upwork01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.jibee.upwork01.repo.StoriesViewModel

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var storiesViewModel: StoriesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)


        //attach activity to view model
        storiesViewModel = ViewModelProvider(this).get(StoriesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if(auth.currentUser != null)
            Log.d("Auth","User is Signed In")
        else {
            Log.d("Auth", "User is not Signed In")
            //sign in anonymously
            createUser()
        }
    }

    private fun createUser() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Auth", "signInAnonymously:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Auth", "signInAnonymously:failure", task.exception)
                }
            }
    }
}