package com.hsy.btverification2.helpUtil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.*

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.helpUtil
 * @创始人: hsy
 * @创建时间: 2020/12/22 15:22
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/22 15:22
 * @修改描述:
 */
object DeviceUtil {

    fun getDeviceId(context: Context): String {

        val mTelephonyMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var imsi = ""
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            imsi = mTelephonyMgr.subscriberId//获取IMSI号
        }
        val imei = mTelephonyMgr.deviceId //获取IMEI号
        return imei
    }

}