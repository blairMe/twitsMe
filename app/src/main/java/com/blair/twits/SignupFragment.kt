package com.example.twits

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.ThemedSpinnerAdapter.Helper
import androidx.navigation.Navigation
import com.blair.twits.R
import com.blair.twits.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern.matches

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    private var auth: FirebaseAuth = Firebase.auth

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
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

        userDetailsFocusedListener()

        binding.btnSignup.setOnClickListener {
            // Checking that the user details are valid
            val firstNameText = binding.userFirstName.text.toString()
            val secondNameText = binding.userSecondName.text.toString()
            val emailText = binding.userEmail.text.toString()
            val passwordText = binding.userPassword.text.toString()
            val passwordConfirmText = binding.userPasswordConfirm.text.toString()


            if (!firstNameText.matches(".*[a-z].*".toRegex())) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid first name.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else if (!secondNameText.matches(".*[a-z].*".toRegex())) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid second name.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid email address.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else if (passwordText.length < 8 && !passwordText.matches(".*[A-Z].*".toRegex()) &&
                !passwordText.matches(".*[a-z].*".toRegex())
            ) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid password.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else if (passwordConfirmText != binding.userPassword.text.toString()
                && passwordConfirmText.length < 8
                && !passwordConfirmText.matches(".*[A-Z].*".toRegex())
                && !passwordConfirmText.matches(".*[a-z].*".toRegex())
            ) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Form")
                    .setMessage("Please enter a valid confirmation password.")
                    .setPositiveButton("Okay") { _, _ ->
                        // Nothing
                    }
                    .show()
            } else {
                signupUser()
            }
        }

    }

    private fun signupUser() {
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


    private fun sendDataToFireStore(
        firstName: String,
        secondName: String,
        email: String,
        password: String,
    ) {
        // Capture the user information
        val userInfo = hashMapOf(
            "first" to firstName,
            "last" to secondName,
            "born" to email,
            "password" to password
        )

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

    // Validation
    private fun userDetailsFocusedListener() {
        binding.userFirstName.setOnFocusChangeListener { _, _ ->
            val firstNameText = binding.userFirstName.text.toString()
            if (!firstNameText.matches(".*[a-z].*".toRegex())) {
                binding.firstNameContainer.helperText = "Must be characters"
            } else {
                binding.firstNameContainer.helperText = null
            }
        }
        binding.userSecondName.setOnFocusChangeListener { _, _ ->
            val secondNameText = binding.userSecondName.text.toString()
            if (!secondNameText.matches(".*[a-z].*".toRegex())) {
                binding.secondNameContainer.helperText = "Must be characters"
            } else {
                binding.secondNameContainer.helperText = null
            }
        }
        binding.userEmail.setOnFocusChangeListener { _, _ ->
            val emailText = binding.userEmail.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                binding.emailContainer.helperText = "Invalid email address"
            } else {
                binding.emailContainer.helperText = null
            }
        }
        binding.userPassword.setOnFocusChangeListener { _, _ ->
            val password = binding.userPassword.text.toString()
            if (password.length < 8) {
                binding.passwordContainer.helperText = "Password must be at least 8 characters"
            } else if (!password.matches(".*[A-Z].*".toRegex())) {
                binding.passwordContainer.helperText =
                    "Password must contain at least one uppercase letter"
            } else if (!password.matches(".*[a-z].*".toRegex())) {
                binding.passwordContainer.helperText =
                    "Password must contain at least one lowercase letter"
            } else {
                binding.passwordContainer.helperText = null
            }
//            else if(!password.matches(".*[@*/$].*".toRegex())) {
//                binding.passwordContainer.helperText = "Password must contain at least on symbol"
//            }
        }
        binding.userPasswordConfirm.setOnFocusChangeListener { _, _ ->
            val password = binding.userPasswordConfirm.text.toString()
            if (password != binding.userPassword.text.toString()) {
                binding.confirmPasswordContainer.helperText =
                    "Password must be equal to the above password"
            } else if (password.length < 8) {
                binding.confirmPasswordContainer.helperText =
                    "Password must be at least 8 characters"
            } else if (!password.matches(".*[A-Z].*".toRegex())) {
                binding.confirmPasswordContainer.helperText =
                    "Password must contain at least one uppercase letter"
            } else if (!password.matches(".*[a-z].*".toRegex())) {
                binding.confirmPasswordContainer.helperText =
                    "Password must contain at least one lowercase letter"
            } else {
                binding.confirmPasswordContainer.helperText = null
            }
        }

    }
}