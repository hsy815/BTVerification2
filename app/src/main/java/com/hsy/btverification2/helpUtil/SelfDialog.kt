package com.hsy.btverification2.helpUtil

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import com.hsy.btverification2.R
import com.hsy.btverification2.net.MyToast

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.helpUtil
 * @创始人: hsy
 * @创建时间: 2021/8/26 15:39
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 15:39
 * @修改描述:
 */
class SelfDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    private var partnerEd: EditText? = null
    private var partnerOk: TextView? = null
    private var partnerNo: TextView? = null
    private var yesOnClickListener: YesOnClickListener? = null//确定按钮被点击了的监听器
    private var noOnClickListener: NoOnClickListener? = null//取消按钮被点击了的监听器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_partner_layout)
        setCanceledOnTouchOutside(false)
        initView()
        initEvent()
    }

    private fun initEvent() {
        partnerOk!!.setOnClickListener {
            if (yesOnClickListener != null) {
                val str = partnerEd!!.text.toString()
                if (TextUtils.isEmpty(str)){
                    MyToast.makeS(context,"请先输入合作单位")
                    return@setOnClickListener
                }
                yesOnClickListener!!.onYesClick(str)
            }
        }

        partnerNo!!.setOnClickListener {
            this.dismiss()
        }
    }

    private fun initView() {
        partnerNo = findViewById(R.id.dialog_partner_no)
        partnerOk = findViewById(R.id.dialog_partner_ok)
        partnerEd = findViewById(R.id.dialog_partner_ed)

    }

    interface YesOnClickListener {
        fun onYesClick(str: String)
    }

    interface NoOnClickListener {
        fun onNoClick()
    }

    fun setYesOnClickListener(yesOnClickListener: YesOnClickListener) {
        this.yesOnClickListener = yesOnClickListener
    }

    fun setNoOnClickListener(noOnClickListener: NoOnClickListener) {
        this.noOnClickListener = noOnClickListener
    }
}