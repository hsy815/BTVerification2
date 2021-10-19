package com.hsy.btverification2.net

import android.content.Context
import android.util.Log
import retrofit2.HttpException
import rx.Subscriber

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/11 14:14
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:14
 * @修改描述:
 */
abstract class MySubscriber<T>(private val context: Context) : Subscriber<T>() {

    override fun onStart() {
        super.onStart()
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            MyToast.makeS(context, "老大，没网络呀，去看看您的网络设置吧")
            if (!isUnsubscribed) {
                unsubscribe()
            }
        }
    }

    override fun onError(e: Throwable?) {
        val responseBody = (e as HttpException).response()!!.errorBody()
        val a2 = (e as HttpException).response()!!.body().toString()
        Log.e("body", "${a2}}${responseBody.toString()}")
//        ThrowableManager.errorException(context, e)
    }
}