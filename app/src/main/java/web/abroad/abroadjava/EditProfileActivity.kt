package web.abroad.abroadjava

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import web.abroad.abroadjava.model.User
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_ID_PICK_CODE = 999
        private const val IMAGE_PROFILE_PICK_CODE = 1000
    }

    private var imageProfileUri : Uri? = null
    private var imageProfileBitmap : Bitmap? = null
    private var imageIDUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        displayCurrentInfo()

        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        imgProfile.setOnClickListener {
            launchGallery(IMAGE_PROFILE_PICK_CODE)
        }

        val btnUploadID = findViewById<Button>(R.id.btn_ID)
        btnUploadID.setOnClickListener {
            launchGallery(IMAGE_ID_PICK_CODE)
        }

        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            updateUser()
        }
    }

    private fun launchGallery(code : Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, code)
    }

    /* Handles result of picked image */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_ID_PICK_CODE){
            val img = findViewById<ImageView>(R.id.img_id)
            img.setImageResource(R.drawable.ic_check)
            imageIDUri = data?.data

            val user = FirebaseAuth.getInstance().currentUser
            if(user != null) {
                val database = Firebase.database
                val myRef = database.getReference("users")
                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (retrievedUser in dataSnapshot.children) {
                            if (retrievedUser.key == user?.uid) {
                                val profileRef = FirebaseStorage.getInstance().getReference("id_images/${user.uid}")
                                profileRef.delete().addOnSuccessListener {
                                    var clientStorage = FirebaseStorage.getInstance().getReference("/id_images/${user.uid}")
                                    clientStorage.putFile(imageIDUri!!).addOnSuccessListener {
                                        img.setImageResource(R.drawable.ic_check)
                                        Toast.makeText(applicationContext, "ID image updated", Toast.LENGTH_SHORT).show()
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(applicationContext, "Failed to update image", Toast.LENGTH_SHORT).show()
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
        }else{
            val img = findViewById<ImageView>(R.id.img_profile)
            val imgRounded = findViewById<ImageView>(R.id.img_rounded_profile)
            img.setImageURI(data?.data)
            imageProfileUri = data?.data
            imgRounded.setImageBitmap(imageProfileBitmap)
            img.alpha = 0f
            imageProfileBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageProfileUri)

            val user = FirebaseAuth.getInstance().currentUser
            if(user != null) {
                val database = Firebase.database
                val myRef = database.getReference("users")
                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (retrievedUser in dataSnapshot.children) {
                            if (retrievedUser.key == user?.uid) {
                                val profileRef = FirebaseStorage.getInstance().getReference("profile_images/${user.uid}")
                                profileRef.delete().addOnSuccessListener {
                                    var clientStorage = FirebaseStorage.getInstance().getReference("/profile_images/${user.uid}")
                                    clientStorage.putFile(imageProfileUri!!).addOnSuccessListener {
                                        imgRounded.setImageURI(imageProfileUri)
                                        Toast.makeText(applicationContext, "Profile image updated", Toast.LENGTH_SHORT).show()
                                    }
                                }.addOnFailureListener{
                                    Toast.makeText(applicationContext, "Failed to update image", Toast.LENGTH_SHORT).show()
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
    }

    private fun displayCurrentInfo(){
        val layout = findViewById<LinearLayout>(R.id.LinearLayoutEditProfile)
        val txtName = findViewById<EditText>(R.id.txt_name)
        val txtLastname = findViewById<EditText>(R.id.txt_lastname)
        val txtHomeCountry = findViewById<EditText>(R.id.txt_home_country)
        val txtHomeCity = findViewById<EditText>(R.id.txt_home_city)
        val txtDestinationCountry = findViewById<EditText>(R.id.txt_destination_country)
        val txtDestinationCity = findViewById<EditText>(R.id.txt_destination_city)
        val txtCurrentStudies = findViewById<EditText>(R.id.txt_current_studies)

        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            val database = Firebase.database
            val myRef = database.getReference("users")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (retrievedUser in dataSnapshot.children) {
                        if (retrievedUser.key == user?.uid) {

                            val storageReference = Firebase.storage.getReference("/profile_images/${user.uid}")
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

                            val isOwner = retrievedUser.child("owner").value as Boolean
                            val name = retrievedUser.child("name").value.toString()
                            txtName.setText(name)

                            val lastname = retrievedUser.child("lastname").value.toString()
                            txtLastname.setText(lastname)

                            val homeCountry = retrievedUser.child("homeCountry").value.toString()
                            txtHomeCountry.setText(homeCountry)

                            val homeCity = retrievedUser.child("homeCity").value.toString()
                            txtHomeCity.setText(homeCity)

                            if(!isOwner){
                                val destinationCountry = retrievedUser.child("destinationCountry").value.toString()
                                txtDestinationCountry.setText(destinationCountry)

                                val destinationCity = retrievedUser.child("destinationCity").value.toString()
                                txtDestinationCity.setText(destinationCity)

                                val currentStudies = retrievedUser.child("currentStudies").value.toString()
                                txtCurrentStudies.setText(currentStudies)
                            }else{
                                layout.removeView(findViewById<View>(R.id.view1))
                                layout.removeView(findViewById<View>(R.id.view2))
                                layout.removeView(findViewById<View>(R.id.view3))
                                layout.removeView(txtDestinationCountry)
                                layout.removeView(txtDestinationCity)
                                layout.removeView(txtCurrentStudies)
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

    private fun updateUser(){
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            val database = Firebase.database
            val myRef = database.getReference("users")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (retrievedUser in dataSnapshot.children) {
                        if (retrievedUser.key == user?.uid) {
                            val txtName = findViewById<EditText>(R.id.txt_name).text.toString()
                            val txtLastname = findViewById<EditText>(R.id.txt_lastname).text.toString()
                            val txtHomeCountry = findViewById<EditText>(R.id.txt_home_country).text.toString()
                            val txtHomeCity = findViewById<EditText>(R.id.txt_home_city).text.toString()

                            database.reference.child("users").child(user.uid).child("name").
                                    setValue(txtName)
                            database.reference.child("users").child(user.uid).child("lastname").
                                    setValue(txtLastname)
                            database.reference.child("users").child(user.uid).child("homeCountry").
                                    setValue(txtHomeCountry)
                            database.reference.child("users").child(user.uid).child("homeCity").
                                    setValue(txtHomeCity)

                            val isOwner = retrievedUser.child("owner").value as Boolean
                            if(!isOwner){
                                val txtDestinationCountry = findViewById<EditText>(R.id.txt_destination_country).text.toString()
                                val txtDestinationCity = findViewById<EditText>(R.id.txt_destination_city).text.toString()
                                val txtCurrentStudies = findViewById<EditText>(R.id.txt_current_studies).text.toString()
                                database.reference.child("users").child(user.uid).child("destinationCountry").
                                        setValue(txtDestinationCountry)
                                database.reference.child("users").child(user.uid).child("destinationCity").
                                        setValue(txtDestinationCity)
                                database.reference.child("users").child(user.uid).child("currentStudies").
                                        setValue(txtCurrentStudies)
                            }

                            Toast.makeText(applicationContext, "Information updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, ProfileActivity::class.java)
                            startActivity(intent)
                            break
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
