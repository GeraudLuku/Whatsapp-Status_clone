package com.jibee.upwork01.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.models.Stories.UserStory
import kotlinx.coroutines.flow.Flow

@Dao
interface StoriesDao {

    //get current user stories
    @Query("SELECT * FROM user_stories")
    fun getAllUserStories(): Flow<UserStory>

    //get all friends stories
    @Query("SELECT * FROM friends_stories")
    fun getAllFriendsStories(): Flow<List<Stories>>

    //insert users added stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStories(userStory: UserStory)

    //insert list of friends stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendStories(stories: List<Stories>)

    //delete all user existing stories
    @Query("DELETE FROM user_stories")
    suspend fun deleteAllUserStories()

    //delete all friends existing stories
    @Query("DELETE FROM friends_stories")
    suspend fun deleteAllFriendsStories()

}