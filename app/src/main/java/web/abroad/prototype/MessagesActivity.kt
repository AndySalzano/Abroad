package web.abroad.prototype

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import web.abroad.prototype.model.Message
import web.abroad.prototype.model.User
import java.io.File
import java.util.*

class MessagesActivity : AppCompatActivity() {

    lateinit var recieverUid : String
    val userUid = FirebaseAuth.getInstance().uid
    var messageList : ArrayList<Message> ?= ArrayList()
    var refresh = false

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
                        recieverName.text = retrievedUser.child("name").value.toString() + " " + retrievedUser.child("lastname").value.toString()
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
        val imgRounded = findViewById<ImageView>(R.id.img_rounded_profile)
        val file = File.createTempFile("img", "jpg")
        storageReference.getFile(file).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imgRounded.setImageBitmap(bitmap)
            imageView.alpha = 0f
        }.addOnFailureListener{
            imageView.setImageResource(R.drawable.img_circle_2)
        }
    }

    private fun displayMessages(){
        val database = Firebase.database
        val myRef = database.getReference("messages").orderByChild("timestamp")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedMessage in dataSnapshot.children) {
                    if((retrievedMessage.child("senderUid").value.toString() == userUid &&
                            retrievedMessage.child("recieverUid").value.toString() == recieverUid) ||
                            (retrievedMessage.child("senderUid").value.toString() == recieverUid &&
                            retrievedMessage.child("recieverUid").value.toString() == userUid)){
                        val bal = retrievedMessage.getValue(Message::class.java)
                        messageList?.add(bal!!)
                    }
                }

                if(!refresh){
                    refresh = true
                    val adapter = MessageAdapter(applicationContext!!,messageList!!, userUid.toString())
                    recyclerviewMessage.layoutManager = LinearLayoutManager(applicationContext)
                    recyclerviewMessage.adapter = adapter
                }else{
                    finish();
                    startActivity(intent);
                }
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
                content,
                (System.currentTimeMillis() / 1000)
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
