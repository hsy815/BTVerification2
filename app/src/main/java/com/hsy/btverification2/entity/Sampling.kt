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
 * @创建时间: 2020/12/11 14:00
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:00
 * @修改描述:
 */
class Sampling {

    private var samplingReturn: SamplingReturn? = null
    private var sampling: Sampling? = null

    fun getSampling(context: Context, subscription: CompositeSubscription, map: Map<String, String>, samplingReturn: SamplingReturn) {
        this.samplingReturn = samplingReturn
        subscription.add(
                DataUtil.sampling(map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : MySubscriber<Sampling>(context) {
                            override fun onCompleted() {
                                samplingReturn.sampling(sampling)
                            }

                            override fun onNext(t: Sampling?) {
                                sampling = t
                            }

                            override fun onError(e: Throwable?) {
                                super.onError(e)
                                samplingReturn.spError(e)
                            }
                        })
        )
    }

    val success: Boolean = false
    val msg: String? = null

    interface SamplingReturn {
        fun sampling(sampling: Sampling?)
        fun spError(e: Throwable?)
    }
}