package com.jibee.upwork01.models.Stories

import java.io.Serializable

data class QueryStory(
    var userID: Int = 11,
    var pageNumber: Int = 0,
    var currentUserID: Int = 11
):Serializable