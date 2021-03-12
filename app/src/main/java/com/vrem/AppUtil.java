package com.vrem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * String 写入文件测试
 */
public class AppUtil {

    public String PATH = "/storage/emulated/0/Download/";

    public void saveAsFileWriter(String content, String filename) {
        FileWriter fwriter = null;
        String strContent = content + "\r\n";
        //String strContent = content + ",";
        try {
            if (!new File(PATH).exists()){
                new File(PATH).mkdirs();
            }
            File file = new File(PATH + filename);
            if (!file.exists()) {
                file.createNewFile();
                //Runtime.getRuntime().exec("chmod 777 " +  file );
            }
            fwriter = new FileWriter(file, true);
            fwriter.write(strContent);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public String readfile(String filePath) {
        FileInputStream fileInputStream;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(filePath);
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + " ");
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return stringBuilder.toString();
    }

}
