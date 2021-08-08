package com.jibee.upwork01.models

import java.io.Serializable

data class Story(
    var content: List<Src> = ArrayList()
) : Serializable
