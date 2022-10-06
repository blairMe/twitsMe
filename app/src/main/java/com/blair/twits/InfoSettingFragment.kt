package com.blair.twits

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.blair.twits.databinding.FragmentInfoSettingBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class InfoSettingFragment : Fragment() {

    private lateinit var binding: FragmentInfoSettingBinding

    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    private val packageName = "com.blair.twits"

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
                    val imageBitmap = data?.extras?.get("data") as Bitmap

                    // Display using Coil (Coroutine Image Loader)
                    binding.profilePicture.load(imageBitmap){
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())
                    }

                }

                GALLERY_REQUEST_CODE -> {
                    binding.profilePicture.load(data?.data) {
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())
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
}






//        binding.iNavigate.setOnClickListener {
//            val intent = Intent(activity, MainActivity::class.java)
//                        startActivity(intent)
//        }