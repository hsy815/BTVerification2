package com.hsy.btverification2.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.hsy.btverification2.MyApplication
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.*
import com.hsy.btverification2.helpUtil.*
import com.hsy.btverification2.net.MyToast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.activity_scan.version_view
import kotlinx.android.synthetic.main.title_layout.*
import java.util.*


class ScanActivity : BaseActivity() {

    companion object {
        const val SCAN_BTN = 201
        const val SCAN_COUNT = 202
    }

    private var mCountSum = 0//记录这个采样人今日采集的数据次数(显示数据)
    private var mCountBoxSum = 0//记录这个箱子里的个数
    private var mSubmitStr = ""//订单号
    private var sm: ScanDevice? = null
    private val SCAN_ACTION = "scan.rcv.message"
    private var mPosHelp: PosHelp? = null
    private var mPrintCode = ""//打印编号
    private val mScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barocode = intent.getByteArrayExtra("barocode")
            val barocodelen = intent.getIntExtra("length", 0)
            val temp = intent.getByteExtra("barcodeType", 0.toByte())
            val aimid = intent.getByteArrayExtra("aimid")
            mSubmitStr = String(barocode!!, 0, barocodelen)
            scan_content_text.hint = mSubmitStr
            scan_content_text2.hint = ""
            getUserInfo()
            sm?.stopScan()
        }
    }
    private var scanHttpManager: ScanHttpManager? = null
    private var mTime = ""//当前日期
    private var mUser = ""//采样人名
    private var mCode = ""//运输码

    override fun init(savedInstanceState: Bundle?) {
        sm = ScanDevice()
        sm!!.openScan()
        sm!!.outScanMode = 0 //启动就是广播模式
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_scan
    }

    @SuppressLint("SetTextI18n")
    override fun initUI() {
        val sPreferences = SPreferences(this, SPreferences.LOGINKEY)
        mUser = sPreferences.getSp(SPreferences.USERKEY).toString()
        mCode = sPreferences.getSp(SPreferences.CODE).toString()
        sampling_user.text = "采样人:\t$mUser"
        sampling_code.text = "运输码:\t$mCode"
        mTime = getDate()
        mCountSum = DbControl.getInstance(this)
            .selectStatisticsSum(mTime, mUser)
        mCountBoxSum = DbControl.getInstance(this).selectStatisticsBoxSum(mCode)
        sampling_count.text = "今日采集\t${mCountSum}\t次"
        sampling_count_box.text = "本箱放入\t${mCountBoxSum}\t个"
        scan_token_refresh.visibility = View.GONE

        sampling_p.setOnClickListener {
            //打印运输码
            loadingDialog.show()
            mPosHelp?.setPrintStart(mCode, mUser)
            mPosHelp?.setHandler(handler, SCAN_BTN)
            mPosHelp?.printStart()
        }
        scan_token_refresh.setOnClickListener {
            getSecretToken()//刷新获取token
        }
        scan_end_bt.setOnClickListener {
            sealingBox()//封箱提交
        }
        scan_p_bt.setOnClickListener {
            //确认并提交采样
            confirmAndSubmit()
        }
        version_view.text = getVersion()
        initPos()
        initManager()
    }

    /**
     * 发送扫码信息获取客户信息
     */
    private fun getUserInfo() {
        if (MyApplication.instance!!.secretToken != null) {
            scanHttpManager!!.getUserInfo(mSubmitStr, mUser, mCode)
        } else {
            scan_token_refresh.visibility = View.VISIBLE
            MyToast.makeL(this, "请检查网络点击右上角刷新按钮后重试")
        }
    }

    /**
     * 确认并提交
     */
    private fun confirmAndSubmit() {
        if (TextUtils.isEmpty(scan_content_u_card.text.toString())) {
            MyToast.makeS(this, "请先扫码获取客户信息")
            return
        }

        if (MyApplication.instance!!.secretToken != null) {
            scanHttpManager!!.setMyHttpDate(object : ScanHttpManager.MyHttpDate {
                override fun ok(httpNetData: HttpNetData) {
                    val mLockProfileMainObj = httpNetData.LockProfileMainObj.Name
                    scan_content_text2.hint = httpNetData.PrintCode
                    mPrintCode = httpNetData.PrintCode
                    //扫码成功，开启打印
                    loadingDialog.show()
                    mPosHelp!!.setPrintStart(httpNetData.PrintCode, mLockProfileMainObj)
                    mPosHelp!!.setHandler(handler, SCAN_COUNT)
                    mPosHelp!!.printStart()
                }
            })
            scanHttpManager!!.confirmAndSubmit(mSubmitStr, mCode)
        } else {
            scan_token_refresh.visibility = View.VISIBLE
            MyToast.makeL(this, "请检查网络点击右上角刷新按钮后重试")
        }
    }

    /**
     * 结束采样 封箱
     */
    private fun sealingBox() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确定已完成采样要封箱吗？？？")
            .setPositiveButton("确定") { dialog, _ ->
                val countSum = DbControl.getInstance(this).selectStatisticsBoxSum(mCode)
                if (countSum > 0) {
                    scanHttpManager!!.setFinishedCode(object : ScanHttpManager.FinishedCode {
                        override fun ok(sendNetDate: SendNetDate) {
                            DbControl.getInstance(this@ScanActivity)
                                .upDateStatisticsSealingBox(mCode)
                            finish()
                        }
                    })
                    scanHttpManager!!.finishedBox(mCode)
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 消除打印按钮
     * 打印程序是异步的，需要异步通知修改UI
     */
    private val handler = object : Handler(Looper.myLooper()!!) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SCAN_BTN -> {
                    loadingDialog.dismiss()
                    sampling_p.visibility = View.GONE
                }
                SCAN_COUNT -> {
                    loadingDialog.dismiss()
                    val count = DbControl.getInstance(this@ScanActivity)
                        .selectStatisticsSubmissionCodeIs(mSubmitStr)
                    if (0 == count) {
                        mCountSum++
                        mCountBoxSum++
                        DbControl.getInstance(this@ScanActivity)
                            .upDateStatisticsSubmission(mSubmitStr, mPrintCode)
                        sampling_count.text = "今日采集\t${mCountSum}\t次"
                        sampling_count_box.text = "本箱放入\t${mCountBoxSum}\t个"
                    }
                }
            }
            super.handleMessage(msg)
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
     * 初始化扫码请求管理器
     */
    private fun initManager() {
        scanHttpManager = ScanHttpManager(
            this@ScanActivity,
            loadingDialog,
            mTime,
            scan_content_u_name,
            scan_content_u_card,
            scan_content_u_group
        )
    }

    /**
     * 初始化打印机
     */
    private fun initPos() {
        mPosHelp = PosHelp(this)
        mPosHelp?.initPos()
    }

    override fun onBackPressed() {
        val countSum = DbControl.getInstance(this).selectStatisticsBoxSum(mCode)
        if (countSum < 1) {
            super.onBackPressed()
        } else {
            MyToast.makeL(this, "请点击”结束采样“确认本次采样信息")
        }
    }

}