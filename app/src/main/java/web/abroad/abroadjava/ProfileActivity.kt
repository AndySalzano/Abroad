package web.abroad.abroadjava

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import web.abroad.abroadjava.model.User
import java.io.File

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        retrieveInfoFromUser()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnEditInfo = findViewById<Button>(R.id.btn_edit_info)
        btnEditInfo.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        val btnAccommodations = findViewById<Button>(R.id.btn_accommodations)
        btnAccommodations.setOnClickListener {
            val intent = Intent(this, MyAccommodationsActivity::class.java)
            startActivity(intent)
        }

        val btnDeactivate = findViewById<Button>(R.id.btn_deactivate_account)
        btnDeactivate.setOnClickListener {
            Toast.makeText(baseContext, "Deactivating account", Toast.LENGTH_SHORT).show()
            val user = FirebaseAuth.getInstance().currentUser
            deleteUserInDatabase(user?.uid)
        }
    }

    private fun deleteUserInDatabase(uid : String?){

        /* Now deletes his ID and profile images from the Storage */
        val storageRef = Firebase.storage.reference
        Log.d("Profile", "$storageRef")
        val profileRef = storageRef.child("profile_images/$uid")
        profileRef.delete().addOnSuccessListener {
            val idRef = storageRef.child("id_images/$uid")
            idRef.delete().addOnSuccessListener {
                val clientDatabase = FirebaseDatabase.getInstance().getReference("/users/$uid")
                clientDatabase.removeValue()
                deleteUserInAuth()
                Toast.makeText(baseContext, "Account deactivated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(baseContext, "Something went wrong, try to log in again", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(baseContext, "Something went wrong, try to log in again", Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteUserInAuth(){
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
    }

    private fun retrieveInfoFromUser() {
        val uid = FirebaseAuth.getInstance().uid
        lateinit var user: DataSnapshot
        Log.d("login", "$uid")
        if(uid != null){
            val database = Firebase.database
            val myRef = database.getReference("users")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (retrievedUser in dataSnapshot.children) {
                        if(retrievedUser.key == uid){
                            /* We change the data displayed in the top for the one of the user */
                            /* Profile image */
                            val storageReference = Firebase.storage.getReference("/profile_images/$uid")
                            val imageView = findViewById<ImageView>(R.id.img_profile)
                            val imgRounded = findViewById<ImageView>(R.id.img_rounded_profile)
                            val file = File.createTempFile("img", "jpg")
                            storageReference.getFile(file).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                imgRounded.setImageBitmap(bitmap)
                                imageView.alpha = 0f
                            }.addOnFailureListener{
                                imageView.setImageResource(R.drawable.img_circle_2)
                            }

                            /* User email and role */
                            val email = retrievedUser.child("email").value.toString()
                            val emailView = findViewById<TextView>(R.id.txt_email)
                            emailView.text = email

                            val isOwner = retrievedUser.child("owner").value as Boolean
                            val roleView = findViewById<TextView>(R.id.txt_role)
                            if(isOwner){
                                roleView.text = resources.getString(R.string.role_owner)
                            }else{
                                roleView.text = resources.getString(R.string.role_user)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("Login", "Failed to read value.", error.toException())
                }
            })
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
