package com.vrem.wifianalyzer;


import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetCompany {

    String company = "无厂商信息";
    public String read_csv(String mac) {
        File csv = new File("data/data/com.vrem.wifianalyzer/files/oui.csv");  //CSV文件路径
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(csv));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        String everyLine = "";
        String Cun = "";
        try {
            List<String> allString = new ArrayList<>();
            while ((line = br.readLine()) != null)  //读取到的内容给line变量
            {
                everyLine = line;
                if (everyLine.indexOf(mac) != -1) { //比对
                    Cun = everyLine;
                    int i1 = Cun.indexOf(',');
                    int i2 = Cun.indexOf(',',i1+1);
                    String ss = Cun.substring(i2+1, Cun.length()-1);
                    company = ss.replace("\"", "");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return company;
    }
}
