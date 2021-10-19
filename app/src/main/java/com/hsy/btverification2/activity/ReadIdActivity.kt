package com.hsy.btverification2.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.IdCardInfo
import com.hsy.btverification2.entity.ModeUtil
import com.hsy.btverification2.entity.Statistics
import com.hsy.btverification2.helpUtil.*
import com.hsy.btverification2.net.MyToast
import com.ivsign.android.IDCReader.IdentityCard
import com.yishu.YSNfcCardReader.NfcCardReader
import com.yishu.util.ByteUtil
import kotlinx.android.synthetic.main.activity_read_id.*
import kotlinx.android.synthetic.main.title_layout.*
import org.json.JSONObject
import rx.subscriptions.CompositeSubscription
import java.lang.ref.WeakReference

class ReadIdActivity : BaseActivity() {

    private var mCount = 0//记录本次采集的数据(准备入库数据)
    private var mCountSum = 0//记录本次采集的数据(显示数据)

    private var sPreferences: SPreferences? = null
    private var mCard: IdentityCard? = null
    private var readIdHelp: ReadIdHelp? = null
    private var mPosHelp: PosHelp? = null
    private var nfcCardReaderAPI: NfcCardReader? = null
    private var mHandler: Handler? = null
    private var isActive = false
    private var thisIntent: Intent? = null
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var mNfcManager: NfcManager? = null
    private var countTag = 0
    private var mPhone = ""
    private var mAddress = ""
    private var mCompany = ""

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

    private var mTime = ""
    private var mPartner = ""
    private var mLocale = ""

    override fun init(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_read_id
    }

    override fun initUI() {
        title_text.text = "身份证录单"
        sPreferences = SPreferences(this, SPreferences.LOGINKEY)
        readIdHelp = ReadIdHelp(this)
        mNfcManager = NfcManager()

        mTime = getDate()
        mPartner = sPreferences!!.getSp(SPreferences.PARTNER)!!
        mLocale = sPreferences!!.getSp(SPreferences.CENTRE)!!
        mCountSum = DbControl.getInstance(this)
            .selectStatisticsSum(mTime, ModeUtil.CARD_ID_SINGLE, mPartner)
        title_right.text = "${mPartner}\n" +
                "${mLocale}采集\t${mCountSum}\t次"

        //初始化并开启读身份证
        returnOpenRead()
        //初始化打印条码
        initPos()
        //提交数据身份证采集数据
        read_submit.setOnClickListener {
//            printBarcode("9876543210", "name")
            restartNFC()//只重启一次就好了
            /**
             * 使用前打开下面的注释
             */
            mPhone = phone_et.text.toString()
            mAddress = address_et.text.toString()
            mCompany = company_et.text.toString()

            if (TextUtils.isEmpty(mPhone)) {
                MyToast.makeS(this@ReadIdActivity, "手机号不能为空")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(mAddress)) {
                MyToast.makeS(this@ReadIdActivity, "地址不能为空")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(mCompany)) {
                MyToast.makeS(this@ReadIdActivity, "公司不能为空")
                return@setOnClickListener
            }
            loadingDialog.show()
            subscription = CompositeSubscription()
            IdCardInfo().getIdCardInfo(
                this,
                subscription!!,
                getSubmitData(),
                object : IdCardInfo.DataReturn {
                    override fun idInfo(idCardInfo: IdCardInfo?) {
                        loadingDialog.dismiss()
                        displayCode(idCardInfo)
                    }

                    override fun idError(e: Throwable?) {
                        loadingDialog.dismiss()
                    }
                })
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
        if (mCard == null) {
            thisIntent = intent
            mHandler!!.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCBUTTON)
        }
    }

    override fun onResume() {
        super.onResume()
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
     * 处理身份证录入信息后结果
     */
    private fun displayCode(idCardInfo: IdCardInfo?) {
        when (idCardInfo?.code) {
            1 -> {
                //打印条码
                printBarcode(idCardInfo.sampleCode, idCardInfo.name)
//                MyToast.makeS(this, "idCardInfo.sampleCode=" + idCardInfo.sampleCode)
                FileTxt.getInstance().writeTxt(
                    (idCardInfo.sampleCode + "," + idCardInfo.code),
                    sPreferences!!.getSp(SPreferences.CENTRE)
                )
                read_code_text.text = idCardInfo.sampleCode
                mCount++
                mCountSum++
                title_right.text = "${mPartner}\n" +
                        "${mLocale}采集\t${mCountSum}\t次"
                clearViewData()
            }
            10, 20 -> {
                readIdHelp?.errorMessageDialog(idCardInfo.msg)
                FileTxt.getInstance().writeTxt(
                    idCardInfo.msg,
                    sPreferences!!.getSp(SPreferences.CENTRE)
                )
            }
//            30 -> {
//                readIdHelp?.errorMessageDialog("请联系客服")
//            }
            else -> {
                readIdHelp?.errorMessageDialog(idCardInfo!!.msg)
                FileTxt.getInstance().writeTxt(
                    idCardInfo!!.msg,
                    sPreferences!!.getSp(SPreferences.CENTRE)
                )
                clearViewData()
            }
        }
    }

    /**
     * 开启身份证扫描
     */
    private fun returnOpenRead() {
        mHandler = MyHandler(this@ReadIdActivity)
        pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this@ReadIdActivity)
        nfcCardReaderAPI = NfcCardReader(mHandler, this@ReadIdActivity)
    }

