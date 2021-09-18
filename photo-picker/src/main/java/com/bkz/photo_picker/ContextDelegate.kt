package com.bkz.photo_picker

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

data class ContextDelegate(
    val activity: AppCompatActivity? = null,
    val fragment: Fragment? = null
) {
    fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I>? =
        fragment?.registerForActivityResult(contract, callback)
            ?: activity?.registerForActivityResult(contract, callback)

    val context: Context =
        activity ?: fragment?.requireContext() ?: throw NullPointerException("the context is null")

    val lifecycleOwner: LifecycleOwner? = activity ?: fragment?.viewLifecycleOwner
}

