package com.hsy.btverification2.entity

import android.content.Context
import com.hsy.btverification2.net.DataUtil
import com.hsy.btverification2.net.MySubscriber
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.entity
 * @创始人: hsy
 * @创建时间: 2020/12/11 14:09
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:09
 * @修改描述:
 */
class ScanCodeInfo {

    private var dataReturn: DataReturn? = null
    private var scanCodeInfo: ScanCodeInfo? = null

    fun getIdCardInfo(
        context: Context,
        subscription: CompositeSubscription,
        mCode: String,
        deviceId: String,
        mDataReturn: DataReturn
    ) {
        this.dataReturn = mDataReturn
        subscription.add(
            DataUtil.scanCodeEntry(mCode, deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<ScanCodeInfo>(context) {
                    override fun onCompleted() {
                        dataReturn?.idInfo(scanCodeInfo)
                    }

                    override fun onNext(t: ScanCodeInfo?) {
                        scanCodeInfo = t
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
    val name: String? = null
    val msg: String? = null

    interface DataReturn {
        fun idInfo(scanCodeInfo: ScanCodeInfo?)
        fun idError(e: Throwable?)
    }
}