package com.hsy.btverification2

import android.app.Application
import com.hsy.btverification2.entity.SecretToken

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification
 * @创始人: hsy
 * @创建时间: 2021/8/26 17:02
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 17:02
 * @修改描述:
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: MyApplication? = null
            private set
    }

    var secretToken: SecretToken? = null
}