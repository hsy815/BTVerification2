package com.hsy.btverification2.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hsy.btverification2.MyApplication
import com.hsy.btverification2.entity.SecretToken
import com.hsy.btverification2.net.DataUtil
import com.hsy.btverification2.net.LoadingDialog
import com.hsy.btverification2.net.MySubscriber
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.activity
 * @创始人: hsy
 * @创建时间: 2020/12/11 15:29
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2020/12/11 15:29
 * @修改描述:
 */
abstract class BaseActivity : AppCompatActivity() {

    internal lateinit var loadingDialog: LoadingDialog

    var subscription: CompositeSubscription? = null

    /**
     * 初始化之前的操作
     */
    abstract fun init(savedInstanceState: Bundle?)

    /**
     * 设置ContentView
     */
    abstract fun setLayoutResourceID(): Int

    /**
     * 处理UI操作
     */
    abstract fun initUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(savedInstanceState)
        if (0 != setLayoutResourceID())
            setContentView(setLayoutResourceID())

        loadingDialog = LoadingDialog(this)
        initUI()
    }

    /**
     * 使用动画跳转
     */
    fun myStartActivity(intent: Intent?, activity: Activity) {
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
    }

    fun myStartActivity(intent: Intent?) {
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        if (subscription != null && subscription!!.hasSubscriptions()) {
            subscription!!.unsubscribe()
        }
    }

    fun getSecretToken() {
        loadingDialog.show()

        subscription = CompositeSubscription()
        subscription!!.add(
            DataUtil.getSecretToken(
                getRequestBody()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySubscriber<SecretToken>(this) {
                    override fun onCompleted() {
                        loadingDialog.dismiss()
                    }

                    override fun onNext(t: SecretToken?) {
                        MyApplication.instance!!.secretToken = t
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        loadingDialog.dismiss()
                    }
                })
        )
    }

    private fun getRequestBody(): RequestBody {
        val data = StringBuffer()
        data.append("client_id").append("=").append("Bio.Covid19.Service.Client").append("&")
        data.append("client_secret").append("=").append("markjiang7m2").append("&")
        data.append("grant_type").append("=").append("client_credentials").append("&")
        data.append("scope").append("=")
            .append("Bio.Covid19.Service.Api Bio.Wechat.Global.Server Bio.SMS.Service")

        return getBody(data.toString())
    }

    private fun getBody(string: String): RequestBody {
        return string.toRequestBody("application/x-www-form-urlencoded; charset=utf-8".toMediaTypeOrNull())
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date(System.currentTimeMillis())
        return simpleDateFormat.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate6(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        return simpleDateFormat.format(calendar.time)
    }

    fun getVersion(): String {
        return packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_CONFIGURATIONS
        ).versionName
    }
}