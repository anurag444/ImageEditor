package com.example.imageeditor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.imageeditor.databinding.ActivityAddTextBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddTextActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTextBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var vH: Float = 0F
    private var vW: Float = 0F
    private lateinit var textBit: Bitmap

    private lateinit var dv: DrawingView
    private val mPaint: Paint = Paint()
    private val mTextPaintOutline:Paint = Paint()
    var Esize = 105f
    var flg = false
    private var userInputValue = ""
    var colorCode = -0x10000
    private val DIALOG_ID = 0

    private var b:Float = 0F

    private var mStorageRef: StorageReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mStorageRef = FirebaseStorage.getInstance().reference
        val currentPath = sharedPreferences.getString(getString(R.string.current_path), "")
        textBit = BitmapFactory.decodeFile(currentPath)
        convertAndSetImage(currentPath)

        val targetW = sharedPreferences.getFloat("width", 0F)
        val targetH = sharedPreferences.getFloat("height", 0F)


        vH = targetH * 0.89f
        vW =
            targetH * 0.89f / textBit.height * textBit.width
        if (vW > targetW) {
            vW = targetW
            vH =
                targetW / textBit.width * textBit.height
        }

        dv = DrawingView(this)
        dv.background = BitmapDrawable(resources, textBit)

        dv.layoutParams = ViewGroup.LayoutParams(vW.toInt(), vH.toInt())
        (findViewById<LinearLayout>(R.id.view_drawing_pad)).addView(dv)


        binding.enterText.setOnClickListener {
            saveBitmap()
            userInputValue = ""
            dv.invalidate()
            dv.background = BitmapDrawable(resources, textBit)
            flg = true
            openDialog()
        }

        binding.saveChanges.setOnClickListener {
            saveImage()
        }

        binding.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                Log.d("Seekbar", "SeekValue ${seek.progress}")
                b = (seek.progress - 255).toFloat()

                dv.invalidate()
                //adjustBrightness((seek.progress - 255).toFloat())
            }
        })
    }


    //Save Bitmap for further editing
    private fun saveBitmap() {
        val bitmap = Bitmap.createBitmap(dv.width, dv.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        dv.draw(c)
        textBit = bitmap
    }
    private fun openDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.input_text_dialog, null)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.input_text)

        with(builder) {
            setTitle("Enter Text...")
            setPositiveButton("Done") { _, _ ->
                userInputValue = editText.text.toString()
                dv.invalidate()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun convertAndSetImage(currentPath: String?) {
        val bitmap = BitmapFactory.decodeFile(currentPath)

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
        //binding.image.setImageBitmap(rotatedBitmap)
        textBit = rotatedBitmap
    }

    inner class DrawingView(context: Context) : View(
        context
    ) {
        private lateinit var mBitmap: Bitmap
        private var mCanvas: Canvas? = null
        private val mBitmapPaint: Paint
        private lateinit var rect: Rect
        var xPos = 0f
        var yPos = 0f
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(vW.toInt(), vH.toInt(), oldw, oldh)
            mBitmap = Bitmap.createBitmap(vW.toInt(), vH.toInt(), Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap)
            xPos = (mCanvas!!.width / 2 - 2).toFloat()
            yPos = (mCanvas!!.height / 2 - (mPaint.descent() + mPaint.ascent()) / 2)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)


            canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
            mPaint.textSize = Esize
            mPaint.color = colorCode
            val width: Float = mPaint.measureText(userInputValue)

            if (flg) {
                //adjust brightness
                val cm = ColorMatrix()
                cm.set(
                    floatArrayOf(
                        1f,
                        0f,
                        0f,
                        0f,
                        b,
                        0f,
                        1f,
                        0f,
                        0f,
                        b,
                        0f,
                        0f,
                        1f,
                        0f,
                        b,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                )

                mBitmapPaint.colorFilter = ColorMatrixColorFilter(cm)
                val matrix = Matrix()
                canvas.drawBitmap(textBit, matrix, mBitmapPaint)

                canvas.drawText(userInputValue, xPos, yPos, mPaint)


                mTextPaintOutline.isAntiAlias = true
                mTextPaintOutline.textSize = 106F
                mTextPaintOutline.color = -0x10000
                mTextPaintOutline.style = Paint.Style.STROKE
                mTextPaintOutline.strokeWidth = 4F
                canvas.drawRect(xPos,yPos +  20F, xPos + width, yPos - 106F, mTextPaintOutline)
            }
        }

        private var mX = 0f
        private var mY = 0f
        private fun touch_start(x: Float, y: Float) {
            mX = x
            mY = y
            xPos = x
            yPos = y
        }

        private fun touch_move(x: Float, y: Float) {
            val dx = Math.abs(x - mX)
            val dy = Math.abs(y - mY)
            if (dx >= 4f || dy >= 4f) {
                mX = x
                mY = y
                xPos = x
                yPos = y
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touch_start(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    touch_move(x, y)
                    invalidate()
                }
            }
            return true
        }

        init {
            mBitmapPaint = Paint(Paint.DITHER_FLAG)
            mPaint.color = colorCode
            mPaint.isAntiAlias = true
            mPaint.isDither = true
            mPaint.style = Paint.Style.FILL
            mPaint.textSize = Esize
            rect = Rect()
        }
    }


    //Function to save image to a file
    @Throws(Exception::class)
    private fun saveImage() {
        saveBitmap()
        var fOut: FileOutputStream? = null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        val file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(imageFileName, ".png", file2)


        try {
            fOut = FileOutputStream(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        textBit.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        try {
            fOut!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            fOut!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            MediaStore.Images.Media.insertImage(
                contentResolver,
                file.absolutePath,
                file.name,
                file.name
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        //notify gallery to include saved image to its list
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val cUri = Uri.fromFile(file)
        mediaScanIntent.data = cUri
        this.sendBroadcast(mediaScanIntent)
        Toast.makeText(applicationContext, "Image Saved to Pictures", Toast.LENGTH_SHORT).show()
        dv.invalidate()


//        if (cUri != null) {
//            val fileName = UUID.randomUUID().toString() +".jpg"
//
//            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
//
//            refStorage.putFile(cUri)
//                .addOnSuccessListener { taskSnapshot ->
//                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
//
//                    }
//                }
//
//                .addOnFailureListener(OnFailureListener { e ->
//                    Toast
//                        .makeText(
//                            this,
//                            "Image Upload Fail",
//                            Toast.LENGTH_SHORT
//                        )
//                        .show();
//                    Log.d("ImageUpload", e.message.toString())
//                })
//        }

        finish()

    }


}


