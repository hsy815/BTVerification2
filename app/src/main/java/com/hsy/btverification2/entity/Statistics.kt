package com.hsy.btverification2.entity

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.entity
 * @创始人: hsy
 * @创建时间: 2021/8/27 14:08
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/27 14:08
 * @修改描述:
 */
class Statistics(

    /**
     * 记录时间戳
     */
    var date: Long,
    /**
     * 记录日期
     */
    var time: String,
    /**
     * 采样人
     */
    var samplingPerson: String,
    /**
     * 提交码
     */
    var submissionCode: String,
    /**
     * 打印码
     */
    var printCode: String,
    /**
     * 封箱码
     */
    var sealingBoxCode: String,
    /**
     * 是否封箱
     * 0未封箱
     * 1封箱
     */
    var isSealingBox: Int,
    /**
     * 提交状态
     * 0未提交
     * 1提交
     */
    var isSubmissionStatus: Int,
    /**
     * 客户姓名
     */
    var userName: String,
    /**
     * 客户证件号
     */
    var userCard: String,
    /**
     * 客户所属团队
     */
    var userGroup: String
) {
}