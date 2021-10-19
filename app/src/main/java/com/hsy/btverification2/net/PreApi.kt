package com.hsy.btverification2.net

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/10 17:15
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/10 17:15
 * @修改描述:
 */
object PreApi {

    const val token = "connect/token"

    /**
     * 身份证录入
     */
    const val idCardEntry = "Covid19Appointment/RegisterAndSamplingForOuter"

    /**
     * 发送运输码
     */
    const val sendCode = "TransCode/CreateTransCode/"

    /**
     * 跟据预约码获取客户信息
     */
    const val getUserInfo = "BookingOrder/GetBookingOrderPlanarization/"

    /**
     * 扫码录入
     */
    const val scanCodeEntry = "BookingOrder/SampleCodePrintAndSamplingCompleted/"
//    const val scanCodeEntry = "ocelot/Covid19Api/BookingOrder/SampleCodePrintAndSamplingCompleted4MobileDevice/"

    /**
     * 封箱
     */
    const val finishedBox = "TransCode/Finished/"

    /**
     * 采样确认
     */
    const val sampling = "ocelot/Covid19Api/BookingOrder/SamplingCompleted/{0}"

    /**
     * 身份证混采
     */
    const val readMix = "RegisterForPartner/BatchSave"

    /**
     * 获取合作方信息
     */
    const val partner = "Covid19Appointment/GetPartnerForMobileDeviceCreateOnAugustTwentieth"

    /**
     * 添加合作方信息
     */
    const val savePartner = "Covid19Appointment/SavePartnerForMobileDeviceCreateOnAugustTwentieth"
}