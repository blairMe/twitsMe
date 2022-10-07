package com.blair.twits

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.blair.twits.databinding.FragmentInfoSettingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.*
import java.util.*

class InfoSettingFragment : Fragment() {

    private lateinit var binding: FragmentInfoSettingBinding

    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    private val packageName = "com.blair.twits"

    private lateinit var selectedImage : Bitmap

    // The the user's information
    val currentUser = Firebase.auth.currentUser

    // Firestore db
    val db = Firebase.firestore

    // Firebase Storage
    //val storage = Firebase.storage("gs://twits-2518e.appspot.com/profilePictures")
    val storage = Firebase.storage

    // Image storage
    private val IMAGE_DIRECTORY = "twitsImages"
    private var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            usernameFocusedListener()

            binding.rlSelectImg.setOnClickListener {
                val pictureDialog = AlertDialog.Builder(requireActivity())
                pictureDialog.setTitle("Select Action")
                val pictureDialogItem = arrayOf("Select photo from Gallery", "Take picture with Camera")
                pictureDialog.setItems(pictureDialogItem) { _, which ->
                    when(which) {
                        0 -> checkGalleryPermission()
                        1 -> checkCameraPermission()
                    }
                }
                pictureDialog.show()
                binding.pickPicture.visibility = View.GONE
            }

            binding.btnProceed.setOnClickListener {
                // Checking that the user details are valid
                val userNameValue = binding.usenameInput.text.toString().trim()

                if (userNameValue.isEmpty()) {
                    android.app.AlertDialog.Builder(requireActivity())
                        .setTitle("Invalid")
                        .setMessage("Please enter a value in the username.")
                        .setPositiveButton("Okay") { _, _ ->
                            // Nothing
                        }
                        .show()
                } else if (!userNameValue.matches(".*[a-z].*".toRegex())) {
                    android.app.AlertDialog.Builder(requireActivity())
                        .setTitle("Invalid")
                        .setMessage("Please enter a valid value.")
                        .setPositiveButton("Okay") { _, _ ->
                            // Nothing
                        }
                        .show()
                }

                currentUser?.let {
                    val email = currentUser.email.toString().trim()

                    db.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val usedEmail = document.data["born"].toString().trim()
                                val docName = document.id
                                Log.i("UserEmail", usedEmail)
                                updatingDetails(email, usedEmail, docName)
                            }

                        }
                        .addOnFailureListener { _ ->
                            Toast.makeText(requireActivity(), "Unsuccessful reaching firestore", Toast.LENGTH_SHORT).show()
                        }

                }
            }

    }

    private fun updatingDetails(email : String, usedEmail : String, docName : String) {

        if(email == usedEmail) {
            val userName = binding.usenameInput.text.toString().trim()

            // Upload the image
            val storageRef = storage.reference

            var file = Uri.fromFile(File("$imagePath"))
            val riversRef = storageRef.child("profilePictures/${file.lastPathSegment}")
            var uploadTask = riversRef.putFile(file)

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
            }

            db.collection("users").document(docName)
                .update(mapOf(
                    "setUniqueName" to userName
                ))
        }
    }

    private fun checkGalleryPermission() {
        Dexter.withContext(requireActivity()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    openGallery()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    showRationalDialogForPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?,
                ) {
                    showRationalDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun checkCameraPermission() {

        Dexter.withContext(requireActivity())
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        p0?.let {
                            if (p0.areAllPermissionsGranted()) {
                                openCamera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?,
                    ) {
                        showRationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val selectedImage = data?.extras?.get("data") as Bitmap

                    // Save image to internal storage
                    imagePath = saveImageToInternalStorage(selectedImage)

                    // Display using Coil (Coroutine Image Loader)
                    binding.profilePicture.load(selectedImage){
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data

                    // Display using Coil (Coroutine Image Loader)
                    binding.profilePicture.load(selectedImage) {
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())
                    }

                    // Convert data to bitmap
                    if (data != null && data.data != null) {
                        val uri = data.data!!
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                        cursor?.use { c ->
                            val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (c.moveToFirst()) {
                                val name = c.getString(nameIndex)
                                inputStream?.let { inputStream ->
                                    // create same file with same name
                                    val file = File(requireContext().cacheDir, name)
                                    val os = file.outputStream()
                                    os.use {
                                        inputStream.copyTo(it)
                                    }
                                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

                                    // Save image to internal storage
                                    imagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("Image path", imagePath)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(requireActivity())
            .setMessage("Please allow permission for your camera in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    // val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e : ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // Username validation
    fun usernameFocusedListener() {
        binding.usenameInput.setOnFocusChangeListener { _, _ ->
            val newUserName = binding.usenameInput.text.toString().trim()
            if (!newUserName.matches(".*[a-z].*".toRegex())) {
                binding.usernameContainer.helperText = "Username can be letters of numbers"
            } else {
                binding.usernameContainer.helperText = null
            }
        }
    }

    fun saveImageToInternalStorage(bitmap: Bitmap) : String {
        val wrapper = ContextWrapper(requireActivity().applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Activity.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return  file.absolutePath

    }
}






//        binding.iNavigate.setOnClickListener {
//            val intent = Intent(activity, MainActivity::class.java)
//                        startActivity(intent)
//        }