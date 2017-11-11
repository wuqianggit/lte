package com.roger.lte.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件写入工具类
 * Created by Administrator on 2017/11/9.
 */
public class FileWriteUtil {
    /**
     * 在源文件的基础上，追加内容
     * @param fileName 文件名称
     * @param content 文件内容
     * @return 返回保存的文件路径，如果返回null则说明，写入文件失败
     */
    public static String writeAppend(String fileName,String content){
        FileOutputStream fos = null;
        try {
            /*获取sd卡的目录*/
            File sdCardDir = Environment.getExternalStorageDirectory();
            File parentFile=new File(sdCardDir,"lte");/*父文件名*/
            if(!parentFile.exists()){
                parentFile.mkdirs();
            }
            File targetFile=new File(parentFile.getAbsolutePath(),fileName+".dat");/*父文件一定要存在*/
            fos=new FileOutputStream(targetFile,true);
            fos.write(content.getBytes());
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
