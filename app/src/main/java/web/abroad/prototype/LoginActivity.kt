package web.abroad.prototype

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import web.abroad.prototype.model.User

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.txt_username).text.toString()
            val pwd = findViewById<EditText>(R.id.txt_pwd).text.toString()
            loginUser(email, pwd)
        }

        val btnCreateAccount = findViewById<Button>(R.id.btn_create_account)
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("key", "Kotlin")
            startActivity(intent)
        }

        val btnCreateOwner = findViewById<Button>(R.id.btn_create_owner)
        btnCreateOwner.setOnClickListener {
            val intent = Intent(this, CreateOwnerActivity::class.java)
            startActivity(intent)
        }

        val btnForgotPwd = findViewById<Button>(R.id.btn_forgot_password)
        btnForgotPwd.setOnClickListener {
            val intent = Intent(this, ForgotPwdActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, pwd : String){
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "signInWithEmail:success")
                        Toast.makeText(baseContext, "Now you are logged", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed, check the credentials",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val logged = User.checkIfUserIsLogged()
        when (item.itemId) {
            R.id.chat -> {
                if(logged){
                    val intent = Intent(applicationContext, ChatsActivity::class.java)
                    startActivity(intent);
                }else{
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
                return true
            }
            R.id.profile -> {
                if(logged){
                    val intent = Intent(applicationContext, ProfileActivity::class.java)
                    startActivity(intent);
                }else {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}