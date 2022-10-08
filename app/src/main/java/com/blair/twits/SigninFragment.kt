package com.blair.twits

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.blair.twits.databinding.FragmentSigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding

    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {

            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)

//            view?.let {
//                Navigation.findNavController(it)
//                    .navigate(R.id.action_signinFragment_to_infoSettingFragment)
//            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButton.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_signinFragment_to_signupFragment)
        }

        userDetailsFocusedListener()
        binding.btnSignin.setOnClickListener {
            // Checking that the user details are valid
            val validEmail = binding.emailSignin.text.toString().trim()
            val validPassword = binding.passwordSignin.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(validEmail).matches()) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid email.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else if (validPassword.length < 8 && !validPassword.matches(".*[A-Z].*".toRegex())
                && !validPassword.matches(".*[a-z].*".toRegex())
            ) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid password.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else {
                signinUser()
                val dialog = Dialog(requireActivity())
                dialog.setContentView(R.layout.loader_dialog)
                dialog.show()
            }
        }
    }

    // Singing in the user with firebase
    private fun signinUser() {
        val email = binding.emailSignin.text.toString()
        val password = binding.passwordSignin.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Signin success
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Signin fails
                    Toast.makeText(requireActivity(), "Couldn't signin",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Validation
    private fun userDetailsFocusedListener() {
        binding.emailSignin.setOnFocusChangeListener { _, _ ->
            val emailText = binding.emailSignin.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                binding.theEmailContainer.helperText = "Enter a valid email address"
            } else {
                binding.theEmailContainer.helperText = null
            }
        }
        binding.passwordSignin.setOnFocusChangeListener { _, _ ->
            val password = binding.passwordSignin.text.toString()
            if (password.length < 8) {
                binding.thePasswordContainer.helperText = "Password must be at least 8 characters"
            } else if (!password.matches(".*[A-Z].*".toRegex())) {
                binding.thePasswordContainer.helperText =
                    "Password must contain at least one uppercase letter"
            } else if (!password.matches(".*[a-z].*".toRegex())) {
                binding.thePasswordContainer.helperText =
                    "Password must contain at least one lowercase letter"
            } else {
                binding.thePasswordContainer.helperText = null
            }
//            else if(!password.matches(".*[@*/$].*".toRegex())) {
//                binding.thePasswordContainer.helperText = "Password must contain at least on symbol"
//            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //binding = null
    }

}