package web.abroad.prototype

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chats.*
import web.abroad.prototype.model.Message
import web.abroad.prototype.model.User
import java.util.ArrayList

class ChatsActivity : AppCompatActivity(), OnAccommodationClickListener {

    var messageList : ArrayList<Message>?= ArrayList()
    val userUid = FirebaseAuth.getInstance().uid
    var arrayUids : ArrayList<String> = ArrayList()

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
                    var interlocutorUid : String
                    if(retrievedMessage.child("senderUid").value.toString() != userUid){
                        interlocutorUid = retrievedMessage.child("senderUid").value.toString()
                    }else{
                        interlocutorUid = retrievedMessage.child("recieverUid").value.toString()
                    }
                    if((retrievedMessage.child("senderUid").value.toString() == userUid || retrievedMessage.child("recieverUid").value.toString()  == userUid)
                            && !arrayUids.contains(interlocutorUid)){
                        val bal = retrievedMessage.getValue(Message::class.java)
                        messageList?.add(bal!!)
                        Log.d("MESSGAE", bal!!.content.toString())
                        arrayUids.add(interlocutorUid)
                    }
                }
                for (data in arrayUids)
                    Log.d("auid", data.toString())
                for (data in messageList!!)
                    Log.d("messageList", data.content.toString())

                val adapter = ChatAdapter(applicationContext!!,messageList!!, this@ChatsActivity, userUid.toString())
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
        if(userUid == messageList?.get(position)!!.recieverUid){
            intent.putExtra("RECIEVERUID", messageList?.get(position)!!.senderUid)
        }else{
            intent.putExtra("RECIEVERUID", messageList?.get(position)!!.recieverUid)
        }
        startActivity(intent)
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
