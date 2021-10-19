package com.hsy.btverification2.helpUtil;

import android.nfc.NfcAdapter;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NfcManager {
    private static final int invalidAge = -1;//非法的年龄，用于处理异常。

    public void nfcEnable(NfcAdapter adapter) {
        try {
            Class clazz = Class.forName("android.nfc.NfcAdapter");
            Method enable = clazz.getMethod("enable");
            // enable.setAccessible(true);
            enable.invoke(adapter);

            Log.d("TAG", "调用enable方法成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nfcDisable(NfcAdapter adapter) {
        try {
            Class clazz = Class.forName("android.nfc.NfcAdapter");
            Method enable = clazz.getMethod("disable", boolean.class);
            // enable.setAccessible(true);
            enable.invoke(adapter, false);

            Log.d("TAG", "调用disable方法成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param dateStr 这样格式的生日 1990年01月01日
     * @return
     */
    public String getBirthday(String dateStr) {
        dateStr = dateStr.replace("年", "-");
        dateStr = dateStr.replace("月", "-");
        dateStr = dateStr.replace("日", "");
        Log.e("tagNfc","getBirthday="+dateStr);
        return dateStr;
    }

    /**
     * 根据生日计算年龄
     *
     * @param dateStr 这样格式的生日 1990-01-01
     */

    public int getAgeByDateString(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date birthday = simpleDateFormat.parse(dateStr);
            return getAgeByDate(birthday);
        } catch (ParseException e) {
            return -1;
        }
    }


    private static int getAgeByDate(Date birthday) {
        Calendar calendar = Calendar.getInstance();

        if (calendar.getTimeInMillis() - birthday.getTime() < 0L) {
            return invalidAge;
        }


        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        int dayOfMonthNow = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(birthday);


        int yearBirthday = calendar.get(Calendar.YEAR);
        int monthBirthday = calendar.get(Calendar.MONTH);
        int dayOfMonthBirthday = calendar.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirthday;


        if (monthNow <= monthBirthday && monthNow == monthBirthday && dayOfMonthNow < dayOfMonthBirthday || monthNow < monthBirthday) {
            age--;
        }

        return age;
    }
}
