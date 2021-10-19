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
 * @创建时间: 2020/12/22 14:44
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/22 14:44
 * @修改描述:
 */
class LoginInfo {

    private var loginInfoReturn: LoginInfoReturn? = null
    private var loginInfo: LoginInfo? = null

    fun getLogin(context: Context, subscription: CompositeSubscription, user: String, psw: String, loginInfoReturn: LoginInfoReturn) {
        this.loginInfoReturn = loginInfoReturn
        subscription.add(
                DataUtil.getLogin(user, psw)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : MySubscriber<LoginInfo>(context) {
                            override fun onCompleted() {
                                loginInfoReturn.spLogin(loginInfo)
                            }

                            override fun onNext(t: LoginInfo?) {
                                loginInfo = t
                            }

                            override fun onError(e: Throwable?) {
                                super.onError(e)
                                loginInfoReturn.spError(e)
                            }
                        })
        )
    }

    val success: Boolean = false
    val msg: String? = null

    interface LoginInfoReturn {
        fun spLogin(loginInfo: LoginInfo?)
        fun spError(e: Throwable?)
    }

}