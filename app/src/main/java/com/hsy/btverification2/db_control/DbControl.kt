package com.hsy.btverification2.db_control

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.hsy.btverification2.entity.Statistics
import com.hsy.btverification2.helpUtil.CopyDb
import java.io.IOException

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.db_control
 * @创始人: hsy
 * @创建时间: 2021/8/26 17:00
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 17:00
 * @修改描述:
 */
class DbControl private constructor(context: Context) {
    private val database: SQLiteDatabase
    private val DbName = "btv.db"
    private val mySqliteHelper: MySqliteHelper

    /**
     * 新增采样记录
     */
    fun addStatistics(statistics: Statistics): Boolean {
        val values = ContentValues()
        values.put("date", statistics.date)
        values.put("time", statistics.time)
        values.put("samplingPerson", statistics.samplingPerson)
        values.put("submissionCode", statistics.submissionCode)
        values.put("printCode", statistics.printCode)
        values.put("sealingBoxCode", statistics.sealingBoxCode)
        values.put("isSealingBox", statistics.isSealingBox)
        values.put("isSubmissionStatus", statistics.isSubmissionStatus)
        values.put("userName", statistics.userName)
        values.put("userCard", statistics.userCard)
        values.put("userGroup", statistics.userGroup)
        val index = database.insert("Statistics", null, values)
        return index > 0
    }

    /**
     * 客户信息修改
     */
    fun upDateStatisticsUserInfo(
        submissionCode: String,
        userName: String,
        userCard: String,
        userGroup: String?
    ) {
        val sql = if (TextUtils.isEmpty(userGroup)) {
            "UPDATE Statistics SET userName = '$userName', userCard = '$userCard' WHERE submissionCode = '$submissionCode'"
        } else {
            "UPDATE Statistics SET userName = '$userName', userCard = '$userCard', userGroup = '$userGroup' WHERE submissionCode = '$submissionCode'"
        }
        database.execSQL(sql)
    }

    /**
     * 确认并打印状态修改
     */
    fun upDateStatisticsSubmission(submissionCode: String, printCode: String) {
        val sql =
            "UPDATE Statistics SET printCode = '$printCode', isSubmissionStatus = 1 WHERE submissionCode = '$submissionCode'"
        database.execSQL(sql)
    }

    /**
     * 封箱状态修改
     */
    fun upDateStatisticsSealingBox(sealingBoxCode: String) {
        val sql =
            "UPDATE Statistics SET isSealingBox = 1 WHERE sealingBoxCode = '$sealingBoxCode'"
        database.execSQL(sql)
    }

    /**
     * 采样删除n天之前的数据
     */
    fun deleteStatistics(time: String) {
        database.execSQL("delete from Statistics where time = '$time'")
//        database.execSQL("delete from Statistics where date('now', '-7 day') >= date($time)")
    }

