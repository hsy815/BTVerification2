package com.hsy.btverification2.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.ModeUtil
import com.hsy.btverification2.helpUtil.DynamicPermission
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.version_view
import kotlinx.android.synthetic.main.title_layout.*

class MainActivity : BaseActivity() {

    private var dynamicPermission: DynamicPermission? = null
    private var mPartner = ""
    private var mDate = ""
    private var isNfc = false

    override fun init(savedInstanceState: Bundle?) {
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_main
    }

    override fun initUI() {
        title_text.text = "宝藤新冠外采系统"

        dynamicPermission =
            DynamicPermission(this@MainActivity) {
                //权限回调处理，我们这里貌似不需要
            }
        dynamicPermission!!.getPermissionStart()

        val manager: NfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter: NfcAdapter? = manager.defaultAdapter
        isNfc = adapter != null && adapter.isEnabled

        id_btn.setOnClickListener {
//            startActivity(Intent(MainActivity@ this, PrintPhotoActivity::class.java))
            startActivity(Intent(MainActivity@ this, ReadIdActivity::class.java))
        }
        id_btn_mix.setOnClickListener {
            startActivity(Intent(MainActivity@ this, ReadMixIdActivity::class.java))
        }
        scan_btn.setOnClickListener {
            startActivity(Intent(MainActivity@ this, ScanActivity::class.java))
        }
        scan_after_btn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("此功能不验证用户是否已付款！！！")
                .setPositiveButton("确定") { dialog, _ ->
                    startActivity(Intent(MainActivity@ this, ScanAfterActivity::class.java))
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        sampling_btn.setOnClickListener {
            startActivity(
                Intent(
                    MainActivity@ this,
                    SamplingConfirmationActivity::class.java
                )
            )
        }

        val version = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_CONFIGURATIONS
        ).versionName
        version_view.text = version
    }

    override fun onResume() {
        super.onResume()
        if (isNfc) {
            id_btn.visibility = View.VISIBLE
            id_btn_mix.visibility = View.VISIBLE
//            readIdBtnText()
//            readMixIdBtnText()
        } else {
            id_btn.visibility = View.GONE
            id_btn_mix.visibility = View.GONE
        }
//        saoNoBtnText()
//        saoBtnText()
    }

    private fun readIdBtnText() {
        val mRid = DbControl.getInstance(this)
            .selectStatisticsSum(mDate, ModeUtil.CARD_ID_SINGLE, mPartner)
        val rIdText = appendBtnText("身份证录入", mRid)
        val rId = SpannableString(rIdText)
        rId.setSpan(
            AbsoluteSizeSpan(16, true),
            5,
            rIdText.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        id_btn.text = rId
    }

    private fun readMixIdBtnText() {
        val mRidMix = DbControl.getInstance(this)
            .selectStatisticsSum(mDate, ModeUtil.CARD_ID_MANY, mPartner)
        val mPCount = DbControl.getInstance(this)
            .selectGroupCardSum(mDate)

        val rIdMixText = appendBtnText("身份证录入(混采)", mRidMix, mPCount)
        val rIdMix = SpannableString(rIdMixText)
        rIdMix.setSpan(
            AbsoluteSizeSpan(16, true),
            9,
            rIdMixText.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        id_btn_mix.text = rIdMix
    }

    private fun saoNoBtnText() {
        val mSaoNo = DbControl.getInstance(this)
            .selectStatisticsSum(mDate, ModeUtil.SAO_NO_VERIFICATION, mPartner)
        val saoNoText = appendBtnText("扫码并采样", mSaoNo)
        val sanNo = SpannableString(saoNoText)
        sanNo.setSpan(
            AbsoluteSizeSpan(16, true),
            5,
            saoNoText.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        scan_btn.text = sanNo
    }

    private fun saoBtnText() {
        val mSao = DbControl.getInstance(this)
            .selectStatisticsSum(mDate, ModeUtil.SAO_VERIFICATION, mPartner)
        val saoNoText = appendBtnText("付款后采样", mSao)
        val sanNo = SpannableString(saoNoText)
        sanNo.setSpan(
            AbsoluteSizeSpan(16, true),
            5,
            saoNoText.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        scan_after_btn.text = sanNo
    }

    private fun appendBtnText(str: String, count: Int): StringBuilder {
        val saoText = StringBuilder(str)
        saoText.append("\n")
        saoText.append("\n")
        saoText.append("(今天采集次数：")
        saoText.append(count)
        saoText.append("次)")
        return saoText
    }

    private fun appendBtnText(str: String, count: Int, pCount: Int): StringBuilder {
        val idText = StringBuilder(str)
        idText.append("\n")
        idText.append("\n")
        idText.append("(今天采集次数：")
        idText.append(count)
        idText.append("次，共：")
        idText.append(pCount)
        idText.append("人)")
        return idText
    }
}