    /**
     * 初始化打印机
     */
    private fun initPos() {
        mPosHelp = PosHelp(this)
        mPosHelp?.setPosCallback(object : PosHelp.PosCallback {
            override fun restartRead() {

            }
        })
        mPosHelp?.initPos()
    }

    /**
     * 打印条码
     */
    private fun printBarcode(sampleCode: String?, name: String?) {
        mPosHelp?.setPrintStart(sampleCode, name)
        mPosHelp?.printStart()
    }

    /**
     * 整理待提交身份信息
     */
    private fun getSubmitData(): HashMap<String, String> {
        val map = HashMap<String, String>()

        map["gid"] = "aiIlBmcYnsd97435ALmpw"

//        map["Name"] = "何守业"
//        map["Gender"] = "男"
//        map["Birthday"] = "1995-06-03"
//        map["Age"] = "27"
//        map["IDcard"] = "622223199506034114"
//        map["Phone"] = "15372823231"
//        map["Address"] = "上海"
//        map["Company"] = "上肢"

        map["Name"] = mCard!!.nameText
        map["Gender"] = mCard!!.sexText
        map["Birthday"] = mNfcManager!!.getBirthday(mCard!!.birthdayText)
        map["Age"] =
            "${mNfcManager!!.getAgeByDateString(mNfcManager!!.getBirthday(mCard!!.birthdayText))}"
        map["IDcard"] = mCard!!.numberText
        map["Phone"] = mPhone
        map["Address"] = mAddress
        map["Company"] = mCompany

        map["ApplicantDept"] = "${sPreferences!!.getSp(SPreferences.CENTRE)}"
        map["DeviceId"] = DeviceUtil.getDeviceId(this)

        val jsonObject = JSONObject(map as Map<*, *>)
        FileTxt.getInstance()
            .writeTxt(jsonObject.toString(), sPreferences!!.getSp(SPreferences.CENTRE))

        return map
    }

    @SuppressLint("HandlerLeak")
    inner class MyHandler(activity: ReadIdActivity) : Handler() {
        private val activityWeakReference: WeakReference<ReadIdActivity> = WeakReference(activity)

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
                    main_tvContent_show.setText("开始读卡，请稍后...")
                }
                ByteUtil.READ_CARD_FAILED -> {
                    Log.e("ReadIdActivity", "enter READ_CARD_FAILED")
                    var message: String = ""
                    if (78 != nfcCardReaderAPI!!.errorFlag) {
                        message = nfcCardReaderAPI!!.message
                    }
                    restartNFC()//防止第一次读卡失败后NFC读卡无法启动
                    main_tvContent_show.setText("读卡失败：" + msg.obj + "message" + message)
                }
                ByteUtil.READ_CARD_SUCCESS -> {
                    Log.e("ReadIdActivity", "enter READ_CARD_SUCCESS")
                    main_tvContent_show.setText("读卡成功！")
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
        if (nfcAdapter != null) nfcAdapter!!.disableForegroundDispatch(this)
        if (nfcCardReaderAPI != null) {
            nfcCardReaderAPI!!.endNFCListener()
        }
        if (mCount <= 0) {
            return
        }
//        DbControl.getInstance(this).addStatistics(
//            Statistics(
//                System.currentTimeMillis(),
//                mTime,
//                ModeUtil.CARD_ID_SINGLE,
//                mPartner,
//                mLocale,
//                mCount
//            )
//        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mPosHelp?.close()
    }

    /**
     * 把获取到的身份证信息显示出来
     */
    private fun setViewData(card: IdentityCard) {
        mCard = card
        name_et.setText(card.nameText)
        sex_et.setText(card.sexText)
        card_id_et.setText(card.numberText)
        address_et.setText(card.addressText)
    }

    private fun clearViewData() {
        name_et.setText("")
        sex_et.setText("")
        card_id_et.setText("")
        address_et.setText("")
        phone_et.setText("")
        company_et.setText("")
        main_tvContent_show.setText("")
        mCard = null
    }
}