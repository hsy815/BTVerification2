package com.hsy.btverification2.helpUtil

import android.app.AlertDialog
import android.content.Context

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.helpUtil
 * @创始人: hsy
 * @创建时间: 2020/12/14 10:20
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/14 10:20
 * @修改描述:
 */
class ReadIdHelp(private val context: Context) {

    /**
     * 提交身份信息出现错误弹出框
     */
    fun errorMessageDialog(msg: String?) {
        AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

}