package com.onlinejudge.judger.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static void saveFile(String content, String path) {
        File file = new File(path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] bytes = content.getBytes();
            outputStream.write(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static boolean deleteFile(String path) {
        File file = new File(path);
        boolean result = false;
        // 判断File对象是不是目录
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            //  遍历数组,显示目录中文件列表
            for (File subFile : files) {
                if (subFile.isDirectory()) {
                    deleteFile(subFile.getPath());
                } else {
                    result = subFile.delete();
                }
            }
        }
        result = file.delete();
        return result;
    }


}
