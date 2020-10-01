package web.abroad.prototype.model

class Message (
    val senderUid : String?,
    val recieverUid : String?,
    val content : String?,
    val timestamp: Long?
){
    constructor() : this(
            "","","",0
    )
}