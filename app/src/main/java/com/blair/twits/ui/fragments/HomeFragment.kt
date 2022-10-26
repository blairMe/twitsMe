package com.blair.twits.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.blair.twits.R
import com.blair.twits.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    // Firestore db
    val db = Firebase.firestore

    private lateinit var binding : FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNewTwit.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_postTwitFragment)
        }

        getTwits()
    }

    fun getTwits() : ArrayList<String> {
        val twitNamesArray =  ArrayList<String>()


        db.collection("twits")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Log.d("Twit data", "${document.id} => ${document.data}")
                    // Log.i("Twit data", "${document.data}")
                    for(fileName in document.id) {
                        twitNamesArray.add(fileName.toString())
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Twit data", "Error getting documents: ", exception)
            }

        return twitNamesArray

    }



}