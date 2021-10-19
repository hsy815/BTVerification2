package com.hsy.btverification2.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanDevice
import android.nfc.NfcAdapter
import android.nfc.tech.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.hsy.btverification2.R
import com.hsy.btverification2.adapter.ReadMixAdapter
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.ModeUtil
import com.hsy.btverification2.entity.ReadMix
import com.hsy.btverification2.entity.ReadMixUser
import com.hsy.btverification2.entity.Statistics
import com.hsy.btverification2.helpUtil.*
import com.hsy.btverification2.net.MyToast
import com.ivsign.android.IDCReader.IdentityCard
import com.yishu.YSNfcCardReader.NfcCardReader
import com.yishu.util.ByteUtil
import kotlinx.android.synthetic.main.activity_read_id.*
import kotlinx.android.synthetic.main.activity_read_mix_id.*
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.item_spinner_layout.view.*
import kotlinx.android.synthetic.main.title_layout.*
import org.json.JSONObject
import rx.subscriptions.CompositeSubscription
import java.lang.ref.WeakReference

class ReadMixIdActivity : BaseActivity() {

    private var mCountId = 10//混采一管多少人
    private var mCount = 0//记录本次采集的数据(准备入库数据)
    private var mCountSum = 0//记录本次采集的数据(显示数据)

    private var readIdHelp: ReadIdHelp? = null
    private var sPreferences: SPreferences? = null
    private var mHandler: Handler? = null
    private var pendingIntent: PendingIntent? = null
    private var nfcAdapter: NfcAdapter? = null
    private var nfcCardReaderAPI: NfcCardReader? = null
    private var isActive = false
    private var thisIntent: Intent? = null
    private var countTag = 0
    private var mCardList: ArrayList<IdentityCard>? = null
    private var mNfcManager: NfcManager? = null

