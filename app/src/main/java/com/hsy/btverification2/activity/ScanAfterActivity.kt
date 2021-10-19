package com.hsy.btverification2.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanDevice
import android.os.Bundle
import android.text.TextUtils
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.ModeUtil
import com.hsy.btverification2.entity.ScanCodeInfo
import com.hsy.btverification2.entity.Statistics
import com.hsy.btverification2.helpUtil.DeviceUtil
import com.hsy.btverification2.helpUtil.FileTxt
import com.hsy.btverification2.helpUtil.PosHelp
import com.hsy.btverification2.helpUtil.SelfScanDialog
import com.hsy.btverification2.net.MyToast
import kotlinx.android.synthetic.main.activity_scan.scan_content_text
import kotlinx.android.synthetic.main.activity_scan.scan_content_text2
import kotlinx.android.synthetic.main.activity_scan_after.*
import kotlinx.android.synthetic.main.title_layout.*
import rx.subscriptions.CompositeSubscription

class ScanAfterActivity : BaseActivity() {

    private var mCount = 0//记录本次采集的数据(准备入库数据)
    private var mCountSum = 0//记录本次采集的数据(显示数据)
    private var mTime = ""
    private var mPartner = ""

    private var selfScanDialog: SelfScanDialog? = null
    private var sm: ScanDevice? = null
    private val SCAN_ACTION = "scan.rcv.message"
    private var mPosHelp: PosHelp? = null
    private val mScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barocode = intent.getByteArrayExtra("barocode")
            val barocodelen = intent.getIntExtra("length", 0)
            val temp = intent.getByteExtra("barcodeType", 0.toByte())
            val aimid = intent.getByteArrayExtra("aimid")
            val barcodeStr = String(barocode!!, 0, barocodelen)
            if (TextUtils.isEmpty(mPartner)) {
                mPartner = barcodeStr
                selfScanDialog!!.setEd(barcodeStr)
            } else {
                scan_content_text.setText(barcodeStr)
                get1q(barcodeStr)
            }
            sm?.stopScan()
        }
    }


    override fun init(savedInstanceState: Bundle?) {
        sm = ScanDevice()
        sm!!.openScan()
        sm!!.outScanMode = 0 //启动就是广播模式
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_scan_after
    }

    override fun initUI() {
        title_text.text = "团体采样"
        mTime = getDate()
        mCountSum = DbControl.getInstance(this)
            .selectStatisticsSum(mTime, ModeUtil.SAO_VERIFICATION, mPartner)
        title_right.text = "${mPartner}\n" +
                "采集\t${mCountSum}\t次"
        initPos()
        showDialog()
    }

    private fun showDialog() {
        selfScanDialog = SelfScanDialog(this)
        selfScanDialog!!.show()
        selfScanDialog!!.setNoOnClickListener(object : SelfScanDialog.NoOnClickListener {
            override fun onNoClick() {
                selfScanDialog!!.dismiss()
                finish()
            }
        })
        selfScanDialog!!.setYesOnClickListener(object : SelfScanDialog.YesOnClickListener {
            override fun onYesClick(str: String) {
                scan_partner_text.setText(mPartner)
                selfScanDialog!!.dismiss()
            }
        })
    }

    private fun get1q(submitStr: String) {
        loadingDialog.show()
        val map = HashMap<String, String>()
        map["gid"] = "aiIlBmcYnsd97435ALmpw"
        map["code"] = submitStr
        map["isNeedPayed"] = "true"
        map["ApplicantDept"] = mPartner
        map["DeviceId"] = DeviceUtil.getDeviceId(this)

        FileTxt.getInstance().writeTxt(submitStr, mPartner)

        subscription = CompositeSubscription()
//        ScanCodeInfo().getIdCardInfo(
//            this,
//            subscription!!,
//            map,
//            object : ScanCodeInfo.DataReturn {
//                override fun idInfo(scanCodeInfo: ScanCodeInfo?) {
//                    loadingDialog.dismiss()
//                    samplingManager(scanCodeInfo)
//                }
//
//                override fun idError(e: Throwable?) {
//                    loadingDialog.dismiss()
//                }
//            })
    }

    /**
     * 处理扫码后结果
     */
    private fun samplingManager(scanCodeInfo: ScanCodeInfo?) {
        if (scanCodeInfo?.success == true) {
            scan_content_text2.setText(scanCodeInfo.sampleCode)
            FileTxt.getInstance().writeTxt(
                (scanCodeInfo.sampleCode + "," + scanCodeInfo.name),
                mPartner
            )

            //扫码成功，开启打印
            mPosHelp?.setPrintStart(scanCodeInfo.sampleCode, scanCodeInfo.name)
            mPosHelp?.setPosCallback(object : PosHelp.PosCallback {
                override fun restartRead() {
//                    scan_content_text.setText("")
//                    scan_content_text2.setText("")
                }
            })
            mPosHelp?.printStart()

            mCount++
            mCountSum++
            title_right.text = "${mPartner}\n" +
                    "采集\t${mCountSum}\t次"
        } else {
            MyToast.makeL(this, scanCodeInfo?.msg)
            FileTxt.getInstance().writeTxt(
                (scanCodeInfo?.msg),
                mPartner
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
//                ModeUtil.SAO_VERIFICATION,
//                mPartner,
//                "",
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

    /**
     * 初始化打印机
     */
    private fun initPos() {
        mPosHelp = PosHelp(this)
        mPosHelp?.initPos()
    }

}