package com.hsy.btverification2.helpUtil;

import android.os.Environment;

import com.hsy.btverification2.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * @项目名: BtVerification
 * @类位置: com.hsy.btverification.helpUtil
 * @创始人: hsy
 * @创建时间: 2021/8/26 17:01
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/8/26 17:01
 * @修改描述:
 */
public class CopyDb {

    /**
     * 见assets目录下的文件拷贝到sd上
     * 复制和加载区域数据库中的数据
     *
     * @return 存储数据库的地址
     */
    public static String CopySqliteFileFromRawToDatabases(String SqliteFileName) throws IOException {
        // 第一次运行应用程序时，加载数据库到data/data/当前包的名称/database/<db_name>
        File dir = new File("data/data/" + Objects.requireNonNull(MyApplication.Companion.getInstance()).getPackageName() + "/databases");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, SqliteFileName);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        //通过IO流的方式，将assets目录下的数据库文件，写入到SD卡中。
        if (!file.exists()) {
            try {
                file.createNewFile();
                inputStream = Objects.requireNonNull(MyApplication.Companion.getInstance().getClass().getClassLoader()).getResourceAsStream("assets/" + SqliteFileName);
                outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        return file.getPath();
    }

    // 复制文件
    public static void copyFile() {
        File f = new File("data/data/" + Objects.requireNonNull(MyApplication.Companion.getInstance()).getPackageName() + "/databases/btv.db");
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
        File o = new File(sdcardPath + "abc.db"); //sdcard上的目标地址

        if (f.exists()) {
            FileChannel outF;
            try {
                outF = new FileOutputStream(o).getChannel();
                new FileInputStream(f).getChannel().transferTo(0, f.length(), outF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

