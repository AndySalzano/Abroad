package web.abroad.prototype

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import web.abroad.prototype.model.User
import java.util.*
import android.widget.Spinner
import android.view.MenuItem
import com.google.firebase.database.FirebaseDatabase
import web.abroad.prototype.model.Accommodation


class UploadAccommodationActivity : AppCompatActivity() {

    private var imageProfileUri : Uri? = null
    private var uid : String? = null
    private var numOfAccomm : Long? = null
    private var accomUid : String = UUID.randomUUID().toString()

    companion object {
        private const val IMAGES_PICK_CODE = 1000
        private const val DOCUMENT_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_accommodation)
        uid = getUserUID()
        getUserNumOfAccomm()

        /* Set the different options for the spinner */
        val types = resources.getStringArray(R.array.UPACCOMM_types)
        val spinner = findViewById<Spinner>(R.id.spinner_types)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, types)
            spinner.adapter = adapter
        }

        val btnUploadPhotos = findViewById<Button>(R.id.btn_add_photo)
        btnUploadPhotos.setOnClickListener {
            launchGallery(IMAGES_PICK_CODE)
        }

        val btnUploadDocument = findViewById<Button>(R.id.btn_add_document)
        btnUploadDocument.setOnClickListener {
            launchGallery(DOCUMENT_PICK_CODE)
        }

        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            saveAccommodation()
        }
    }

    private fun launchGallery(code : Int){
        if(code == IMAGES_PICK_CODE){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), code)
        }else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, code)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGES_PICK_CODE){
            imageProfileUri = data?.data
            val numImg = data?.clipData?.itemCount
            Log.d("NUM IMG", numImg.toString())
            for (i in 0 until numImg!!) {
                uploadImg(data?.clipData?.getItemAt(i)?.uri, i.toString())
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode == DOCUMENT_PICK_CODE){
            uploadDocument(data?.data)
        }
    }

    private fun uploadImg(img: Uri?, position : String){
        var clientStorage = FirebaseStorage.getInstance().getReference("accommodations/$uid/$accomUid/img_$position")
        clientStorage.putFile(img!!).addOnSuccessListener {
            Toast.makeText(applicationContext, "Images uploaded successfully", Toast.LENGTH_SHORT).show()
            val imgLayout = findViewById<ImageView>(R.id.img_accomm)
            imgLayout.setImageURI(img)
        }
    }

    /**
     * Uploads the Register of Property into the folder
     */
    private fun uploadDocument(img: Uri?){
        var clientStorage = FirebaseStorage.getInstance().getReference("accommodations/$uid/$accomUid/$uid")
        clientStorage.putFile(img!!).addOnSuccessListener {
            Toast.makeText(applicationContext, "Register of Property uploaded successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAccommodation() {
        val name = findViewById<EditText>(R.id.txt_name).text.toString()
        val city = findViewById<EditText>(R.id.txt_city).text.toString()
        val address = findViewById<EditText>(R.id.txt_address).text.toString()
        val price = findViewById<EditText>(R.id.txt_price).text.toString()
        val rentalPeriod = findViewById<EditText>(R.id.txt_rental_period).text.toString()
        val description = findViewById<EditText>(R.id.txt_description).text.toString()
        val spinnerType = findViewById<Spinner>(R.id.spinner_types)
        val type = spinnerType.selectedItem.toString()

        val service1 : Boolean
        val service2 : Boolean
        val service3 : Boolean
        val service4 : Boolean
        val service5 : Boolean
        val service6 : Boolean
        val service7 : Boolean

        val swService1 = findViewById<Switch>(R.id.switch_service_1)
        service1 = swService1.isChecked

        val swService2 = findViewById<Switch>(R.id.switch_service_2)
        service2 = swService2.isChecked

        val swService3 = findViewById<Switch>(R.id.switch_service_3)
        service3 = swService3.isChecked

        val swService4 = findViewById<Switch>(R.id.switch_service_4)
        service4 = swService4.isChecked

        val swService5 = findViewById<Switch>(R.id.switch_service_5)
        service5 = swService5.isChecked

        val swService6 = findViewById<Switch>(R.id.switch_service_6)
        service6 = swService6.isChecked

        val swService7 = findViewById<Switch>(R.id.switch_service_7)
        service7 = swService7.isChecked

        val accommodation = Accommodation(
                uid,
                accomUid,
                name,
                type,
                city,
                address,
                price,
                rentalPeriod,
                description,
                service1,
                service2,
                service3,
                service4,
                service5,
                service6,
                service7
        )

        val clientDatabase = FirebaseDatabase.getInstance().getReference("/accommodations/$accomUid")
        clientDatabase.setValue(accommodation)
                .addOnSuccessListener {
                    uploadNumOfAccoms()
                    Log.d("UploadAccom", "Successfully accommodation saved")
                    Toast.makeText(applicationContext,"Accommodation uploaded", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MyAccommodationsActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("UploadAccom", "Accommodation not saved")
                }
    }

    private fun uploadNumOfAccoms(){
        val database = Firebase.database
        val myRef = database.getReference("users/$uid/numOfAccoms")
        myRef.setValue(numOfAccomm!! + 1)
    }

    private fun getUserUID() : String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun getUserNumOfAccomm(){
        val database = Firebase.database
        val myRef = database.getReference("users/$uid")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numOfAccomm = dataSnapshot.child("numOfAccoms").value as Long?
                if(numOfAccomm == null){
                    numOfAccomm = 0
                }
                //Toast.makeText(applicationContext, numOfAccomm, Toast.LENGTH_SHORT).show()
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
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
