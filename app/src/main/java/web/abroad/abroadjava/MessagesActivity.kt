package web.abroad.abroadjava

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.activity_see_accommodations.*
import web.abroad.abroadjava.model.Accommodation
import web.abroad.abroadjava.model.Message
import java.io.File
import java.util.*

class MessagesActivity : AppCompatActivity() {

    lateinit var recieverUid : String
    val userUid = FirebaseAuth.getInstance().uid
    var messageList : ArrayList<Message> ?= ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        displayReciever()
        displayMessages()
        recieverUid = intent.getStringExtra("RECIEVERUID")!!;

        val btnMessage = findViewById<Button>(R.id.btn_message)
        btnMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun displayReciever(){
        val database = Firebase.database
        val myRef = database.getReference("users")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedUser in dataSnapshot.children) {
                    if(retrievedUser.child("uid").value.toString() == recieverUid){
                        val recieverName = findViewById<TextView>(R.id.txt_reciever)
                        recieverName.text = retrievedUser.child("name").value.toString() + retrievedUser.child("lastname").value.toString()
                        displayImage()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
    }

    private fun displayImage(){
        val storageReference = Firebase.storage.getReference("/profile_images/$recieverUid")
        val imageView = findViewById<ImageView>(R.id.img_reciever)
        val file = File.createTempFile("img", "jpg")
        storageReference.getFile(file).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
        }.addOnFailureListener{
            imageView.setImageResource(R.drawable.img_circle_2)
        }
    }
    private fun displayMessages(){
        val database = Firebase.database
        val myRef = database.getReference("messages")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedMessage in dataSnapshot.children) {
                    if(retrievedMessage.child("senderUid").value.toString() == userUid &&
                            retrievedMessage.child("recieverUid").value.toString() == recieverUid){
                        val bal = retrievedMessage.getValue(Message::class.java)
                        messageList?.add(bal!!)
                    }
                }
                val adapter = MessageAdapter(applicationContext!!,messageList!!, userUid.toString())
                recyclerviewMessage.layoutManager = LinearLayoutManager(applicationContext)
                recyclerviewMessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })
    }

    private fun sendMessage(){
        val content = findViewById<EditText>(R.id.txt_message).text.toString()
        val message = Message(
                userUid,
                recieverUid,
                content
        )

        val msgUid = UUID.randomUUID().toString()
        val clientDatabase = FirebaseDatabase.getInstance().getReference("/messages/$msgUid")
        clientDatabase.setValue(message)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext,"message sent", Toast.LENGTH_SHORT).show()
                    finish();
                    startActivity(intent);
                }
                .addOnFailureListener{
                    Log.d("UploadAccom", "Accommodation not saved")
                }
    }
}
