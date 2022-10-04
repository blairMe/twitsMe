package com.example.twits

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.blair.twits.R
import com.blair.twits.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    private var auth: FirebaseAuth = Firebase.auth

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toSignin.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_signupFragment_to_signinFragment)
        }

        signupUser()



    }

    private fun signupUser() {

        binding.btnSignup.setOnClickListener {
            val firstName = binding.userFirstName.text.toString()
            val secondName = binding.userSecondName.text.toString()
            val email = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Successful signup
                        Toast.makeText(
                            requireActivity(), "Successful signing in",
                            Toast.LENGTH_SHORT
                        ).show()
                        sendDataToFireStore(firstName, secondName, email, password)
                        view?.let { it1 ->
                            Navigation.findNavController(it1)
                                .navigate(R.id.action_signupFragment_to_infoSettingFragment)
                        }
//                        val intent = Intent(activity, MainActivity::class.java)
//                        startActivity(intent)
                    } else {
                        // Unsuccessful signup fails
                        Toast.makeText(
                            requireActivity(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun sendDataToFireStore(firstName : String, secondName : String, email : String, password : String) {
        // Capture the user information
        val userInfo = hashMapOf(
            "first" to firstName,
            "last" to secondName,
            "born" to email,
            "password" to password
        )

// Add a new document with a generated ID
        db.collection("users")
            .add(userInfo)
            .addOnSuccessListener { _ ->
                Toast.makeText(requireActivity(), "Success adding to firestore",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(requireActivity(), "Problem adding to firestore",
                    Toast.LENGTH_SHORT).show()
            }


    }
}