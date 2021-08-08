package com.jibee.upwork01.models

import java.io.Serializable

data class Src(
    var uid: String = "",
    var type:String = "",
    var description: String = "",
    var time:String = "",
    var src: String = ""
) : Serializable
