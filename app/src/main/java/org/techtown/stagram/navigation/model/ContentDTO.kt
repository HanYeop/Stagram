package org.techtown.stagram.navigation.model

data class ContentDTO (
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    var favorites : MutableMap<String, Boolean> = HashMap()){

    // 버그 관리
    data class Comment(
        var uid : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null)
}