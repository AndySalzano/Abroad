package web.abroad.abroadjava

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_see_accommodations.*
import web.abroad.abroadjava.model.Accommodation
import web.abroad.abroadjava.model.User
import java.io.Serializable

class SeeAccommodationsActivity : AppCompatActivity() , OnAccommodationClickListener, Serializable {

    var AccommodationList : ArrayList<Accommodation> ?= ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_accommodations)

        val city = findViewById<TextView>(R.id.txt_city)
        city.text = intent.getStringExtra("city")

        val from = findViewById<TextView>(R.id.txt_from)
        from.text = intent.getStringExtra("from")

        val to = findViewById<TextView>(R.id.txt_to)
        to.text = intent.getStringExtra("to")

        searchAccommodations(city.text.toString())
    }

    private fun searchAccommodations(city: String){
            Toast.makeText(applicationContext, "Looking for accommodations", Toast.LENGTH_SHORT).show()
            val uid = FirebaseAuth.getInstance().uid
            lateinit var accommodation: DataSnapshot
            Log.d("login", "$uid")
            //if(uid != null){
                val database = Firebase.database
                val myRef = database.getReference("accommodations")
                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (retrievedAccomm in dataSnapshot.children) {
                            if(retrievedAccomm.child("ownerUid").value.toString() != uid &&
                                    retrievedAccomm.child("city").value.toString().equals(city, ignoreCase = true)){
                                Toast.makeText(applicationContext, "Displaying accommodations", Toast.LENGTH_SHORT).show()
                                val bal = retrievedAccomm.getValue(Accommodation::class.java)
                                AccommodationList?.add(bal!!)
                            }
                        }
                        val adapter = AccommodationAdapter(applicationContext!!,AccommodationList!!, this@SeeAccommodationsActivity)
                        recyclerviewUser.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerviewUser.adapter = adapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("Login", "Failed to read value.", error.toException())
                    }
                })
            //}
    }

    override fun onAccommodationClickListener(position : Int){
        AccommodationList?.get(position)
        val intent = Intent(this, AccommodationActivity::class.java)
        intent.putExtra("ACCOMMODATION", AccommodationList?.get(position))
        startActivity(intent)
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
                /*if(logged){
                    Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                    startActivity(intent);
                    return true;
                }else{*/
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                return true
            }
        }//}
        return super.onOptionsItemSelected(item)
    }
}
