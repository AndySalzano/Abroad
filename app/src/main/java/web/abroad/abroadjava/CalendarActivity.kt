package web.abroad.abroadjava

		// Not quetly the data change should be observed, but you should query as you need the information.
		// Notification could be important  if you create a backgroud service that maintains the local adapters.


import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class CalendarActivity : AppCompatActivity() {

    private var uid : String? = null
    private var numOfAccomm : Long? = null
    private var namesAccomm = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        uid = getUserUID()
        getUserNumOfAccomm()
        getDataFromAccom()
        val names : Array<String> = arrayOf("hey","hola","whynot")
    }

    private fun displayNamesOfAccomm(names: ArrayList<String>){
        val spinner = findViewById<Spinner>(R.id.spinner_accomm)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, names)
            spinner.adapter = adapter
        }
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

    fun getDataFromAccom(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database
        val myRef = database.getReference("accommodations")
        myRef.addValueEventListener(object : ValueEventListener {
		
		
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedAccomm in dataSnapshot.children) {
                    if (retrievedAccomm.child("uid").value.toString() == uid.toString()) {
                        retrievedAccomm.child("name").value.toString()
                        namesAccomm.add(retrievedAccomm.child("name").value.toString())
                    }
                }
                displayNamesOfAccomm(namesAccomm)
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
    }
}
