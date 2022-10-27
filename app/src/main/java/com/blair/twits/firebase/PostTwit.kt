package com.blair.twits.firebase


import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.blair.twits.R
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

    // Getting username from the firestore user details
    lateinit var storedUsername: String
    lateinit var storedFirstName : String
    lateinit var storedSecondName : String

    // Loading dialog
    private var progressDialog : Dialog? = null

    fun postTwit(
        twitText: String,
        imagePath: String,
        currentUser: String,
        view: View,
        requireActivity: FragmentActivity
    ) {

        // Display Please Wait dialog to post the twit
        progressDialog = Dialog(requireActivity)
        progressDialog?.let {
            it.setContentView(R.layout.loader_dialog)
            it.show()
        }

        // Get the user name
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val usedEmail = document.data["userEmail"].toString().trim()
                    docName = document.id

                    if (usedEmail == currentUser) {
                        storedUsername = document.data["setUserName"].toString().trim()
                        storedFirstName = document.data["firstName"].toString().trim()
                        storedSecondName = document.data["lastName"].toString().trim()
                    }
                }
            }

        // Upload the image
        val storageRef = storage.reference

        val file = Uri.fromFile(File("$imagePath"))
        val profileImageRef = storageRef.child("twitPictures/${file.lastPathSegment}")
        val uploadTask = profileImageRef.putFile(file)

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

                    // Send username and profile picture url to firestore
                    val twit = hashMapOf(
                        "twitText" to twitText,
                        "email" to currentUser,
                        "imageUrl" to imageUri,
                        "userName" to storedUsername,
                        "firstUserName" to storedFirstName,
                        "secondUserName" to storedSecondName
                    )

                    // Add a new document with a generated ID
                    db.collection("twits")
                        .add(twit)
                        .addOnSuccessListener { _ ->

                            // Dismiss dialog once twit has been successfully posted
                            progressDialog?.let {
                                it.dismiss()
                            }

                            // Move to Home Fragment
                            Navigation.findNavController(view)
                                .navigate(R.id.action_postTwitFragment_to_homeFragment)
                        }
                } else {
                    Log.e("Failed URL", "Can't get the URL")
                }
            }
        }

    }

    fun postNonImageTweet(twitText: String, currentUser: String, view: View, requireActivity: FragmentActivity) {

        // Display Please Wait dialog to post the twit
        progressDialog = Dialog(requireActivity)
        progressDialog?.let {
            it.setContentView(R.layout.loader_dialog)
            it.show()
        }

        // Get the user name
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val usedEmail = document.data["userEmail"].toString().trim()

                    docName = document.id

                    if (usedEmail == currentUser) {
                        storedUsername = document.data["setUserName"].toString().trim()
                        storedFirstName = document.data["firstName"].toString().trim()
                        storedSecondName = document.data["lastName"].toString().trim()

                        // Send username and profile picture url to firestore
                        val twit = hashMapOf(
                            "twitText" to twitText,
                            "email" to currentUser,
                            "imageUrl" to "",
                            "userName" to storedUsername,
                            "firstUserName" to storedFirstName,
                            "secondUserName" to storedSecondName
                        )

                        // Add a new document with a generated ID
                        db.collection("twits")
                            .add(twit)
                            .addOnSuccessListener { _ ->

                                // Dismiss dialog once twit has been successfully posted
                                progressDialog?.let {
                                    it.dismiss()
                                }

                                // Move to Home Fragment
                                Navigation.findNavController(view)
                                    .navigate(R.id.action_postTwitFragment_to_homeFragment)
                        }
                    }
                }
            }
    }
}