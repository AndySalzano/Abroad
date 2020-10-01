package web.abroad.prototype

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_my_accommodations.*
import web.abroad.prototype.model.Accommodation
import web.abroad.prototype.model.User

class MyAccommodationsActivity : AppCompatActivity() , OnAccommodationClickListener {

    var AccommodationList : ArrayList<Accommodation> ?= ArrayList()
    var ref : DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_accommodations)
        attachButtons()

        recyclerview.layoutManager = LinearLayoutManager(this)

        ref = FirebaseDatabase.getInstance().getReference("accommodations")
        ref?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    for (h in p0.children){
                        if(h.child("ownerUid").value.toString() == FirebaseAuth.getInstance().uid){
                            val bal = h.getValue(Accommodation::class.java)
                            AccommodationList?.add(bal!!)
                        }
                    }

                    val adapter = AccommodationAdapter(applicationContext!!,AccommodationList!!, this@MyAccommodationsActivity)
                    recyclerview.adapter = adapter
                }
            }
        })
    }

    private fun attachButtons(){
        val uid = FirebaseAuth.getInstance().uid
        val btnUploadAccomm = findViewById<Button>(R.id.btn_upload_accomm)
        val btnCalendar = findViewById<Button>(R.id.btn_status_accomm)
        btnUploadAccomm.setOnClickListener {
            val intent = Intent(this, UploadAccommodationActivity::class.java)
            startActivity(intent)
        }

        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        var found = false
        val database = Firebase.database
        val myRef = database.getReference("users")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedUser in dataSnapshot.children) {
                    if(retrievedUser.child("uid").value.toString() == uid &&
                            retrievedUser.child("owner").value.toString() == "false"){
                        found = true
                    }
                }

                if(found){
                    Log.d("FOUND","FOUND")
                    val ly = findViewById<LinearLayout>(R.id.ly_myaccoms)
                    ly.removeView(btnUploadAccomm)
                    ly.removeView(btnCalendar)
                }
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

    override fun onAccommodationClickListener(position : Int){
        AccommodationList?.get(position)
        val intent = Intent(this, AccommodationActivity::class.java)
        intent.putExtra("ACCOMMODATION", AccommodationList?.get(position))
        startActivity(intent)
    }

}
