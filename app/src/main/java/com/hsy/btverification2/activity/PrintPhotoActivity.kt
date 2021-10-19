package com.hsy.btverification2.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import com.hsy.btverification2.R
import com.hsy.btverification2.helpUtil.PosHelp
import kotlinx.android.synthetic.main.activity_print_photo.*

/**
 * 这是之前写了玩的
 */
class PrintPhotoActivity : BaseActivity() {

    private val TAKE_PHOTO_REQUEST: Int = 111
    private val TAKE_ALBUM_REQUEST: Int = 222
    private var mPosHelp: PosHelp? = null

    override fun init(savedInstanceState: Bundle?) {

    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_print_photo
    }

    override fun initUI() {
        initPos()
        print_photo_btn.setOnClickListener {
            pictureManager()
        }
    }

    private fun pictureManager() {
        AlertDialog.Builder(this)
                .setMessage("选择图片方式")
                .setNeutralButton("拍照") { dialog, _ ->
                    isCamera()
                    dialog.dismiss()
                }
                .setPositiveButton("相册") { dialog, _ ->
                    isAlbum()
                    dialog.dismiss()
                }.create().show()
    }

    /**
     * 打开相机拍照
     */
    private fun isCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, TAKE_PHOTO_REQUEST)
    }

    /**
     * 打开相册选择图片
     */
    private fun isAlbum() {
        //以下方式可以打开最近图片
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, TAKE_ALBUM_REQUEST)

        //以下方式只打开相册
//        val intent = Intent()
//        intent.action = Intent.ACTION_PICK
//        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        startActivityForResult(intent, 222)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO_REQUEST -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    return
                }
                val photo: Bitmap? = data!!.getParcelableExtra("data")
                print_img.setImageBitmap(photo)
                mPosHelp?.setBmp(photo!!)
            }
            TAKE_ALBUM_REQUEST -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    return
                }
                val imgUri = data?.data
                print_img.setImageURI(imgUri)
                val bitmap =
                        BitmapFactory.decodeStream(contentResolver.openInputStream(imgUri!!))
                mPosHelp?.setBmp(bitmap!!)
            }
        }
    }

    /**
     * 初始化打印机
     */
    private fun initPos() {
        mPosHelp = PosHelp(this)
        mPosHelp?.initPos()
    }
}