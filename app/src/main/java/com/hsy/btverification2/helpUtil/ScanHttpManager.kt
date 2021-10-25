package com.hsy.btverification2.helpUtil

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.widget.EditText
import com.google.gson.Gson
import com.hsy.btverification2.db_control.DbControl
import com.hsy.btverification2.entity.*
import com.hsy.btverification2.net.LoadingDialog
import com.hsy.btverification2.net.MyHttpNet
import com.hsy.btverification2.net.MyToast
import com.hsy.btverification2.net.NetWorkUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * @项目名: BTVerification2
 * @类位置: com.hsy.btverification2.helpUtil
 * @创始人: hsy
 * @创建时间: 2021/9/27 9:16
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/9/27 9:16
 * @修改描述:
 */
class ScanHttpManager(
    val context: Context,
    val loadingDialog: LoadingDialog,
    private val mTime: String,
    private val scan_content_u_name: EditText,
    private val scan_content_u_card: EditText,
    private val scan_content_u_group: EditText
) {

    companion object {
        const val USER_INFO_MANAGER = 1
        const val CONFIRM_AND_SUBMIT = 2
        const val FINISHED_BOX = 3
    }

    private var mSubmitStr = ""
    private var mMyHttpDate: MyHttpDate? = null
    private var mFinishedCode: FinishedCode? = null

    /**
     * 获取客户信息
     */
    fun getUserInfo(submitStr: String, mUser: String, mCode: String) {
        mSubmitStr = submitStr
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            MyToast.makeS(context, "老大，没网络呀，去看看您的网络设置吧")
            return
        }
        loadingDialog.show()
        val count = DbControl.getInstance(context)
            .selectStatisticsSubmissionCodeSum(mSubmitStr)
        if (count < 1) {
            DbControl.getInstance(context).addStatistics(
                Statistics(
                    System.currentTimeMillis(),
                    mTime,
                    mUser,
                    submitStr,
                    "",
                    mCode,
                    0,
                    0,
                    "",
                    "",
                    "",
                )
            )
        }
        val isSubmissionStatus = DbControl.getInstance(context)
            .selectStatisticsSubmissionCodeIs(mSubmitStr)
        if (1 == isSubmissionStatus) {
            val selectStatistics = DbControl.getInstance(context)
                .selectStatistics(mSubmitStr)
            var dateTime = ""
            var dateName = ""
            if (selectStatistics != null) {
                dateTime = getStr2Date(selectStatistics.date)
                dateName = selectStatistics.userName
            }
            AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(
                    "客户:$dateName \n" +
                            "订单号:$mSubmitStr \n" +
                            "采样时间:$dateTime \n" +
                            "\n" +
                            "已完成采样并打码，是否继续采样打码？？？"
                )
                .setPositiveButton("确定") { dialog, _ ->
                    MyHttpNet.goOkHttpUserInfo(submitStr, mHandler)
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                    loadingDialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } else {
            MyHttpNet.goOkHttpUserInfo(submitStr, mHandler)
        }
    }

    /**
     * 确认提交并打印
     */
    fun confirmAndSubmit(submitStr: String, code: String) {
        loadingDialog.show()
        mSubmitStr = submitStr
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            MyToast.makeS(context, "老大，没网络呀，去看看您的网络设置吧")
            return
        }
        MyHttpNet.goOkHttpConfirmAndSubmit(submitStr, code, mHandler)
    }

    /**
     * 发起封箱
     */
    fun finishedBox(code: String) {
        loadingDialog.show()
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            MyToast.makeS(context, "老大，没网络呀，去看看您的网络设置吧")
            return
        }
        MyHttpNet.goOkHttpFinishedBox(code, mHandler)
    }

    private val mHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            loadingDialog.dismiss()
            when (msg.what) {
                USER_INFO_MANAGER -> {
                    myUserManager(msg)
                }
                CONFIRM_AND_SUBMIT -> {
                    mPrintCode(msg)
                }
                FINISHED_BOX -> {
                    mFinishedBox(msg)
                }
            }
            super.handleMessage(msg)
        }
    }

    /**
     * 获取回来的客户信息处理
     */
    private fun myUserManager(msg: Message) {
        val gson = Gson()
        try {
            val httpNetError =
                gson.fromJson(msg.obj.toString(), HttpNetError::class.java)
            if (httpNetError != null) {
                if (!TextUtils.isEmpty(httpNetError.ResultMsg)) {
                    MyToast.makeL(context, httpNetError.ResultMsg)
                    return
                }
            }
            val scanUserInfo =
                gson.fromJson(msg.obj.toString(), ScanUserInfo::class.java)
            if (scanUserInfo != null) {
                if (!TextUtils.isEmpty(scanUserInfo.LockProfileMainObjIDNo)) {
                    scan_content_u_name.setText(scanUserInfo.LockProfileMainObjName)
                    scan_content_u_card.setText(scanUserInfo.LockProfileMainObjIDNo)
                    scan_content_u_group.setText(scanUserInfo.GroupSourceObjName)

                    DbControl.getInstance(context).upDateStatisticsUserInfo(
                        mSubmitStr,
                        scanUserInfo.LockProfileMainObjName,
                        scanUserInfo.LockProfileMainObjIDNo,
                        scanUserInfo.GroupSourceObjName
                    )
                }
            }
        } catch (e: Exception) {
            MyToast.makeL(context, msg.obj.toString())
        }
    }

    /**
     * 处理确认采样并打码返回信息
     */
    private fun mPrintCode(msg: Message) {
        val gson = Gson()
        try {
            val httpNetError =
                gson.fromJson(msg.obj.toString(), HttpNetError::class.java)
            if (httpNetError != null) {
                if (!TextUtils.isEmpty(httpNetError.ResultMsg)) {
                    MyToast.makeL(context, httpNetError.ResultMsg)
                    return
                }
            }
            val httpNetData = gson.fromJson(msg.obj.toString(), HttpNetData::class.java)
            if (httpNetData != null) {
                if (!TextUtils.isEmpty(httpNetData.PrintCode)) {
                    if (mMyHttpDate != null) mMyHttpDate!!.ok(httpNetData)
                }
            }
        } catch (e: Exception) {
            MyToast.makeL(context, msg.obj.toString())
        }
    }

    /**
     * 处理封箱请求结果
     */
    private fun mFinishedBox(msg: Message) {
        val gson = Gson()
        try {
            val httpNetError =
                gson.fromJson(msg.obj.toString(), HttpNetError::class.java)
            if (httpNetError != null) {
                if (!TextUtils.isEmpty(httpNetError.ResultMsg)) {
                    MyToast.makeL(context, httpNetError.ResultMsg)
                    return
                }
            }
            val sendNetDate = gson.fromJson(msg.obj.toString(), SendNetDate::class.java)

            if (sendNetDate != null) {
                if (!TextUtils.isEmpty(sendNetDate.Code)) {
                    if (mFinishedCode != null) mFinishedCode!!.ok(sendNetDate)
                }
            }
        } catch (e: Exception) {
            MyToast.makeL(context, msg.obj.toString())
        }
    }

    interface MyHttpDate {
        fun ok(httpNetData: HttpNetData)
    }

    /**
     * 确认提交并打码
     */
    fun setMyHttpDate(myHttpDate: MyHttpDate) {
        this.mMyHttpDate = myHttpDate
    }

    interface FinishedCode {
        fun ok(sendNetDate: SendNetDate)
    }

    /**
     * 封箱
     */
    fun setFinishedCode(finishedCode: FinishedCode) {
        this.mFinishedCode = finishedCode
    }

    @SuppressLint("SimpleDateFormat")
    fun getStr2Date(str: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
        val date = Date(str)
        return simpleDateFormat.format(date)
    }
}