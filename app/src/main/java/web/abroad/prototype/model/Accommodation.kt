package web.abroad.prototype.model

import java.io.Serializable

data class Accommodation(
        val ownerUid : String?,
        val accomUid : String?,
        val name : String?,
        val type : String?,
        val city : String?,
        val address : String?,
        val price : String?,
        val rentalPeriod : String?,
        val description : String?,
		
		// Maybe an array should be introduced
        val service1 : Boolean?,
        val service2 : Boolean?,
        val service3 : Boolean?,
        val service4 : Boolean?,
        val service5 : Boolean?,
        val service6 : Boolean?,
        val service7 : Boolean?
) : Serializable {
    constructor() : this("", "", "",
            "", "", "", "",
            "", "", false, false,
            false, false, false, false, false
    )
}