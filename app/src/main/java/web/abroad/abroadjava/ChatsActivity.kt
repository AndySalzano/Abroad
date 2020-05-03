package web.abroad.abroadjava

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chats.*
import kotlinx.android.synthetic.main.activity_messages.*
import web.abroad.abroadjava.model.Message
import java.util.ArrayList

class ChatsActivity : AppCompatActivity(), OnAccommodationClickListener {

    var messageList : ArrayList<Message>?= ArrayList()
    val userUid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        displayChats()
    }

    private fun displayChats(){
        val database = Firebase.database
        val myRef = database.getReference("messages")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedMessage in dataSnapshot.children) {
                    if(retrievedMessage.child("senderUid").value.toString() == userUid ||
                            retrievedMessage.child("recieverUid").value.toString()  == userUid){
                        val bal = retrievedMessage.getValue(Message::class.java)
                        messageList?.add(bal!!)
                    }
                }
                val adapter = ChatAdapter(applicationContext!!,messageList!!, this@ChatsActivity)
                recyclerviewChats.layoutManager = LinearLayoutManager(applicationContext)
                recyclerviewChats.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onAccommodationClickListener(position : Int){
        //Toast.makeText(applicationContext,"message sent", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MessagesActivity::class.java)
        intent.putExtra("RECIEVERUID", messageList?.get(position)!!.recieverUid)
        startActivity(intent)
    }

}
