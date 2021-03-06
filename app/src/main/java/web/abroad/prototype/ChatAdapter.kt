package web.abroad.prototype

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import web.abroad.prototype.model.Message
import java.io.File

class ChatAdapter(val context: Context, val chatList : ArrayList<Message>, onAccommodationClickListener: OnAccommodationClickListener, val userUid : String) : RecyclerView.Adapter<CustomChat>(){

    var mOnAccommodationClickListener : OnAccommodationClickListener = onAccommodationClickListener

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomChat {
        val cellForRow = LayoutInflater.from(context).inflate(R.layout.chat_data, parent, false)
        return CustomChat(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomChat, position: Int) {
        holder.bind(chatList[position], position.toString(), mOnAccommodationClickListener, context, userUid)
    }
}

class CustomChat(val view : View) : RecyclerView.ViewHolder(view) , View.OnClickListener{

    var name  : TextView = view.findViewById<TextView>(R.id.txt_reciever)
    var content : TextView = view.findViewById<TextView>(R.id.txt_message)
    var img   : ImageView = view.findViewById<ImageView>(R.id.img_reciever)
    val imgRounded : ImageView = view.findViewById<ImageView>(R.id.img_rounded_profile)

    lateinit var onAccommodationClickListener : OnAccommodationClickListener

    fun bind(message: Message, position: String, onAccommodationClickListener: OnAccommodationClickListener, context: Context, userUid: String) {
        if(message.content!!.count() > 20){
            content.text = message.content!!.take(20) + "..."
        }else{
            content.text = message.content!!.take(20)
        }
        Log.d("ADAPTER", message.content)
        val database = Firebase.database
        val myRef = database.getReference("users")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (retrievedUser in dataSnapshot.children) {
                    if(retrievedUser.child("uid").value.toString() == message.recieverUid &&
                            retrievedUser.child("uid").value.toString() != userUid){
                        name.text = retrievedUser.child("name").value.toString()
                    }else if(retrievedUser.child("uid").value.toString() == message.senderUid &&
                            retrievedUser.child("uid").value.toString() != userUid){
                        name.text = retrievedUser.child("name").value.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Login", "Failed to read value.", error.toException())
            }
        })

        val storageReference : StorageReference
        if(message.recieverUid == userUid){
            storageReference = Firebase.storage.getReference("/profile_images/${message.senderUid}")
        }else{
            storageReference = Firebase.storage.getReference("/profile_images/${message.recieverUid}")
        }
        val file = File.createTempFile("img", "jpg")
        storageReference.getFile(file).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imgRounded.setImageBitmap(bitmap)
            img.alpha = 0f
        }.addOnFailureListener{
            img.setImageResource(R.drawable.img_circle_2)
        }

        this.onAccommodationClickListener = onAccommodationClickListener
        view.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        onAccommodationClickListener.onAccommodationClickListener(adapterPosition)
    }
}