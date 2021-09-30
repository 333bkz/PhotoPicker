package com.bkz.photo_picker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.util.FileUtils
import java.io.File

class PickerDelegate(
    private val contextDelegate: ContextDelegate,
    private val result: (Uri, String) -> Unit
) {

    private var cameraUri: Uri? = null

    val options: UCrop.Options = UCrop.Options().apply {
        setCompressionFormat(Bitmap.CompressFormat.JPEG)
        setHideBottomControls(true)
        //useSourceImageAspectRatio()
        withAspectRatio(1f, 1f)
        withMaxResultSize(800, 800)
        //setCompressionQuality(70)
    }

    //相册
    private var albumLauncher: ActivityResultLauncher<Array<String>>? =
        contextDelegate.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.run {
                crop(this)
            }
        }

    //相机
    private val cameraLauncher: ActivityResultLauncher<Uri>? =
        contextDelegate.registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                cameraUri?.run {
                    crop(this)
                }
            } else {
                cameraUri = null
            }
        }

    //裁剪
    private val cropLauncher: ActivityResultLauncher<Intent>? =
        contextDelegate.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.run {
                    val uri = UCrop.getOutput(this)
                    uri?.run {
                        val path = FileUtils.getPath(contextDelegate.context, uri)
                        result.invoke(this, path)
                    }
                }
            }
        }

    //权限
    private val cameraPermissionLauncher: ActivityResultLauncher<Array<String>>? =
        contextDelegate.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.forEach { item ->
                if (item.value == false) {
                    Toast.makeText(
                        contextDelegate.context, "我们需要相关权限，才能实现功能，请开启应用的相关权限", Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }
            }
            launchCamera()
        }

    //权限
    private val albumPermissionLauncher: ActivityResultLauncher<Array<String>>? =
        contextDelegate.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.forEach { item ->
                if (item.value == false) {
                    Toast.makeText(
                        contextDelegate.context, "我们需要相关权限，才能实现功能，请开启应用的相关权限", Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }
            }
            launchAlbum()
        }

    private fun crop(it: Uri) {
        val path = contextDelegate.context.appCacheFile("image").absolutePath
        val appDir = File(path)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val file = File(appDir, "${System.currentTimeMillis()}.jpg")
        val uCrop = UCrop.of(it, Uri.fromFile(file))
        uCrop.withOptions(options)
        uCrop.intent(contextDelegate)?.let {
            cropLauncher?.launch(it)
        }
    }

    private fun launchCamera() {
        val name = "${System.currentTimeMillis()}.jpg"
        cameraUri = contextDelegate.context.cameraUriExpand(name)
        cameraUri?.let {
            contextDelegate.context.cameraImageUri(it) { uri ->
                cameraLauncher?.launch(uri)
            }
        }
    }

    private fun launchAlbum() {
        albumLauncher?.launch(arrayOf("image/*"))
    }

    fun openCamera() {
        cameraPermissionLauncher?.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    fun openAlbum() {
        albumPermissionLauncher?.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    fun recycle() {
        albumLauncher?.unregister()
        cropLauncher?.unregister()
        cameraLauncher?.unregister()
        cameraPermissionLauncher?.unregister()
        albumPermissionLauncher?.unregister()
    }
}
