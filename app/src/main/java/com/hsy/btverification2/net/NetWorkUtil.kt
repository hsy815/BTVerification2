package com.hsy.btverification2.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/11 14:15
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:15
 * @修改描述:
 */
object NetWorkUtil {

    /**
     * 判断是否连网
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networks = mgr.allNetworks
            var networkInfo: NetworkInfo
            for (mNewWork in networks) {
                networkInfo = mgr.getNetworkInfo(mNewWork)!!
                if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        } else {
            val info = mgr.allNetworkInfo
            if (info != null) {
                for (i in info.indices) {
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }
}