    private var sm: ScanDevice? = null
    private val SCAN_ACTION = "scan.rcv.message"
    private var smCode: String? = null
    private val mScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barocode = intent.getByteArrayExtra("barocode")
            val barocodelen = intent.getIntExtra("length", 0)
            val temp = intent.getByteExtra("barcodeType", 0.toByte())
            val aimid = intent.getByteArrayExtra("aimid")
            val barcodeStr = String(barocode!!, 0, barocodelen)
            get1q(barcodeStr)
            sm?.stopScan()
        }
    }

    companion object {
        private var TECHLISTS: Array<Array<String>>? = null
        private var FILTERS: Array<IntentFilter>? = null

        init {
            try {
                TECHLISTS = arrayOf(
                    arrayOf(IsoDep::class.java.name), arrayOf(
                        NfcV::class.java.name
                    ), arrayOf(NfcF::class.java.name), arrayOf(
                        NfcA::class.java.name
                    ), arrayOf(NfcB::class.java.name), arrayOf<String>(
                        NdefFormatable::class.java.name,
                        MifareClassic::class.java.name
                    )
                )
                FILTERS = arrayOf<IntentFilter>(
                    IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")
                )
            } catch (e: Exception) {
            }
        }
    }

    private var mReadMixAdapter: ReadMixAdapter? = null
    private var mReadMixList: ArrayList<ReadMixUser>? = null

    private var mTime = ""
    private var mPartner = ""
    private var mLocale = ""

    override fun init(savedInstanceState: Bundle?) {
        sm = ScanDevice()
        sm!!.openScan()
        sm!!.outScanMode = 0 //启动就是广播模式
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_read_mix_id
    }

    override fun initUI() {
        title_text.text = "身份证录单"
        sPreferences = SPreferences(this, SPreferences.LOGINKEY)
        readIdHelp = ReadIdHelp(this)
        mTime = getDate()
        mPartner = sPreferences!!.getSp(SPreferences.PARTNER)!!
        mLocale = sPreferences!!.getSp(SPreferences.CENTRE)!!
        mCountSum = DbControl.getInstance(this)
            .selectStatisticsSum(mTime, ModeUtil.CARD_ID_MANY, mPartner)
        title_right.text = "${mPartner}\n" +
                "${mLocale}采集\t${mCountSum}\t次"

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        read_mix_cyc.layoutManager = layoutManager
        mReadMixList = ArrayList<ReadMixUser>()
        mReadMixAdapter = ReadMixAdapter(this@ReadMixIdActivity, mReadMixList!!)
        read_mix_cyc.adapter = mReadMixAdapter

        mNfcManager = NfcManager()
        //初始化并开启读身份证
        returnOpenRead()

        read_mix_sub.setOnClickListener {
            //需要判断不能为null
            if (TextUtils.isEmpty(smCode)) {
                MyToast.makeS(this@ReadMixIdActivity, "你还没有扫码呢，别乱点···")
                return@setOnClickListener
            } else if (mCardList == null || mCardList!!.size == 0) {
                MyToast.makeS(this@ReadMixIdActivity, "你需要至少录入一个身份信息")
                return@setOnClickListener
            }
            sub()
        }
    }

    /**
     * 提交发送数据
     */
    private fun sub() {

        loadingDialog.show()
        subscription = CompositeSubscription()
        ReadMix().getReadMix(this,
            subscription!!,
            getSubData(),
            object : ReadMix.ReadMixReturn {
                override fun readMix(readMix: ReadMix?) {
                    loadingDialog.dismiss()
                    displayMix(readMix)
                }

                override fun spError(e: Throwable?) {
                    loadingDialog.dismiss()
                }
            })
    }

    /**
     * 处理数据提交反馈
     */
    private fun displayMix(readMix: ReadMix?) {
        if (readMix == null) {
            readIdHelp!!.errorMessageDialog("我也不知道服务器怎么了")
            return
        }
        if (readMix.success) {
            //每提交一组，记录一次本组提交数量
//            DbControl.getInstance(this).addReadGroup(
//                System.currentTimeMillis(),
//                getDate(),
//                mReadMixList!!.size
//            )
            clearViewData()
            MyToast.makeS(this@ReadMixIdActivity, "上传成功")
            mCount++
            mCountSum++
            title_right.text = "${mPartner}\n" +
                    "${mLocale}采集\t${mCountSum}\t次"
        } else {
            readIdHelp!!.errorMessageDialog(readMix.msg)
        }
    }

    /**
     * 重启NFC读取
     */
    private fun restartNFC() {
        if (countTag == 0) {
            mHandler!!.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCSTART)
            countTag = 1
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (TextUtils.isEmpty(smCode)) {
            MyToast.makeS(this@ReadMixIdActivity, "你还没有扫码呢···")
            restartNFC()//只重启一次就好了
            return
        }
        if (mCardList == null || mCardList!!.size < (mCountId + 1)) {
            thisIntent = intent
            mHandler!!.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCBUTTON)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(SCAN_ACTION)
        registerReceiver(mScanReceiver, filter)

        if (nfcAdapter != null) {
            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, FILTERS, TECHLISTS)
        }
        isActive = true
        //打开nfc前台调度系统
        nfcCardReaderAPI!!.startNFCListener()
        val action = intent.action
        if ("android.nfc.action.TECH_DISCOVERED" == action) {
            if (thisIntent == null) {
                mHandler!!.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCSTART)
                thisIntent = intent
                mHandler!!.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCBUTTON)
            }
        }
    }

    /**
     * 开启身份证扫描
     */
    private fun returnOpenRead() {
        mHandler = MyHandler(this@ReadMixIdActivity)
        pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this@ReadMixIdActivity)
        nfcCardReaderAPI = NfcCardReader(mHandler, this@ReadMixIdActivity)
    }

    @SuppressLint("HandlerLeak")
    inner class MyHandler(activity: ReadMixIdActivity) : Handler() {
        private val activityWeakReference: WeakReference<ReadMixIdActivity> =
            WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = activityWeakReference.get() ?: return
            when (msg.what) {
                ByteUtil.MESSAGE_VALID_NFCSTART -> {
                    Log.e("ReadIdActivity", "enter MESSAGE_VALID_NFCSTART")
                    var enabledNFC = false
                    if (activity.isActive) {
                        enabledNFC = activity.nfcCardReaderAPI!!.enabledNFCMessage()
                    }
                    if (enabledNFC) {
//                        Toast.makeText(activity, "NFC初始化成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "NFC初始化失败", Toast.LENGTH_SHORT).show()
                    }
                }
                ByteUtil.MESSAGE_VALID_NFCBUTTON -> {
                    Log.e("ReadIdActivity", "enter MESSAGE_VALID_NFCBUTTON")
                    val isNFC: Boolean =
                        nfcCardReaderAPI!!.isNFC(thisIntent)
                    if (isNFC) {
                        nfcCardReaderAPI!!.CreateCard(thisIntent)
                    } else {
                        Toast.makeText(activity, "获取nfc失败", Toast.LENGTH_SHORT).show()
                    }
                    thisIntent = null
                }
                ByteUtil.READ_CARD_START -> {
                    Log.e("ReadIdActivity", "enter READ_CARD_START")
                    read_mix_show.text = "开始读卡，请稍后..."
                }
                ByteUtil.READ_CARD_FAILED -> {
                    Log.e("ReadIdActivity", "enter READ_CARD_FAILED")
                    var message: String = ""
                    if (78 != nfcCardReaderAPI!!.errorFlag) {
                        message = nfcCardReaderAPI!!.message
                    }
                    restartNFC()//防止第一次读卡失败后NFC读卡无法启动
                    read_mix_show.text = "读卡失败：${msg.obj}message：${message}"
                }
                ByteUtil.READ_CARD_SUCCESS -> {
                    Log.e("ReadIdActivity", "enter READ_CARD_SUCCESS")
                    read_mix_show.text = "读卡成功！"
                    val card = msg.obj as IdentityCard
                    setViewData(card)
                }
                else -> {
                    Log.e("ReadIdActivity", "不知道读身份证发生了啥")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mScanReceiver)

        if (nfcAdapter != null) nfcAdapter!!.disableForegroundDispatch(this)
        if (nfcCardReaderAPI != null) {
            nfcCardReaderAPI!!.endNFCListener()
        }

        //防止没任何操作也提交一次
        if (mCount <= 0) {
            return
        }
        //记录本次数据提交次数
//        DbControl.getInstance(this).addStatistics(
//            Statistics(
//                System.currentTimeMillis(),
//                mTime,
//                ModeUtil.CARD_ID_MANY,
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

    private fun getSubData(): HashMap<String, String> {
        val map = HashMap<String, String>()

//        map["Name"] = "何守业"
//        map["Gender"] = "男"
//        map["Birthday"] = "1995-06-03"
//        map["Age"] = "27"
//        map["IDcard"] = "622223199506034114"
//        map["Phone"] = "15372823231"
//        map["Address"] = "上海"
//        map["Company"] = "上肢"

        map["gid"] = "xgyyjcpdgaSIOWLkdslwiAOkl139701"
        map["dept"] = "手持机"
        map["partner"] = "mPartner"//等下获取
        map["DeviceId"] = DeviceUtil.getDeviceId(this)
        val gsonList = Gson().toJson(mReadMixList)
        map["data"] = gsonList

        val jsonObject = JSONObject(map as Map<*, *>)
        FileTxt.getInstance()
            .writeTxt(jsonObject.toString(), sPreferences!!.getSp(SPreferences.CENTRE))

        return map
    }

    /**
     * 处理扫码数据
     */
    private fun get1q(submitStr: String) {
        //要想确保数据提交了才能clear
        if (mCardList == null) {
            smCode = submitStr
            read_mix_sm.text = submitStr
        } else {
            //数据未提交弹窗
            readIdHelp!!.errorMessageDialog("请先提交数据后再扫码！！！")
        }
    }

    /**
     * 把获取到的身份证信息显示出来
     */
    private fun setViewData(card: IdentityCard) {
        restartNFC()
        if (mCardList == null) {
            mCardList = ArrayList<IdentityCard>()
        }
        when {
            mCardList!!.size < (mCountId - 1) -> {
                mCardList!!.add(card)
                mReadMixAdapter!!.setReadMix(
                    ReadMixUser(
                        card.nameText,
                        card.numberText,
                        card.sexText,
                        mNfcManager!!.getBirthday(card.birthdayText),
                        "${mNfcManager!!.getAgeByDateString(mNfcManager!!.getBirthday(card.birthdayText))}",
                        2,
                        "${sPreferences!!.getSp(SPreferences.CENTRE)}",
                        smCode!!
                    )
                )
            }
            mCardList!!.size == (mCountId - 1) -> {
                mCardList!!.add(card)
                mReadMixAdapter!!.setReadMix(
                    ReadMixUser(
                        card.nameText,
                        card.numberText,
                        card.sexText,
                        mNfcManager!!.getBirthday(card.birthdayText),
                        "${mNfcManager!!.getAgeByDateString(mNfcManager!!.getBirthday(card.birthdayText))}",
                        2,
                        "${sPreferences!!.getSp(SPreferences.CENTRE)}",
                        smCode!!
                    )
                )
                //弹窗提交换管子
                readIdHelp!!.errorMessageDialog("已达上限，请提交数据！！！")
            }
            else -> {
                //弹窗提交换管子
                readIdHelp!!.errorMessageDialog("已达上限，请先提交数据然后取新管子并扫码！！！")
            }
        }

    }

    private fun clearViewData() {
        read_mix_sm.text = ""
        mReadMixAdapter!!.deleteList()
        mCardList = null
//        mCount = 0
        smCode = null
    }


}