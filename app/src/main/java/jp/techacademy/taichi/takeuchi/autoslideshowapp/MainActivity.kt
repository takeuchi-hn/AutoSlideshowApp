package jp.techacademy.taichi.takeuchi.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.widget.CursorAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //メンバー変数定義
    var mCursor : Cursor? = null
    var mFieldIndex : Int? = null
    var mId : Long? = null
    var mImageUri : Uri? = null
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    fun getImageUri(){
        mFieldIndex = mCursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        mId = mCursor!!.getLong(mFieldIndex!!)
        mImageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mId!!)
        imageView.setImageURI(mImageUri)
    }
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        start_pause_button.setOnClickListener(){
            if(start_pause_button.text == "再生"){
                start_pause_button.text = "停止"
                next_button.text =""
                back_button.text =""
                if (mTimer == null) {
                    // タイマーの作成
                                        Log.d("TIMER","XXXXXXXXXXXXXXXX")
                    mTimer = Timer()
                    // タイマーの始動
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                if (start_pause_button.text == "停止") {
                                    if (mCursor!!.moveToNext()) {
                                    } else {
                                        mCursor!!.moveToFirst()
                                    }
                                    getImageUri()
                                } else if(start_pause_button.text == "再生") {
                                }
                            }
                        }
                    }, 1000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                }
            }else{
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
                start_pause_button.text = "再生"
                next_button.text ="進む"
                back_button.text ="戻る"
            }
        }



        next_button.setOnClickListener() {
            if(start_pause_button.text == "再生"){
                if (mCursor!!.moveToNext()) {
                } else {
                    mCursor!!.moveToFirst()
                }
                getImageUri()
            }
        }
        back_button.setOnClickListener() {
            if(start_pause_button.text == "再生") {
                if (mCursor!!.moveToPrevious()) {
                } else {
                    mCursor!!.moveToLast()
                }
                getImageUri()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }else{
                //拒否されたら終了する
                    finish()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )
        if (mCursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            getImageUri()
            imageView.setImageURI(mImageUri)
        }
    }


   override fun onDestroy(){
        super.onDestroy()
       if(mCursor!=null) {
           mCursor!!.close()//TODO nullでない場合にクローズにする
       }
   }
}
//   END   //