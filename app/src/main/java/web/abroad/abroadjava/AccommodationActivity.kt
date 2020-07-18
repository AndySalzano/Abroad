package web.abroad.abroadjava

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_accommodation.*
import web.abroad.abroadjava.model.Accommodation
import web.abroad.abroadjava.model.User
import java.io.File
import java.io.IOException

class AccommodationActivity : AppCompatActivity() {

    private lateinit var accommodation : Accommodation
    private lateinit var images        : ArrayList<Bitmap>
    private lateinit var mapFragment   : SupportMapFragment
    private lateinit var googleMap     : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accommodation)

        images = ArrayList()
        accommodation = intent.getSerializableExtra("ACCOMMODATION") as Accommodation
        getOwner()
        isMyAccommodation()
        displaySlider()
        fillInformation()

        val geopoint = getLocationFromAddress(accommodation.address!!)
        if(geopoint == null){
            getLocationFromAddress(accommodation.city!!)
            txt_map_title.text = getString(R.string.ACCOMMODATION_map_title, accommodation.city)
        }else{
            txt_map_title.text = getString(R.string.ACCOMMODATION_map_title, accommodation.address)
        }
        val lat = geopoint?.latitude
        val lng = geopoint?.longitude

        if(lat != null){
            mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(OnMapReadyCallback {
                googleMap = it
                val location = com.google.android.gms.maps.model.LatLng(lat!!,lng!!)
                googleMap.addMarker(MarkerOptions().position(location).title("Location"))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            })
        }else{
            txt_map_title.text = getString(R.string.ACCOMMODATION_map_title_error)
        }

        attachButtons()
    }

    private fun fillInformation(){
        txt_title.text         = accommodation.name
        txt_address.text       = accommodation.address
        txt_type.text          = accommodation.type
        txt_price.text         = getString(R.string.ACCOMADAPTER_price, accommodation.price)
        txt_rental_period.text = getString(R.string.ACCOMMODATION_rental_period, accommodation.rentalPeriod)
        txt_description.text   = accommodation.description

        val services = resources.getStringArray(R.array.ACCOMMODATION_services)
        if(accommodation.service1 as Boolean){
            txt_service_1.text = services[0] + getString(R.string.yes)
        }else{
            txt_service_1.text = services[0] + getString(R.string.no)
        }

        if(accommodation.service2 as Boolean){
            txt_service_2.text = services[1] + getString(R.string.yes)
        }else{
            txt_service_2.text = services[1] + getString(R.string.no)
        }

        if(accommodation.service3 as Boolean){
            txt_service_3.text = services[2] + getString(R.string.yes)
        }else{
            txt_service_3.text = services[2] + getString(R.string.no)
        }

        if(accommodation.service4 as Boolean){
            txt_service_4.text = services[3] + getString(R.string.yes)
        }else{
            txt_service_4.text = services[3] + getString(R.string.no)
        }

        if(accommodation.service5 as Boolean){
            txt_service_5.text = services[4] + getString(R.string.yes)
        }else{
            txt_service_5.text = services[4] + getString(R.string.no)
        }

        if(accommodation.service6 as Boolean){
            txt_service_6.text = services[5] + getString(R.string.yes)
        }else{
            txt_service_6.text = services[5] + getString(R.string.no)
        }

        if(accommodation.service7 as Boolean){
            txt_service_7.text = services[6] + getString(R.string.yes)
        }else{
            txt_service_7.text = services[6] + getString(R.string.no)
        }
    }

    private fun getOwner(){
        val database = Firebase.database
        val myRef = database.getReference("users")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedUser in dataSnapshot.children) {
                    if(retrievedUser.key == accommodation.ownerUid){
                        txt_owner.text = retrievedUser.child("name").value.toString()
                        txt_owner.text = getString(
                                R.string.ACCOMMODATION_owner,
                                retrievedUser.child("name").value.toString(),
                                retrievedUser.child("lastname").value.toString()
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
    }

    private fun isMyAccommodation(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == accommodation.ownerUid){
            btn_contact_edit.text = getString(R.string.ACCOMMODATION_edit)
        }else{
            btn_contact_edit.text = getString(R.string.ACCOMMODATION_contact)
        }
    }

    private fun attachButtons(){
        val btnDocument = findViewById<Button>(R.id.btn_see_document)
        btnDocument.setOnClickListener {
            val storageReference = Firebase.storage.getReference("/accommodations/${accommodation.ownerUid}/${accommodation.accomUid}/${accommodation.ownerUid}")
            displayDialog(storageReference)
        }

        val btnMessage = findViewById<Button>(R.id.btn_contact_edit)
        btnMessage.setOnClickListener {
            if(FirebaseAuth.getInstance().uid != null){
                val intent = Intent(applicationContext, MessagesActivity::class.java)
                intent.putExtra("RECIEVERUID", accommodation.ownerUid)
                startActivity(intent)
            }else{
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * Displays the dialog with the Document of Property
     */
    private fun displayDialog(ref : StorageReference){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_dialog)

        val img = dialog.findViewById<ImageView>(R.id.img_document)
        Log.d("IMG", img.toString())
        Log.d("FILE",ref.toString())
        val file = File.createTempFile("img", "jpg")
        ref.getFile(file).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            img.setImageBitmap(bitmap)
        }.addOnFailureListener{
            img.setImageResource(R.drawable.img_circle_2)
        }

        dialog.show()
    }

    private fun displaySlider(){
        val refImages = Firebase.storage.getReference(
                "/accommodations/${accommodation.ownerUid}/${accommodation.accomUid}"
        )

        refImages.listAll().addOnSuccessListener {
            it.items.forEachIndexed() { i : Int, item : StorageReference ->
                /* Add to the list if  the name of the img is not the Owner UID (Document of Property) */
                if(item.name != accommodation.ownerUid){
                    val file = File.createTempFile("img", "jpg")
                    item.getFile(file).addOnSuccessListener { snapshot ->
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        images.add(bitmap)
                        if(i == it.items.size - 1){
                            /* Adapter */
                            val viewPager = findViewById<ViewPager>(R.id.img_slider)
                            val adapter = ImageSliderAdapter(this, images)
                            viewPager.adapter = adapter
                        }
                    }
                }
            }
        }.addOnFailureListener {
            //TODO
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

    private fun getLocationFromAddress(strAddress : String) : GeoPoint? {

        val coder   = Geocoder(this)
        val address : List<Address>
        var p1      : GeoPoint?

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address == null) {
                return null;
            }
            val location = address[0]
            location.longitude

            p1 = GeoPoint(
                    (location.latitude),
                    (location.longitude)
            )

            return p1;
        }catch (e : IOException){
            Log.d("EXCEPTION", e.toString())
        }

        return null
    }
}



