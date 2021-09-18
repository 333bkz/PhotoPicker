package com.bkz.photo_picker

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.yalantis.ucrop.UCrop
import java.io.File

fun UCrop.intent(context: ContextDelegate): Intent? =
    context.activity?.run {
        getIntent(this)
    } ?: context.fragment?.context?.run {
        getIntent(this)
    }

fun Context.cameraImageUri(uri: Uri, action: (uri: Uri) -> Unit) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    packageManager?.resolveActivity(intent, 0)?.let {
        action.invoke(uri)
    }
}

fun Context.appCacheFile(dir: String) = File(externalCacheDir ?: cacheDir, dir)

fun Context.cameraUriExpand(name: String): Uri? {
    val file: File = when {
        // Android Q
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> File("", name)
        // 获取file.path
        else -> lowerVersionFile(name)
    }
    return insertImageUri(file, Environment.DIRECTORY_PICTURES)
}

// 获取图片Uri,适配至高版本,Q以上按照[MediaStore.MediaColumns.RELATIVE_PATH]，以下按照[MediaStore.MediaColumns.DATA]
fun Context.insertImageUri(
    file: File,
    relativePath: String,
): Uri? = this@insertImageUri.insertImageUri(ContentValues().apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
    } else {
        @Suppress("DEPRECATION")
        put(MediaStore.MediaColumns.DATA, file.path)
    }
})

//获取图片Uri
fun Context.insertImageUri(contentValues: ContentValues): Uri? =
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    } else {
        null
    }

fun Context.lowerVersionFile(fileName: String): File = File(
    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
        || !Environment.isExternalStorageRemovable()
    ) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
    } else {
        externalCacheDir?.path ?: cacheDir.path
    }, fileName
)