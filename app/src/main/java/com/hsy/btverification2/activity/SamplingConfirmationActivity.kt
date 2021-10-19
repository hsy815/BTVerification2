package com.hsy.btverification2.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanDevice
import android.os.Bundle
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.ModeUtil
import com.hsy.btverification2.entity.Sampling
import com.hsy.btverification2.entity.Statistics
import com.hsy.btverification2.helpUtil.DeviceUtil
import com.hsy.btverification2.helpUtil.FileTxt
import com.hsy.btverification2.helpUtil.SPreferences
import com.hsy.btverification2.net.MyToast
import kotlinx.android.synthetic.main.activity_read_id.*
import kotlinx.android.synthetic.main.activity_sampling_confirmation.*
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.title_layout.*
import rx.subscriptions.CompositeSubscription

class SamplingConfirmationActivity : BaseActivity() {

    private var mCount = 0//记录本次采集的数据(准备入库数据)
    private var mCountSum = 0//记录本次采集的数据(显示数据)

    private var sPreferences: SPreferences? = null
    private var sm: ScanDevice? = null
    private val SCAN_ACTION = "scan.rcv.message"
    private val mScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barocode = intent.getByteArrayExtra("barocode")
            val barocodelen = intent.getIntExtra("length", 0)
            val temp = intent.getByteExtra("barcodeType", 0.toByte())
            val aimid = intent.getByteArrayExtra("aimid")
            val barcodeStr = String(barocode!!, 0, barocodelen)
            sm_content_text.setText(barcodeStr)
            scanCode(barcodeStr)
            sm?.stopScan()
        }
    }

    private var mTime = ""
    private var mPartner = ""
    private var mLocale = ""

    override fun init(savedInstanceState: Bundle?) {
        sm = ScanDevice()
        sm!!.openScan()
        sm!!.outScanMode = 0 //启动就是广播模式
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_sampling_confirmation
    }

    override fun initUI() {
        title_text.text = "仅采样"
        sPreferences = SPreferences(this, SPreferences.LOGINKEY)
        mTime = getDate()
        mPartner = sPreferences!!.getSp(SPreferences.PARTNER)!!
        mLocale = sPreferences!!.getSp(SPreferences.CENTRE)!!
        mCountSum = DbControl.getInstance(this)
            .selectStatisticsSum(mTime, ModeUtil.SAO_CONFIRM, mPartner)
        title_right.text = "${mPartner}\n" +
                "${mLocale}采集\t${mCountSum}\t次"
    }

    private fun scanCode(code: String) {
        //拿到扫码结果提交条形码信息
        subscription = CompositeSubscription()
        loadingDialog.show()
        val map = HashMap<String, String>()
        map["gid"] = "aiIlBmcYnsd97435ALmpw"
        map["sampleCode"] = code
        map["ApplicantDept"] = "${sPreferences!!.getSp(SPreferences.CENTRE)}"
        map["DeviceId"] = DeviceUtil.getDeviceId(this)

        FileTxt.getInstance().writeTxt(code, sPreferences!!.getSp(SPreferences.CENTRE))

        Sampling().getSampling(
            this,
            subscription!!,
            map,
            object : Sampling.SamplingReturn {
                override fun sampling(sampling: Sampling?) {
                    loadingDialog.dismiss()
                    samplingManager(sampling)
                }

                override fun spError(e: Throwable?) {
                    loadingDialog.dismiss()
                }
            })
    }

    /**
     * 处理已采样确认后结果
     */
    private fun samplingManager(sampling: Sampling?) {
        if (sampling?.success == true) {
            FileTxt.getInstance().writeTxt(("确认成功"), sPreferences!!.getSp(SPreferences.CENTRE))

            MyToast.makeS(this, "确认成功")
            sm_content_text.setText("确认成功")
            mCount++
            mCountSum++
            title_right.text = "${mPartner}\n" +
                    "${mLocale}采集\t${mCountSum}\t次"
        } else {
            MyToast.makeS(this, sampling?.msg)
            FileTxt.getInstance().writeTxt(
                (sampling?.msg),
                sPreferences!!.getSp(SPreferences.CENTRE)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(SCAN_ACTION)
        registerReceiver(mScanReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mScanReceiver)
        if (mCount <= 0) {
            return
        }
//        DbControl.getInstance(this).addStatistics(
//            Statistics(
//                System.currentTimeMillis(),
//                mTime,
//                ModeUtil.SAO_CONFIRM,
//                mPartner,
//                mLocale,
//                mCount
//            )
//        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sm != null) {
            sm!!.stopScan()
            sm!!.scanLaserMode = 8
            sm!!.closeScan()
        }
    }
}