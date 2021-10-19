package com.hsy.btverification2.db_control

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.db_control
 * @创始人: hsy
 * @创建时间: 2021/8/26 16:59
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 16:59
 * @修改描述:
 */
class MySqliteHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}