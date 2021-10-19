package com.hsy.btverification2.entity

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.entity
 * @创始人: hsy
 * @创建时间: 2021/8/27 14:23
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/27 14:23
 * @修改描述:
 */
object ModeUtil {
    const val CARD_ID_SINGLE = "身单"//身份证单采
    const val CARD_ID_MANY = "身多"//身份证混采
    const val SAO_VERIFICATION = "团采"//验证付款采样
    const val SAO_NO_VERIFICATION = "单采"//不验证付款采样
    const val SAO_CONFIRM = "扫确"//确认采样
}