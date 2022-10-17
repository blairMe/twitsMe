package com.blair.twits.firebase


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import com.blair.twits.ui.activities.MainActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class PostTwit {
    val db = Firebase.firestore

    // Firebase Storage
    val storage = Firebase.storage

    // Firestore users document name
    lateinit var docName: String

    fun postTwit(twitText : String, imagePath: String, currentUser : String) {


        // Get the user name
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val usedEmail = document.data["userEmail"].toString().trim()
                    docName = document.id

                    if(usedEmail == currentUser) {
                        Log.i("Doc Name", docName)
                    }
//                    Log.i("UserEmail Used", usedEmail)
//                    Log.i("UserEmail Used", docName)
                    //gettingUsername(currentUser, usedEmail, docName)

                }
            }

        // Upload the image
        val storageRef = storage.reference

        var file = Uri.fromFile(File("$imagePath"))
        val profileImageRef = storageRef.child("twitPictures/${file.lastPathSegment}")
        var uploadTask = profileImageRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { _ ->

            // Get profile picture URL
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                profileImageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUri = task.result
                    Log.i("Your URL", "$imageUri")

                    // Send username and profile picture url to firestore
                    val twit = hashMapOf(
                        "twitText" to twitText,
                        "email" to currentUser,
                        "imageUrl" to imageUri
                    )

                    // Add a new document with a generated ID
                    db.collection("twits")
                        .add(twit)
                        .addOnSuccessListener { documentReference ->
                            Log.d("Posted Twit", "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Posted Twit", "Error adding document", e)
                        }

                } else {
                    Log.e("Failed URL", "Can't get the URL")
                }
            }
        }
    }

//    private fun gettingUsername(currentUser: String, usedEmail: String, docName: String) {
//        if(usedEmail == currentUser) {
//
//        }
//    }


}