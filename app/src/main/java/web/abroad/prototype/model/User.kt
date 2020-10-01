package web.abroad.prototype.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot

data class User(
        val uid : String,
        val name : String,
        val lastname : String,
        val email : String,
        val pwd : String,
        val imgProfile : String,
        val homeCountry : String,
        val homeCity : String,
        val destinationCountry : String,
        val destinationCity : String,
        val currentStudies: String,
        val isOwner: Boolean

) {

    companion object{
        fun checkIfUserIsLogged() : Boolean {
            val uid = FirebaseAuth.getInstance().uid
            lateinit var user: DataSnapshot
            Log.d("login", "$uid")
            if(uid == null){
                return false
            }
            return true
        }
    }
}