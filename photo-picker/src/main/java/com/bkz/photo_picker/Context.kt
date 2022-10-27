package com.bkz.photo_picker

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner

sealed class Context {
    abstract fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>,
    ): ActivityResultLauncher<I>


    abstract val context: Context
    abstract val lifecycleOwner: LifecycleOwner

    class Activity(
        private val activity: AppCompatActivity,
    ) : com.bkz.photo_picker.Context() {

        override val context: Context = activity
        override val lifecycleOwner: LifecycleOwner = activity
        override fun <I, O> registerForActivityResult(
            contract: ActivityResultContract<I, O>,
            callback: ActivityResultCallback<O>,
        ): ActivityResultLauncher<I> = activity.registerForActivityResult(contract, callback)
    }

    class Fragment(
        private val fragment: androidx.fragment.app.Fragment,
    ) : com.bkz.photo_picker.Context() {

        override val context: Context = fragment.requireContext()
        override val lifecycleOwner: LifecycleOwner = fragment.viewLifecycleOwner
        override fun <I, O> registerForActivityResult(
            contract: ActivityResultContract<I, O>,
            callback: ActivityResultCallback<O>,
        ): ActivityResultLauncher<I> = fragment.registerForActivityResult(contract, callback)
    }
}

