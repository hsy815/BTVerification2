package com.hsy.btverification2.net

import android.content.Context
import android.net.ParseException
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonParseException
import org.json.JSONException
import java.net.ConnectException

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/11 14:15
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:15
 * @修改描述:
 */
object ThrowableManager {

    private val UNAUTHORIZED = 401
    private val FORBIDDEN = 403
    private val NOT_FOUND = 404
    private val REQUEST_TIMEOUT = 408
    private val INTERNAL_SERVER_ERROR = 500
    private val BAD_GATEWAY = 502
    private val SERVICE_UNAVAILABLE = 503
    private val GATEWAY_TIMEOUT = 504

    fun errorException(context: Context, e: Throwable?) {
        Log.i("tag", "e.toString = ${e!!.message}")

        if (e is retrofit2.HttpException) {
            when (e.code()) {
                UNAUTHORIZED, FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT -> mToast(
                        context,
                        "哎呀～走丢啦 |･ω･｀)"
                )//404找不到服务器
                GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> mToast(
                        context,
                        "不好～服务器睡着啦 Ծ‸Ծ"
                )//服务器错误
                else -> mToast(context, "咦～网络开小差了")//网络错误
            }

        } else if (e is JsonParseException
                || e is JSONException
                || e is ParseException
        ) {
            mToast(context, "老大，小的解析不了啊")//解析错误
        } else if (e is ConnectException) {
            mToast(context, "老大，人家不给访问...")//连接失败
        } else if (e is javax.net.ssl.SSLHandshakeException) {
            mToast(context, "老大，您的证书失效了...")//证书验证失败
        } else if (e is java.net.SocketTimeoutException) {
            mToast(context, "老大，等不到了...")//请求超时
        } else {
            mToast(context, "老大，请求失败了...")
        }
    }

    private fun mToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}