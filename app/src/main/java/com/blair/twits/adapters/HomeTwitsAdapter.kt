package com.blair.twits.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.blair.twits.databinding.TwitItemLayoutBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeTwitsAdapter(private val fragment : Fragment, private val twitsArray : ArrayList<String>) :
    RecyclerView.Adapter<HomeTwitsAdapter.ViewHolder>()
{

    // Firestore db
    val db = Firebase.firestore


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : TwitItemLayoutBinding = TwitItemLayoutBinding.inflate(
                        LayoutInflater.from(fragment.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val twitItems = twitsArray[position]

        //Log.i("Array Items", "$twitItems")
        val docRef = db.collection("twits").document(twitItems)

        // Get the document, forcing the SDK to use the offline cache
        docRef.get()
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Document found in the offline cache
                val document = task.result
                val userName = document["userName"]
                val uniqueUsername = document["userName"]

                Log.d("your data", "Cached document data: ${document?.data}")
                Log.d("your data", "$userName")

                holder.username.text = userName.toString()
                holder.uniqueUsername.text = uniqueUsername.toString()
            } else {
                Log.d("your data", "Cached get failed: ", task.exception)
            }
        }

        // holder.username.text = twitItems

    }

    override fun getItemCount(): Int {
        return twitsArray.size
    }

    class ViewHolder(view : TwitItemLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Defining the views
        val username = view.userName
        val uniqueUsername = view.userUsernames
    }


}