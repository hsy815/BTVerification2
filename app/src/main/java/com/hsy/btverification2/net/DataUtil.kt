package com.hsy.btverification2.net

import com.hsy.btverification2.entity.*
import okhttp3.RequestBody
import rx.Observable

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/10 17:26
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/10 17:26
 * @修改描述:
 */
object DataUtil {

    /**
     * 登录
     */
    fun getLogin(user: String, psw: String): Observable<LoginInfo> {
        return NetRetrofit.reRetrofit().getLogin(user, psw)
    }

    fun getSecretToken(requestMap: RequestBody): Observable<SecretToken> {
        return NetRetrofit.reRetrofit().getSecretToken(requestMap)
    }

    /**
     * 身份证录入信息
     */
    fun subIdCardEntry(map: Map<String, String>): Observable<IdCardInfo> {
        return NetRetrofit.reRetrofit().subIdCardEntry(map)
    }

    /**
     * 二维码录入信息
     */
    fun scanCodeEntry(code: String, deviceId: String): Observable<ScanCodeInfo> {
        return NetRetrofit.reRetrofit().scanCodeEntry(code)
    }

    /**
     * 采样确认
     */
    fun sampling(map: Map<String, String>): Observable<Sampling> {
        return NetRetrofit.reRetrofit().sampling(map)
    }

    /**
     * 采样确认
     */
    fun readMix(map: Map<String, String>): Observable<ReadMix> {
        return NetRetrofit.reRetrofit().readMix(map)
    }

    /**
     * 获取合作方信息
     */
    fun getPartner(gid: String): Observable<PartnerNet> {
        return NetRetrofit.reRetrofit().getPartner(gid)
    }

    /**
     * 编辑合作方信息
     */
    fun savePartner(map: Map<String, String>): Observable<PartnerNet> {
        return NetRetrofit.reRetrofit().savePartner(map)
    }
}