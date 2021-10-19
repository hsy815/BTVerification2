package com.hsy.btverification2.net

import com.hsy.btverification2.entity.*
import okhttp3.RequestBody
import retrofit2.http.*
import rx.Observable

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.net
 * @创始人: hsy
 * @创建时间: 2020/12/10 17:07
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/10 17:07
 * @修改描述:
 */
interface ApiService {

    /**
     * 登录
     */
    @GET("PreApi.login")
//    @GET(PreApi.login)
    fun getLogin(
        @Query("userName") userName: String,
        @Query("password") password: String
    ): Observable<LoginInfo>


    @JvmSuppressWildcards
    @POST(PreApi.token)
    fun getSecretToken(
        @Body requestMap: RequestBody
    ): Observable<SecretToken>

    /**
     * 身份证录入信息
     */
    @FormUrlEncoded
    @POST(PreApi.idCardEntry)
    fun subIdCardEntry(
        @FieldMap map: Map<String, String>
    ): Observable<IdCardInfo>

    /**
     * 二维码录入信息
     */
    @PATCH("${PreApi.scanCodeEntry}/{path1}/{path2}")
    fun scanCodeEntry(
        @Path("path1") code: String,
        @Path("path2") deviceId: String
    ): Observable<ScanCodeInfo>

    /**
     * 二维码录入信息
     */
    @PATCH("${PreApi.scanCodeEntry}/{path1}")
    fun scanCodeEntry(
        @Path("path1") code: String
    ): Observable<ScanCodeInfo>
    /**
     * 采样确认
     */
    @FormUrlEncoded
    @POST(PreApi.sampling)
    fun sampling(
        @FieldMap map: Map<String, String>
    ): Observable<Sampling>

    /**
     * 身份证混采
     */
    @FormUrlEncoded
    @POST(PreApi.readMix)
    fun readMix(
        @FieldMap map: Map<String, String>
    ): Observable<ReadMix>

    /**
     * 获取合作方信息
     */
    @GET(PreApi.partner)
    fun getPartner(
        @Query("gid") gid: String
    ): Observable<PartnerNet>

    /**
     * 编辑合作方信息
     */
    @FormUrlEncoded
    @POST(PreApi.savePartner)
    fun savePartner(
        @FieldMap map: Map<String, String>
    ): Observable<PartnerNet>
}