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
 * @创建时间: 2021/8/26 10:48
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 10:48
 * @修改描述:
 */
class PartnerNet() {

    private var mPartnerNetReturn: PartnerNetReturn? = null//获取数据
    private var partnerNet: PartnerNet? = null//获取数据

    private var mPartnerNetSaveReturn: PartnerNetSaveReturn? = null//新建数据
    private var partnerNetSave: PartnerNet? = null//新建数据

    fun getReadMix(
        context: Context,
        subscription: CompositeSubscription,
        gid: String,
        partnerNetReturn: PartnerNetReturn
    ) {
        this.mPartnerNetReturn = partnerNetReturn
        subscription.add(
            DataUtil.getPartner(gid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<PartnerNet>(context) {
                    override fun onCompleted() {
                        mPartnerNetReturn!!.partner(partnerNet)
                    }

                    override fun onNext(t: PartnerNet?) {
                        partnerNet = t
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        mPartnerNetReturn!!.spError(e)
                    }

                })
        )
    }

    fun saveReadMix(
        context: Context,
        subscription: CompositeSubscription,
        map: Map<String, String>,
        partnerNetSaveReturn: PartnerNetSaveReturn
    ) {
        this.mPartnerNetSaveReturn = partnerNetSaveReturn
        subscription.add(
            DataUtil.savePartner(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<PartnerNet>(context) {
                    override fun onCompleted() {
                        mPartnerNetSaveReturn!!.partner(partnerNetSave)
                    }

                    override fun onNext(t: PartnerNet?) {
                        partnerNetSave = t
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        mPartnerNetSaveReturn!!.spError(e)
                    }

                })
        )
    }

    val success: Boolean = false
    val total: Int = 0
    val rows: ArrayList<Partner>? = null
    val errorMsg: String? = null

    /**
     * 获取数据返回数据的接口
     */
    interface PartnerNetReturn {
        fun partner(partnerNet: PartnerNet?)
        fun spError(e: Throwable?)
    }

    /**
     * 新建数据返回数据的接口
     */
    interface PartnerNetSaveReturn {
        fun partner(partnerNet: PartnerNet?)
        fun spError(e: Throwable?)
    }
}