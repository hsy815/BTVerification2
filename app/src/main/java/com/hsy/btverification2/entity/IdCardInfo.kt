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
 * @创建时间: 2020/12/10 17:22
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/10 17:22
 * @修改描述:
 */
class IdCardInfo {

    private var dataReturn: DataReturn? = null
    private var inCardInfo: IdCardInfo? = null

    fun getIdCardInfo(
        context: Context,
        subscription: CompositeSubscription,
        map: Map<String, String>,
        mDataReturn: DataReturn
    ) {
        this.dataReturn = mDataReturn
        subscription.add(
            DataUtil.subIdCardEntry(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<IdCardInfo>(context) {
                    override fun onCompleted() {
                        dataReturn?.idInfo(inCardInfo)
                    }

                    override fun onNext(t: IdCardInfo?) {
                        inCardInfo = t
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        dataReturn?.idError(e)
                    }
                })
        )
    }

    val success: Boolean = false
    val code: Int = 0
    val sampleCode: String? = null
    val appointmentId: String? = null
    val msg: String? = null
    val name: String? = null

    interface DataReturn {
        fun idInfo(idCardInfo: IdCardInfo?)
        fun idError(e: Throwable?)
    }

}