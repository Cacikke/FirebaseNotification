package com.example.firebaselogin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.firebaselogin.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //SETUP
        setup()
        session()
    }

    private fun session() {

        val prefs: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        val provider = prefs.getString("provider",null)
        if (email != null && provider != null){
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        title = "Autenticación"

        //BOTÓN DE REGISTRO
        binding.signUpButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?:"",ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                }
            }
        }

        //BOTÓN DE LOGIN
        binding.logInButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){

                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()).addOnCompleteListener{
                    if(it.isSuccessful){
                        showHome(it.result?.user?.email ?:"",ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            }
        }

        //BOTÓN DE GOOGLE
        binding.googleButton.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_IN){
            val task= GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if(account != null){
                val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                    if(it.isSuccessful){
                        showHome(account.email ?: "", ProviderType.GOOGLE)
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }
}