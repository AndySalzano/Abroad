package web.abroad.abroadjava.model

class Message (
    val senderUid : String?,
    val recieverUid : String?,
    val content : String?
){
    constructor() : this(
            "","",""
    )
}