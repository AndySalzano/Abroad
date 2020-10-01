package web.abroad.prototype

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import web.abroad.prototype.model.Accommodation
import java.io.File

/* Hints:
        https://stackoverflow.com/questions/58219364/kotlin-displaying-data-from-firebase-database-in-recyclerview-in-fragment
        https://www.youtube.com/watch?v=69C1ljfDvl0
*/

class AccommodationAdapter(val context: Context, val AccommodationList : ArrayList<Accommodation>, onAccommodationClickListener: OnAccommodationClickListener) : RecyclerView.Adapter<CustomAccommodation>(){

    var mOnAccommodationClickListener : OnAccommodationClickListener = onAccommodationClickListener

    override fun getItemCount(): Int {
        return AccommodationList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAccommodation {
        val cellForRow = LayoutInflater.from(context).inflate(R.layout.overview_accomodation, parent, false)
        return CustomAccommodation(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomAccommodation, position: Int) {
        holder.bind(AccommodationList[position], position.toString(), mOnAccommodationClickListener, context)
    }
}

class CustomAccommodation(val view : View) : RecyclerView.ViewHolder(view) , View.OnClickListener{

    var name  : TextView  = view.findViewById<TextView>(R.id.txt_title)
    var type  : TextView  = view.findViewById<TextView>(R.id.txt_type)
    var price : TextView  = view.findViewById<TextView>(R.id.txt_monthly_price)
    var img   : ImageView = view.findViewById<ImageView>(R.id.img_accomm)

    lateinit var onAccommodationClickListener : OnAccommodationClickListener

    fun bind(accommodation: Accommodation, position: String, onAccommodationClickListener: OnAccommodationClickListener, context: Context) {
        name.text  = accommodation.name
        type.text  = accommodation.type
        price.text = context.resources.getString(R.string.ACCOMADAPTER_price, accommodation.price)

        val storageReference = Firebase.storage.getReference("/accommodations/${accommodation.ownerUid}/${accommodation.accomUid}/img_0")
        val file = File.createTempFile("img", "jpg")
        storageReference.getFile(file).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            img.setImageBitmap(bitmap)
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

interface OnAccommodationClickListener{
    fun onAccommodationClickListener(position : Int)
}