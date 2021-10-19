package com.hsy.btverification2.helpUtil;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class FileTxt {

    private FileTxt() {
    }

    public static class FileTxtHolder {
        private static final FileTxt INSTANCE = new FileTxt();
    }

    public static FileTxt getInstance() {
        return FileTxtHolder.INSTANCE;
    }

    public void writeTxt(String strTxt, String centerName) {
        String path = Environment.getExternalStorageDirectory() + "/BtVerification/";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date(System.currentTimeMillis());
        String strDate = simpleDateFormat.format(date);
        writeTxtToFile((strDate + ":" + strTxt), path, (centerName + strDate));
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String sContent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = sContent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                Objects.requireNonNull(file.getParentFile()).mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    private void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public static String str216Str(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String str2by(String args) {
        byte[] dataToByte = args.getBytes();
        byte[] oneMB = new byte[1024];
        // 本来的数据拷贝
        for (int i = 0; i < dataToByte.length; i++) {
            oneMB[i] = dataToByte[i];
        }
        // 补全空格，对应的 byte 是32
        for (int i = dataToByte.length; i < 1024; i++) {
            oneMB[i] = (byte) 32;
        }
        return oneMB.toString();
    }
}
