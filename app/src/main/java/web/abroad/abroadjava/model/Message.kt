package web.abroad.abroadjava.model

import java.sql.Timestamp

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