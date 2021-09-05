package com.example.imageeditor

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.example.imageeditor.databinding.ActivityEditImageBinding

class EditImage : AppCompatActivity() {

    private lateinit var binding: ActivityEditImageBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val currentPath = sharedPreferences.getString(getString(R.string.current_path), "")
        convertAndSetImage(currentPath)


        binding.addText.setOnClickListener {
            val intent = Intent(this, AddTextActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun convertAndSetImage(currentPath: String?) {
        var bitmap = BitmapFactory.decodeFile(currentPath)

        val matrix = Matrix()


        matrix.postRotate(90f)


        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)


        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
        binding.image.setImageBitmap(rotatedBitmap)
    }
}