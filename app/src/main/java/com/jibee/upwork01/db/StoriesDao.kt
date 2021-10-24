package com.jibee.upwork01.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.Stories.UserStory
import kotlinx.coroutines.flow.Flow

@Dao
interface StoriesDao {

    //get current user stories
    @Query("SELECT * FROM user_stories")
    fun getAllUserStories() : Flow<UserStory>

    //get all friends stories
    @Query("SELECT * FROM friends_stories")
    fun getAllFriendsStories() : Flow<Stories_All>

    //insert list of current users added stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStories(userStory: UserStory)

    //insert list of current users friends stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendStories(storiesAll: Stories_All)

    //delete all current user existing stories
    @Query("DELETE FROM user_stories")
    suspend fun deleteAllUserStories()

    //delete all current user friends existing stories
    @Query("DELETE FROM friends_stories")
    suspend fun deleteAllFriendsStories()

}