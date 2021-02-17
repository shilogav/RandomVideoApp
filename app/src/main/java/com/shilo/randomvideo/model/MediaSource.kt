package com.shilo.randomvideo.model

/*class MediaSource constructor(title : String
                              , mediaURL : String, thumbnail : String
                              , description : String) {
    private lateinit var title : String
    private lateinit var mediaURL : String
    private lateinit var thumbnail : String
    private lateinit var description : String


}*/

class MediaSource {
    var title: String? = null
    var media_url: String? = null
    var thumbnail: String? = null
    var description: String? = null

    constructor(title: String?, media_url: String?, thumbnail: String?, description: String?) {
        this.title = title
        this.media_url = media_url
        this.thumbnail = thumbnail
        this.description = description
    }

    constructor() {}

}