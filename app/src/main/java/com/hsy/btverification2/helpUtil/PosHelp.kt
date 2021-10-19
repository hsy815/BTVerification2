package com.hsy.btverification2.helpUtil

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import com.android.printsdk.PosManager
import com.android.printsdk.bean.TextData
import com.android.printsdk.bean.enums.ALIGN_MODE
import com.android.printsdk.bean.enums.BARCODE_NAME
import com.android.printsdk.bean.enums.MODE_ENLARGE
import com.android.printsdk.interfaces.OnPrintEventListener
import com.hsy.btverification2.R
import com.hsy.btverification2.net.MyToast


/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.helpUtil
 * @创始人: hsy
 * @创建时间: 2020/12/15 10:26
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/15 10:26
 * @修改描述:
 */
class PosHelp(private val context: Context) {

    private var mHandler: Handler? = null
    private var mPosCallback: PosCallback? = null
    private var mWhatTag = 0

    fun initPos() {
        PosManager.getClient().initSDK()
        PosManager.getClient().setEncode(3)
        PosManager.getClient().setLanguage(15)
        PosManager.getClient().setConcentration(30)
        PosManager.getClient().setEnableMark(true)
        PosManager.getClient().setPrintEventListener(onPrintEventListener)
    }

    private var onPrintEventListener: OnPrintEventListener = object : OnPrintEventListener {
        override fun onPrintState(state: Int) {
            when (state) {
                0 -> {
                    MyToast.makeS(context, context.getString(R.string.toast_print_success))
                    if (mPosCallback != null)
                        mPosCallback!!.restartRead()
                    if (mHandler != null)
                        mHandler!!.obtainMessage(mWhatTag).sendToTarget()
                }
                2 -> {
                    errorMessageDialog(context.getString(R.string.toast_print_error))
                }
                1 -> {
                    errorMessageDialog(context.getString(R.string.toast_no_paper))
                }
                else -> {
                    errorMessageDialog(context.getString(R.string.unknown_error))
                }
            }
        }

        override fun onVersion(s: String) {

        }
    }

    private fun errorMessageDialog(msg: String?) {
        AlertDialog.Builder(context)
            .setTitle("提示")
            .setMessage(msg)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 添加打印数据
     */
    fun setPrintStart(sampleCode: String?, name: String?) {

        val textData1 = TextData()
        textData1.addFontSize(MODE_ENLARGE.NORMAL)
        textData1.addAlignment(ALIGN_MODE.ALIGN_CENTER)
        textData1.addText(name)
        textData1.addText("\n")
        PosManager.getClient().addText(textData1)
        PosManager.getClient()
            .addBarCode(BARCODE_NAME.CODE128, sampleCode, 30, 1, ALIGN_MODE.ALIGN_CENTER)
//        PosManager.getClient().addGoToNextMark()

        val textData2 = TextData()
        textData2.addFontSize(MODE_ENLARGE.NORMAL)
        textData2.addAlignment(ALIGN_MODE.ALIGN_CENTER)
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("\n")
        PosManager.getClient().addText(textData2)

        PosManager.getClient().addText(textData1)
        PosManager.getClient()
            .addBarCode(BARCODE_NAME.CODE128, sampleCode, 30, 1, ALIGN_MODE.ALIGN_CENTER)
        PosManager.getClient().addGoToNextMark()
    }

    /**
     * 打印二维码
     */
    fun setPrintStartQR(sampleCode: String?, name: String?) {
        val textData1 = TextData()
        textData1.addFontSize(MODE_ENLARGE.NORMAL)
        textData1.addAlignment(ALIGN_MODE.ALIGN_CENTER)
        textData1.addText(name)
        PosManager.getClient().addText(textData1)
        PosManager.getClient()
            .addQR(3, 3, 39, ALIGN_MODE.ALIGN_CENTER, sampleCode)

        val textData2 = TextData()
        textData2.addFontSize(MODE_ENLARGE.NORMAL)
        textData2.addAlignment(ALIGN_MODE.ALIGN_CENTER)
        textData2.addText("\n")
        textData2.addText("\n")
        PosManager.getClient().addText(textData2)

        PosManager.getClient().addText(textData1)
        PosManager.getClient()
            .addQR(3, 3, 39, ALIGN_MODE.ALIGN_CENTER, sampleCode)
        PosManager.getClient().addGoToNextMark()
    }

    fun setBmp(bitmap: Bitmap) {
        PosManager.getClient().addBmp(10, bitmap)
        val textData2 = TextData()
        textData2.addFontSize(MODE_ENLARGE.NORMAL)
        textData2.addAlignment(ALIGN_MODE.ALIGN_CENTER)
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("------------------")
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("\n")
        textData2.addText("\n")
        PosManager.getClient().addText(textData2)
        PosManager.getClient().printStart()
    }

    /**
     * 开始打印
     */
    fun printStart() {
        PosManager.getClient().printStart()
    }

    interface PosCallback {
        fun restartRead()
    }

    fun setPosCallback(posCallback: PosCallback) {
        this.mPosCallback = posCallback
    }

    /**
     * 打印程序是异步的，需要异步通知刷新UI
     */
    fun setHandler(handler: Handler?, whatTag: Int) {
        this.mHandler = handler
        this.mWhatTag = whatTag
    }

    fun close() {
        PosManager.getClient().closeDev()
    }
}