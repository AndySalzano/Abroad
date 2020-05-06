package web.abroad.abroadjava

import android.content.Context
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import web.abroad.abroadjava.model.Accommodation
import web.abroad.abroadjava.model.Message
import java.io.File
import java.nio.file.LinkOption

class MessageAdapter(val context: Context, val MessageList : ArrayList<Message>, val userUid : String) : RecyclerView.Adapter<CustomMessage>(){

    override fun getItemCount(): Int {
        return MessageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomMessage {
        val cellForRow = LayoutInflater.from(context).inflate(R.layout.message_data, parent, false)
        return CustomMessage(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomMessage, position: Int) {
        holder.bind(MessageList[position], position.toString(), context, userUid)
    }
}

class CustomMessage(val view : View) : RecyclerView.ViewHolder(view){

    var info : TextView = view.findViewById<TextView>(R.id.txt_content)
    val cv : LinearLayout = view as LinearLayout

    fun bind(message: Message, position: String, context: Context, userUid: String) {
        info.text  = message.content
        if(message.senderUid == userUid){
            //NOT WORKING view.layoutDirection = View.LAYOUT_DIRECTION_RTL
            cv.layoutDirection = View.LAYOUT_DIRECTION_RTL
            info.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        }
    }
}