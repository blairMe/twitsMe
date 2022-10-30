package com.blair.twits.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.blair.twits.R
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

        Log.i("The Items", "$twitItems")

        // Get the document
        docRef.get()
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Document found in the offline cache
                val document = task.result
                val firstUserName = document["firstUserName"]
                val secondUserName = document["secondUserName"]
                val uniqueUsername = document["userName"]
                val twitText = document["twitText"]
                val twitImageLink = document["imageUrl"]
                val userProfilePicture = document["storedProfilePicture"]

                holder.username.text = "$firstUserName $secondUserName"
                holder.uniqueUsername.text = "@$uniqueUsername"
                holder.twitText.text = twitText.toString()

                holder.userProfileImage.load(userProfilePicture) {
                    crossfade(true)
                    crossfade(200)
                    scale(Scale.FILL)
                    transformations(CircleCropTransformation())
                }

                if(twitImageLink.toString().isNotEmpty()) {
                    holder.twitImage.visibility = View.VISIBLE
                    holder.twitImage.load(twitImageLink) {
                        crossfade(true)
                        crossfade(200)
                        scale(Scale.FILL)
                        transformations(RoundedCornersTransformation(30f))
                    }
                }
            } else {
                Log.d("your data", "Cached get failed: ", task.exception)
            }
        }


        var trueFalse = false
        holder.btnLike.setOnClickListener {

            //trueFalse = true
            if (!trueFalse) {
                holder.btnLike.setImageDrawable(ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.like_selected_icon))
                trueFalse = true
            } else if (trueFalse) {
                holder.btnLike.setImageDrawable(ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.like_unselected_icon))
                trueFalse = false
            }
        }
    }

    override fun getItemCount(): Int {
        return twitsArray.size
    }

    class ViewHolder(view : TwitItemLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Defining the views
        val username = view.userName
        val uniqueUsername = view.userUsernames
        val twitText = view.userTwitText
        val twitImage = view.twitImg
        val userProfileImage = view.userProfileImg
        val btnLikeContainer = view.twitLikeContainer
        val btnLike = view.twitLike
    }



}