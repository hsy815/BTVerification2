package com.hsy.btverification2.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/10 16:59
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/10 16:59
 * @修改描述:
 */
object NetRetrofit {

    private const val DEFAULT_TIMEOUT = 120
    private const val mUrl = "http://ApiGateway.biotecan.com/"//测试环境
//    private const val mUrl = "https://iam.biotecan.com:8667/"//正式环境

    fun reRetrofit(): ApiService {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        val client: OkHttpClient = OkHttpClient.Builder()
//            .proxy(Proxy.NO_PROXY) //不使用代理 以防被抓包（测试时需要注意）
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }) //打印log
            .addInterceptor(AddToken())
            .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}