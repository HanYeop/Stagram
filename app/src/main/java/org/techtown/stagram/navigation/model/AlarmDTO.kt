package org.techtown.stagram.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    var kind : Int? = null,
    /* kind
    0 : like
    1 : comment
    2 : follow
     */
    var message : String? = null,
    var timestamp : Long? = null
)