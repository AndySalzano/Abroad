package web.abroad.abroadjava

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import web.abroad.abroadjava.model.User

class ForgotPwdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pwd)

        val btnSendEmail = findViewById<Button>(R.id.btn_submit)
        btnSendEmail.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val emailAddress = findViewById<EditText>(R.id.txt_email).toString()

            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Forgot Pwd", "Email sent.")
                            Toast.makeText(applicationContext, "Email sent", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chat -> {
                Toast.makeText(applicationContext, "chat", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.profile -> {
                val logged = User.checkIfUserIsLogged()
                if(logged){
                    val intent = Intent(applicationContext, ProfileActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}