    /**
     * 查询所有采样人姓名
     */
    fun selectStatisticsSamplingPerson(): ArrayList<String?> {
        val list: ArrayList<String?> = ArrayList()
        val sql = "SELECT distinct samplingPerson FROM Statistics"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    /**
     * 查询单日，单方式，单合作方，单采样点采样记录
     *
     * 返回 总和
     */
    fun selectStatisticsSum(time: String, mode: String, partner: String): Int {
        val sql =
            "SELECT SUM(locale_count) FROM Statistics WHERE time = '${time}' AND partner = '${partner}' AND mode = '${mode}'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }

    /**
     * 查询这个人今天有效采样总次数
     */
    fun selectStatisticsSum(time: String, samplingPerson: String): Int {
        val sql =
            "SELECT COUNT(*) FROM Statistics WHERE time = '$time' AND samplingPerson = '$samplingPerson' AND isSubmissionStatus = 1"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }

    /**
     * 查询本条数据是否已经存储数据库，排除重复记录
     */
    fun selectStatisticsSubmissionCodeSum(submissionCode: String): Int {
        val sql =
            "SELECT COUNT(*) FROM Statistics WHERE submissionCode = '$submissionCode'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }

    /**
     * 查询该订单数据是否已打码，避免重复计数
     */
    fun selectStatisticsSubmissionCodeIs(submissionCode: String): Int {
        val sql =
            "SELECT isSubmissionStatus FROM Statistics WHERE submissionCode = '$submissionCode'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }

    /**
     * 查询箱子里是否有有效数据
     */
    fun selectStatisticsBoxSum(sealingBoxCode: String): Int {
        val sql =
            "SELECT COUNT(*) FROM Statistics WHERE sealingBoxCode = '$sealingBoxCode' AND isSubmissionStatus = 1 AND isSealingBox = 0"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }


    /**
     * 查询当日混采总共人数
     */
    fun selectGroupCardSum(time: String): Int {
        val sql =
            "SELECT SUM(groupCount) FROM ReadGroup WHERE time = '${time}'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        val mCount = cursor.getInt(0)
        cursor.close()
        return mCount
    }

    /**
     * 查询是否有未封箱数据
     */
    fun selectIsSealingBox(): String? {
        val list: ArrayList<String?> = ArrayList()
        val sql =
            "SELECT distinct sealingBoxCode FROM Statistics WHERE isSealingBox = 0"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return if (list.size > 0) {
            list[0]
        } else {
            null
        }
    }

    /**
     * 查询是否有未封箱数据
     */
    fun selectIsSealingBoxSP(mSealingBoxCode: String): String? {
        val list: ArrayList<String?> = ArrayList()
        val sql =
            "SELECT distinct samplingPerson FROM Statistics WHERE sealingBoxCode = '$mSealingBoxCode'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return if (list.size > 0) {
            list[0]
        } else {
            null
        }
    }

    /**
     * 查询是否有未封箱数据
     */
    fun selectStatistics(submissionCode: String): Statistics? {
        val list: ArrayList<Statistics?> = ArrayList()
        val sql =
            "SELECT * FROM Statistics WHERE submissionCode = '$submissionCode'"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(
                Statistics(
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11)
                )
            )
            cursor.moveToNext()
        }
        cursor.close()
        return if (list.size > 0) {
            list[0]
        } else {
            null
        }
    }

    /**
     * 处理未提交并且未封箱的数据
     */
    fun handleNoSubmissionStatus(sealingBoxCode: String) {
        val list: ArrayList<String?> = ArrayList()
        val sql =
            "SELECT submissionCode FROM Statistics WHERE sealingBoxCode = '$sealingBoxCode' AND isSealingBox = 0 AND isSubmissionStatus = 0"
        val cursor: Cursor = database.rawQuery(sql, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        val mCount = if (list.size > 0) {
            list[0]
        } else {
            null
        }
        if (!TextUtils.isEmpty(mCount)) {
            val mCounts = "$mCount+"
            database.execSQL("UPDATE Statistics SET submissionCode = '$mCounts' WHERE sealingBoxCode = '$sealingBoxCode' AND isSealingBox = 0 AND isSubmissionStatus = 0 ")
        }
    }

    /**
     * 获取所有表名
     *
     * @return
     */
    val allTableName: ArrayList<String?>
        get() {
            val list: ArrayList<String?> = ArrayList<String?>()
            val sql = "SELECT name FROM SQLITE_MASTER WHERE type='table' ORDER BY name"
            val cursor = database.rawQuery(sql, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(cursor.getString(0))
                cursor.moveToNext()
            }
            cursor.close()
            return list
        }

    /**
     * 判断是否有这个表
     *
     * @param tableName  需要检查的表名
     * @param stringList 所有表
     * @return
     */
    fun isTable(tableName: String, stringList: List<String>): Boolean {
        var i = 0
        while (1 < stringList.size) {
            return tableName == stringList[i]
            i++
        }
        return false
    }

    /**
     * 增加一条信息
     *
     * @param sql
     */
    fun addAll(sql: String?) {
        database.execSQL(sql)
    }

    /**
     * 删除一条信息
     *
     * @param sql
     */
    fun daleAll(sql: String?) {
        database.execSQL(sql)
    }

    /**
     * 关闭数据库连接
     */
    fun close() {
        mySqliteHelper.close()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var dbControl: DbControl? = null
        fun getInstance(mContext: Context): DbControl {
            if (dbControl == null) dbControl = DbControl(mContext)
            return dbControl!!
        }
    }

    init {
        try {
            CopyDb.CopySqliteFileFromRawToDatabases(DbName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mySqliteHelper = MySqliteHelper(context, DbName, null, 1)
        database = mySqliteHelper.writableDatabase
    }
}