package com.hsy.btverification2.net

import com.hsy.btverification2.MyApplication
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @项目名: BTVerification2
 * @类位置: com.hsy.btverification2.net
 * @创始人: hsy
 * @创建时间: 2021/9/17 15:18
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/9/17 15:18
 * @修改描述:
 */
class AddToken : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val headers = Headers.Builder()
        val secretToken = MyApplication.instance!!.secretToken
        if (secretToken != null) {
            headers.addUnsafeNonAscii(
                "Authorization",
                "Bearer ${secretToken.access_token}"
            )
            headers.addUnsafeNonAscii(
                "Host",
                "apigateway.biotecan.com"
            )
            headers.addUnsafeNonAscii(
                "Content-Type",
                "text/plain; charset=utf-8"
            )
        }

        val requestBuilder: Request.Builder = original.newBuilder()
            .headers(headers.build())
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}