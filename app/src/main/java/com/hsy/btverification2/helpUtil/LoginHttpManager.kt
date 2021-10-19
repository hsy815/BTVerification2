package com.hsy.btverification2.helpUtil

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.google.gson.Gson
import com.hsy.btverification2.entity.HttpNetError
import com.hsy.btverification2.entity.SendNetDate
import com.hsy.btverification2.net.LoadingDialog
import com.hsy.btverification2.net.MyHttpNet
import com.hsy.btverification2.net.MyToast
import com.hsy.btverification2.net.NetWorkUtil

/**
 * @项目名: BTVerification2
 * @类位置: com.hsy.btverification2.helpUtil
 * @创始人: hsy
 * @创建时间: 2021/9/28 14:38
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/9/28 14:38
 * @修改描述:
 */
class LoginHttpManager(
    val context: Context,
    val loadingDialog: LoadingDialog
) {

    private var mOKSendCode: OKSendCode? = null

    companion object {
        const val SEND_CODE = 101
    }

    fun goOkHttpSendCode(context: Context, mCode: String) {
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            MyToast.makeS(context, "老大，没网络呀，去看看您的网络设置吧")
            return
        }
        loadingDialog.show()
        MyHttpNet.goOkHttpSendCode(mCode, DeviceUtil.getDeviceId(context), handler)
    }

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            loadingDialog.dismiss()
            mYSendDate(msg)
            super.handleMessage(msg)
        }
    }

    private fun mYSendDate(msg: Message) {
        val gson = Gson()
        try {
            val httpNetError =
                gson.fromJson(msg.obj.toString(), HttpNetError::class.java)
            if (httpNetError != null) {
                if (!TextUtils.isEmpty(httpNetError.ResultMsg)) {
                    if (500 == httpNetError.ResultCode) {
                        MyToast.makeL(context, "服务器错误，重新点击生成运输码后再试！")
                    } else {
                        MyToast.makeL(context, httpNetError.ResultMsg)
                    }
                    return
                }
            }
            val sendNetDate = gson.fromJson(msg.obj.toString(), SendNetDate::class.java)
            if (sendNetDate != null) {
                if (!TextUtils.isEmpty(sendNetDate.Code)) {
                    mOKSendCode?.ok()
                }
            }
        } catch (e: Exception) {
            MyToast.makeL(context, msg.obj.toString())
        }
    }

    interface OKSendCode {
        fun ok()
    }

    fun setOKSendCode(oKSendCode: OKSendCode) {
        this.mOKSendCode = oKSendCode
    }

}