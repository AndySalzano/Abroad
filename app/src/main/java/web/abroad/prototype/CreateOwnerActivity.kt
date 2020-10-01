package web.abroad.prototype

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import web.abroad.prototype.model.User

class CreateOwnerActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_ID_PICK_CODE = 999
        private const val IMAGE_PROFILE_PICK_CODE = 1000
    }

    private var imageProfileUri : Uri? = null
    private var imageProfileBitmap : Bitmap? = null
    private var imageIDUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_owner)

        val btnUploadID = findViewById<Button>(R.id.btn_ID)
        btnUploadID.setOnClickListener {
            launchGallery(IMAGE_ID_PICK_CODE)
        }

        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            val txtName = findViewById<EditText>(R.id.txt_name).text.toString()
            val txtLastname = findViewById<EditText>(R.id.txt_lastname).text.toString()
            val txtEmail = findViewById<EditText>(R.id.txt_email).text.toString()
            val txtPwd = findViewById<EditText>(R.id.txt_pwd).text.toString()
            val txtHomeCountry = findViewById<EditText>(R.id.txt_home_country).text.toString()
            val txtHomeCity = findViewById<EditText>(R.id.txt_home_city).text.toString()

            val infoFilled = checkForEmptySlots(
                    txtName,
                    txtLastname,
                    txtEmail,
                    txtPwd,
                    txtHomeCountry,
                    txtHomeCity
            )

            if(infoFilled){
                authUserIntoFirebase(
                        txtName,
                        txtLastname,
                        txtEmail,
                        txtPwd,
                        txtHomeCountry,
                        txtHomeCity,
                        true
                )
            }
        }

        val imgProfile = findViewById<ImageView>(R.id.img_profile)
        imgProfile.setOnClickListener {
            launchGallery(IMAGE_PROFILE_PICK_CODE)
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
        }else{
            val img = findViewById<ImageView>(R.id.img_profile)
            val imgRounded = findViewById<ImageView>(R.id.img_rounded_profile)
            img.setImageURI(data?.data)
            imageProfileUri = data?.data
            imageProfileBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageProfileUri)

            imgRounded.setImageBitmap(imageProfileBitmap)
            img.alpha = 0f
        }
    }


    /**
     * Saves all the info from the user into the Firebase Database & Storage
     */
    private fun saveUser(
            uid : String,
            name: String,
            lastname: String,
            email: String,
            pwd: String,
            homeCountry : String,
            homeCity : String,
            isOwner : Boolean
    ) {
        Toast.makeText(applicationContext,"Creating account", Toast.LENGTH_SHORT).show()
        /* Saves the profile img into Firebase Storage */
        var clientStorage = FirebaseStorage.getInstance().getReference("/profile_images/$uid")
        clientStorage.putFile(imageProfileUri!!).addOnSuccessListener {
            Log.d("Register", "Successfully uploaded image ${it.metadata?.path}")
            /* If the image is succesfully stored, we proceed to save the user into the Database */
            clientStorage.downloadUrl.addOnSuccessListener{
                val clientDatabase = FirebaseDatabase.getInstance().getReference("/users/$uid")

                val imgProfile = it.toString()
                val user = User(
                        uid,
                        name,
                        lastname,
                        email,
                        pwd,
                        imgProfile,
                        homeCountry,
                        homeCity,
                        "",
                        "",
                        "",
                        isOwner
                )

                clientDatabase.setValue(user)
                        .addOnSuccessListener {
                            Log.d("Register", "Successfully user saved")
                            /* We save the ID photos with the same uid than the user */
                            clientStorage = FirebaseStorage.getInstance().getReference("/id_images/$uid")
                            clientStorage.putFile(imageIDUri!!).addOnSuccessListener {
                                Log.d("Register", "Successfully uploaded image ${it.metadata?.path}")
                                Toast.makeText(applicationContext,"Account created", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener{
                            Log.d("Register", "User not saved")
                        }
            }
        }
    }

    /**
     * Checks all information fields have been filled
     */
    private fun checkForEmptySlots(
            name: String,
            lastname: String,
            email: String,
            pwd: String,
            homeCountry : String,
            homeCity : String
    ) : Boolean {

        if(name.isNotEmpty() && lastname.isNotEmpty() && email.isNotEmpty()
                && pwd.isNotEmpty() && homeCountry.isNotEmpty()
                && homeCity.isNotEmpty() && imageProfileUri != null && imageIDUri != null) {
            return true
        }else{
            /* If its empty, it displays a message showing the problem */
            val txtError = findViewById<TextView>(R.id.txt_error)
            txtError.textSize = 20f
            txtError.text = resources.getString(R.string.REGISTER_error_empty)
        }
        return false
    }

    private fun authUserIntoFirebase(
            name: String,
            lastname: String,
            email: String,
            pwd: String,
            homeCountry : String,
            homeCity : String,
            isOwner: Boolean
    ) {
        if(imageProfileUri != null && imageIDUri != null){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener{
                        if(!it.isSuccessful) return@addOnCompleteListener
                        Toast.makeText(applicationContext,"Creating account", Toast.LENGTH_SHORT).show()
                        Log.d("Register", "Created user with uid ${it.result.toString()}")
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pwd)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        val user = FirebaseAuth.getInstance().currentUser
                                        user?.let {
                                            val uid = user.uid
                                            saveUser(
                                                    uid,
                                                    name,
                                                    lastname,
                                                    email,
                                                    pwd,
                                                    homeCountry,
                                                    homeCity,
                                                    isOwner
                                            )
                                        }
                                    }
                                }
                    }
                    .addOnFailureListener{
                        Log.d("Login", "Problem authenticating the user ${it.message}")
                        val txtError = findViewById<TextView>(R.id.txt_error)
                        txtError.textSize = 20f
                        txtError.text = it.message
                        Toast.makeText(applicationContext,"${it.message}", Toast.LENGTH_SHORT).show()
                    }
        }else{
            val txtError = findViewById<TextView>(R.id.txt_error)
            txtError.textSize = 20f
            txtError.text = resources.getString(R.string.REGISTER_error_empty)
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