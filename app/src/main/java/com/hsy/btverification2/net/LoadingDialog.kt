package com.hsy.btverification2.net

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import com.hsy.btverification2.R
import kotlinx.android.synthetic.main.loading_dialog.*


/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/11 14:35
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 14:35
 * @修改描述:
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_dialog)
        setCanceledOnTouchOutside(false)
        val operatingAnim =
                AnimationUtils.loadAnimation(
                        context,
                        R.anim.loading_data
                )
        val lin = LinearInterpolator()
        operatingAnim!!.interpolator = lin
        loading_dialog_img.animation = operatingAnim
    }

}