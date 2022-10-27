package com.bkz.demo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bkz.photo_picker.Context
import com.bkz.photo_picker.PickerDelegate

class MainActivity : AppCompatActivity() {

    private var picker: PickerDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        picker = PickerDelegate(Context.Activity(this)) { _, path ->
            findViewById<TextView>(R.id.result).text = path
        }.also {
            it.options.withAspectRatio(1.5f, 1f)
        }
        findViewById<View>(R.id.openCamera).setOnClickListener {
            picker?.openCamera()
        }
        findViewById<View>(R.id.openAlbum).setOnClickListener {
            picker?.openAlbum()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        picker?.recycle()
    }
}