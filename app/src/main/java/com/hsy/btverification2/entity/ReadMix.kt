package com.hsy.btverification2.entity

import android.content.Context
import com.hsy.btverification2.net.DataUtil
import com.hsy.btverification2.net.MySubscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.entity
 * @创始人: hsy
 * @创建时间: 2021/8/23 15:41
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/23 15:41
 * @修改描述:
 */
class ReadMix {
    private var readMixReturn: ReadMixReturn? = null
    private var readMix: ReadMix? = null

    fun getReadMix(context: Context, subscription: CompositeSubscription, map: Map<String, String>, readMixReturn: ReadMixReturn) {
        this.readMixReturn = readMixReturn
        subscription.add(
            DataUtil.readMix(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<ReadMix>(context) {
                    override fun onCompleted() {
                        readMixReturn.readMix(readMix)
                    }

                    override fun onNext(t: ReadMix?) {
                        readMix = t
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        readMixReturn.spError(e)
                    }
                })
        )
    }

    val success: Boolean = false
    val msg: String? = null

    interface ReadMixReturn {
        fun readMix(readMix: ReadMix?)
        fun spError(e: Throwable?)
    }
}