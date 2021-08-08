package com.jibee.upwork01.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jibee.upwork01.models.Src
import com.jibee.upwork01.models.Story
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

object FirebaseService {

    var job: CompletableJob? = null

    //methods for the firebase real-time database

    suspend fun postStatus(src: Src, context: Context) {

        //check if its a media or a text
        if (src.type.equals("text")) {
            //upload directly to the database
            val userId = FirebaseAuth.getInstance().currentUser?.uid!!
            val db = Firebase.database
            val myRef = db.reference.child("story")
                .child(userId)
                .child("content")
                .push()
                .setValue(src)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Toast.makeText(context, "Story Added", Toast.LENGTH_LONG).show()
                    else
                        Log.d("Post-status", "error adding story")
                }.addOnFailureListener {
                    Log.d("Post-status", it.localizedMessage!!)
                }

        } else {
            val storageRef = FirebaseStorage.getInstance().reference

            val ref = storageRef.child(
                "/uploadedMedia/"
                        + UUID.randomUUID().toString()
            )
            val uploadTask = ref.putFile(Uri.parse(src.src))

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.d("Post-status", it.localizedMessage!!)
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Log.d("Post-status", "Downloaded URL: is ${downloadUri.toString()}")
                    var downloadUrl = downloadUri.toString() //Return URL of uploaded doc

                    //upload src ofimage to the downloadUri
                    src.src = downloadUrl

                    //now use the download uri to post the status in the real-time database
                    val userId = FirebaseAuth.getInstance().currentUser?.uid!!
                    val db = Firebase.database
                    db.reference.child("story")
                        .child(userId)
                        .child("content")
                        .push()
                        .setValue(src)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Post-status", "Added story")
                                Toast.makeText(context, "Story Added", Toast.LENGTH_LONG).show()
                            } else
                                Log.d("Post-status", "error adding story")
                        }.addOnFailureListener {
                            Log.d("Post-status", it.localizedMessage!!)
                            Toast.makeText(context, "Error Adding Story..", Toast.LENGTH_LONG).show()
                        }

                } else {
                    Log.d("Post-status", "upload failed")
                }
            }.addOnFailureListener {
                Log.d("Post-status", "upload failed")
            }
        }

    }


}