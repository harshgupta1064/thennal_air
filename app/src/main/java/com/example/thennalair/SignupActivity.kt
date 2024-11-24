package com.example.thennalair

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.thennalair.databinding.ActivitySignupBinding
import com.example.thennalair.models.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import org.json.JSONException
import java.util.*

class SignupActivity : AppCompatActivity() {
    val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private lateinit var phone: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var user: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // Initialize Google Sign-In
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Already Have Account Click Listener
        binding.haveAccount.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
        }

        // Sign Up Button Click Listener
        binding.signupButton.setOnClickListener {
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()
            phone = binding.phoneNumber.text.toString()
            name = binding.name.text.toString().trim()
            if (email.isNotBlank() && phone.isNotBlank() && name.isNotBlank() && password.isNotBlank()) {
                registerUserUsingEmailAndPassword(email, password)
            } else {
                Toast.makeText(applicationContext, "All Fields are Required", Toast.LENGTH_LONG).show()
            }
        }

        // Google Sign-In Button Click Listener
        binding.googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        // Successfully signed in with Google
                        Toast.makeText(this, "Successfully Signed in with Google", Toast.LENGTH_SHORT).show()
                        name = account.displayName!!
                        email = account.email!!
                        saveUserData()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Google Sign in failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUserUsingEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "This Email is already registered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid!!
        try {
            user = UserModel(userId=userId,userName = name, email = email, password = password, phoneNumber = phone)
            database.child("Users").child(userId).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "user data saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "user data not saved", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
        }
    }
}