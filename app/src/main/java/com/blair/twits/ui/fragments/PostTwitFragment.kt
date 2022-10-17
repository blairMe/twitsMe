package com.blair.twits.ui.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.blair.twits.R
import com.blair.twits.databinding.FragmentPostTwitBinding
import com.blair.twits.firebase.PostTwit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class PostTwitFragment : Fragment() {

    private lateinit var binding : FragmentPostTwitBinding

    private val GALLERY_REQUEST_CODE = 1

    private val packageName = "com.blair.twits"

    // The current user's information
    val currentUser = Firebase.auth.currentUser

    // Image storage
    private val IMAGE_DIRECTORY = "twitsImages"
    private var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostTwitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.attachImg.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(requireActivity())
            pictureDialog.setTitle("Select Action")
            val pictureDialogItem = arrayOf("Select photo from Gallery", "Take picture with Camera")
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when(which) {
                    0 -> checkGalleryPermission()
                }
            }
            pictureDialog.show()
        }

        binding.posterButton.setOnClickListener {
            val twitText = binding.twitTextInput.text.toString().trim()
            val currentUserEmail = currentUser!!.email.toString()

            if(imagePath.isNotEmpty()) {
                // Posting
                PostTwit().postTwit(twitText, imagePath, currentUserEmail, view)
            } else {
                //Log.i("ImagePath Empty", "Image path has nothing")
                // Posting twits that have no image

                PostTwit().postNonImageTweet(twitText, currentUserEmail, view)

//                Navigation.findNavController(view)
//                    .navigate(R.id.action_postTwitFragment_to_homeFragment)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                GALLERY_REQUEST_CODE -> {
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

    private fun saveImageToInternalStorage(bitmap: Bitmap) : String {
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