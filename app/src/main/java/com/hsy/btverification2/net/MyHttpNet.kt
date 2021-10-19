package com.hsy.btverification2.net

import android.os.Handler
import android.util.Log
import com.hsy.btverification2.MyApplication
import com.hsy.btverification2.helpUtil.LoginHttpManager
import com.hsy.btverification2.helpUtil.ScanHttpManager
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder

/**
 * @项目名: BTVerification2
 * @类位置: com.hsy.btverification2.net
 * @创始人: hsy
 * @创建时间: 2021/9/18 13:34
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/9/18 13:34
 * @修改描述:
 */
object MyHttpNet {

    private const val httpStr = "http://apigateway.biotecan.com/ocelot/Covid19Api/"
//    private const val httpStr = "http://10.5.35.114:88/"

    /**
     * 发送运输码
     */
    fun goOkHttpSendCode(mCode: String, deviceId: String, handler: Handler) {
        val secretToken = MyApplication.instance!!.secretToken!!.access_token
        val mUrl = StringBuffer()
        mUrl.append(httpStr)
        mUrl.append(PreApi.sendCode)
        mUrl.append(URLEncoder.encode(mCode, "GBK"))
        mUrl.append("/")
        mUrl.append(deviceId)
        Log.e("MyHttpNet", mUrl.toString())
        Thread {
            val client = OkHttpClient().newBuilder().build()
            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body = "".toRequestBody(mediaType)
            val request: Request = Request.Builder()
                .url(mUrl.toString())
                .post(body)
                .addHeader(
                    "Authorization",
                    "Bearer $secretToken"
                )
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body!!.string()
                Log.e("MyHttpNet", responseBody)
                handler.obtainMessage(LoginHttpManager.SEND_CODE, responseBody).sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 跟据预约码获取客户信息
     */
    fun goOkHttpUserInfo(submitStr: String?, handler: Handler) {
        val secretToken = MyApplication.instance!!.secretToken!!.access_token
        val mUrl = StringBuffer()
        mUrl.append(httpStr)
        mUrl.append(PreApi.getUserInfo)
        mUrl.append(URLEncoder.encode(submitStr, "GBK"))

        Thread {
            val client = OkHttpClient().newBuilder().build()
            val request: Request = Request.Builder()
                .url(mUrl.toString())
                .get()
                .addHeader(
                    "Authorization",
                    "Bearer $secretToken"
                )
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body!!.string()
                Log.e("MyHttpNet", responseBody)
                handler.obtainMessage(ScanHttpManager.USER_INFO_MANAGER, responseBody)
                    .sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 采样确认并打码
     */
    fun goOkHttpConfirmAndSubmit(submitStr: String?, code: String, handler: Handler) {
        val secretToken = MyApplication.instance!!.secretToken!!.access_token
        val mUrl = StringBuffer()
        mUrl.append(httpStr)
        mUrl.append(PreApi.scanCodeEntry)
        mUrl.append(URLEncoder.encode(submitStr, "GBK"))
        mUrl.append("/")
        mUrl.append(code)

        Thread {
            val client = OkHttpClient().newBuilder().build()
            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body = "".toRequestBody(mediaType)
            val request: Request = Request.Builder()
                .url(mUrl.toString())
                .patch(body)
                .addHeader(
                    "Authorization",
                    "Bearer $secretToken"
                )
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body!!.string()
                Log.e("MyHttpNet", responseBody)
                handler.obtainMessage(ScanHttpManager.CONFIRM_AND_SUBMIT, responseBody)
                    .sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 封箱操作
     */
    fun goOkHttpFinishedBox(code: String, handler: Handler) {
        val secretToken = MyApplication.instance!!.secretToken!!.access_token
        val mUrl = StringBuffer()
        mUrl.append(httpStr)
        mUrl.append(PreApi.finishedBox)
        mUrl.append(URLEncoder.encode(code, "GBK"))

        Thread {
            val client = OkHttpClient().newBuilder().build()
            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body = "".toRequestBody(mediaType)
            val request: Request = Request.Builder()
                .url(mUrl.toString())
                .patch(body)
                .addHeader(
                    "Authorization",
                    "Bearer $secretToken"
                )
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body!!.string()
                Log.e("MyHttpNet", responseBody)
                handler.obtainMessage(ScanHttpManager.FINISHED_BOX, responseBody)
                    .sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
}