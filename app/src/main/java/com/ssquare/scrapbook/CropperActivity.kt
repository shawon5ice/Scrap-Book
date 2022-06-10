package com.ssquare.scrapbook

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yalantis.ucrop.UCrop
import java.io.File
import java.lang.StringBuilder
import java.util.*

class CropperActivity : AppCompatActivity() {
    private lateinit var result:String
    private lateinit var fileUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)

        readIntent()
        val dest_uri = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        UCrop.of(fileUri, Uri.fromFile(File(cacheDir,dest_uri)))
            .useSourceImageAspectRatio()
            .withMaxResultSize(120,120)
            .start(this@CropperActivity)
    }

    private fun readIntent() {
        val intent = intent
        if(intent.extras!=null){
            result = intent.getStringExtra("DATA").toString()
            fileUri = Uri.parse(result)


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            val resultUri=UCrop.getOutput(data!!).toString();
            Log.d("resultu",resultUri)
            val returnIntent= Intent();
            returnIntent.putExtra("RESULT",resultUri);
            setResult(-1,returnIntent);
            finish();
        }else if(resultCode == UCrop.RESULT_ERROR){
            val cropError : Throwable? = UCrop.getError(data!!)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}