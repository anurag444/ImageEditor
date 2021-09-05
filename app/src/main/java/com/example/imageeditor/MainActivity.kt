package com.example.imageeditor

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.widget.Filter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.imageeditor.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private var currentPhotoPath: String? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = sharedPreferences.edit()



//        binding.captureButton.isEnabled = false
//
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
//        }else
//            binding.captureButton.isEnabled = true


        binding.captureButton.setOnClickListener {

            var fileName = "photo"
            var storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            var imageFile = File.createTempFile(fileName, ".jpg", storageDirectory)

            currentPhotoPath = imageFile.absolutePath
            editor.putString(getString(R.string.current_path), currentPhotoPath)
            editor.apply()

            var imageUri: Uri = FileProvider.getUriForFile(this, "com.example.imageeditor.fileprovider", imageFile)

            var i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(i, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101){

            val H = this.window.decorView.height
            val W = this.window.decorView.width
            editor.putFloat("height", H.toFloat())
            editor.putFloat("width", W.toFloat())
            editor.apply()
            val intent = Intent(this,EditImage::class.java)


            startActivity(intent)



        }
    }

}