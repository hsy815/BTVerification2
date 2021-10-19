package com.hsy.btverification2.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.hsy.btverification2.MyApplication
import com.hsy.btverification2.R
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.helpUtil.CopyDb
import com.hsy.btverification2.helpUtil.LoginHttpManager
import com.hsy.btverification2.helpUtil.PosHelp
import com.hsy.btverification2.helpUtil.SPreferences
import com.hsy.btverification2.net.MyToast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.version_view
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.item_spinner_layout.view.*
import java.util.*


class LoginActivity : BaseActivity() {

    private val ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTWVUXYZ"

    private var mUser = ""
    private var mCode = ""
    private var mLoginHttpManager: LoginHttpManager? = null
    private var mPosHelp: PosHelp? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private val random = Random()

    override fun init(savedInstanceState: Bundle?) {

    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_login
    }

    override fun initUI() {
        copy_db.setOnClickListener { CopyDb.copyFile() }
        login_set.setOnClickListener {
            mUser = login_user.text.toString()
            getCode()
        }
        login_btn.setOnClickListener {
            if (isUserInfo()) {
                if (MyApplication.instance!!.secretToken != null) {
                    saveUser(mUser, mCode)
                    mLoginHttpManager!!.goOkHttpSendCode(this, mCode)
                } else {
                    token_refresh.visibility = View.VISIBLE
                    MyToast.makeL(this, "请点击右上角刷新按钮后重试")
                }
            }
        }
        token_refresh.setOnClickListener {
            getSecretToken()
        }
        /**
         * 删除n天之前的数据
         */
        DbControl.getInstance(this).deleteStatistics(getDate6())
        mLoginHttpManager = LoginHttpManager(this, loadingDialog)
        mLoginHttpManager!!.setOKSendCode(object : LoginHttpManager.OKSendCode {
            override fun ok() {
                loadingDialog.show()
                mPosHelp?.setPrintStart(mCode, mUser)
                mPosHelp?.printStart()
            }
        })
        version_view.text = getVersion()
        getSecretToken()
    }

    /**
     * 处理未封箱数据
     */
    private fun isSealingBoxCount() {
        val isSealingBoxCount = DbControl.getInstance(this).selectIsSealingBox()
        if (!TextUtils.isEmpty(isSealingBoxCount)) {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("存在未封箱数据是否完成采样并封箱？？？")
                .setPositiveButton("确定") { dialog, _ ->
                    val samplingPerson = DbControl.getInstance(this@LoginActivity)
                        .selectIsSealingBoxSP(isSealingBoxCount!!)
                    saveUser(samplingPerson, isSealingBoxCount)
                    myStartActivity(Intent(this@LoginActivity, ScanActivity::class.java))
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun samplingPerson() {
        arrayAdapter =
            ArrayAdapter<String>(
                this, R.layout.item_spinner_layout,
                DbControl.getInstance(this).selectStatisticsSamplingPerson()
            )
        read_spinner.prompt = "采样人员名称"
        read_spinner.adapter = arrayAdapter
        read_spinner.setSelection(0)
        read_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val partner = view!!.item_spinner_text.text.toString()
                login_user.setText(partner)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    override fun onResume() {
        samplingPerson()
        initPos()
        isSealingBoxCount()
        super.onResume()
    }

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            loadingDialog.dismiss()
            myStartActivity(Intent(this@LoginActivity, ScanActivity::class.java))
            super.handleMessage(msg)
        }
    }


    private fun getCode() {
        mUser = login_user.text.toString()
        if (TextUtils.isEmpty(mUser)) {
            MyToast.makeS(this, "请输入采样人名称")
            return
        }
        var timeStr = System.currentTimeMillis().toString()
        timeStr = timeStr.substring(3, (timeStr.length - 3))
        val codeStr = StringBuffer()
        codeStr.append("M")
        codeStr.append(timeStr)
        codeStr.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        mCode = codeStr.toString()
        login_code.text = mCode
    }

    private fun isUserInfo(): Boolean {
        mUser = login_user.text.toString()
        return when {
            TextUtils.isEmpty(mUser) -> {
                MyToast.makeS(this, "请输入采样人名称")
                false
            }
            TextUtils.isEmpty(mCode) -> {
                MyToast.makeS(this, "请点先生成运输码")
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * 初始化打印机
     */
    private fun initPos() {
        mPosHelp = PosHelp(this)
        mPosHelp?.initPos()
        mPosHelp!!.setHandler(handler, 203)
    }

    /**
     * 存储用户名运输码
     */
    private fun saveUser(user: String?, code: String) {
        val sPreferences = SPreferences(this, SPreferences.LOGINKEY)
        if (!TextUtils.isEmpty(user)) {
            sPreferences.saveSp(SPreferences.USERKEY, user!!)
        }
        sPreferences.saveSp(SPreferences.CODE, code)
    }
}