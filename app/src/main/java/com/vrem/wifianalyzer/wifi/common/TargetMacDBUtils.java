package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class TargetMacDBUtils {
    private static final String TAG = TargetMacDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 1;
    // DB名
    public static final String DATABASE_NAME = "Targetmac.Db";

    public static final String TABLE_MACSSID_TABLE ="targetmac";
    public static final String KEY_ROWID = "id"; // integer 自增长，主key
    public static final String MAC = "mac"; //热点MAC
    public static final String COMPANY = "company"; //厂商地址
    public static final String REMARKS = "remarks"; // 备注

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public TargetMacDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建Permission信息表
            StringBuilder createSnifferFilesTable = new StringBuilder();
            createSnifferFilesTable.append("create table ").append(TABLE_MACSSID_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(MAC).append(" text, ")
                    .append(COMPANY).append(" text, ")
                    .append(REMARKS).append(" text ")
                    .append(");");
            db.execSQL(createSnifferFilesTable.toString());
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Auto-generated method stub
            Log.w(TAG, "Upgrading database from version " + oldVersion + "to " +
                    newVersion + ", which will destroy all old data" + TABLE_MACSSID_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MACSSID_TABLE);
            onCreate(db);
        }
    }

    public TargetMacDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void insertOrUpdate(String mac, String company, String remarks){
        String sql = "insert or ignore into "+TABLE_MACSSID_TABLE+" ("+MAC+","+COMPANY+","+REMARKS+") values ('"+mac+"','"+company+"','"+remarks+"')";
        Cursor cursor = null;
        try{
            cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()){
                if (cursor.getCount() > 0){}
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return;
    }

    // 获取数据库mac
    public List<String> selectMac(){
        List<String> List = new ArrayList<>();
        String sql = "select * from targetmac";

        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()){
            String mac = cursor.getString(cursor.getColumnIndex("mac"));
            System.out.println("20202020==1>" + mac);
            List.add(mac);
        }
        return List;
    }

    // mac获取remarks
    public String getRemarks(String mac) {
        String sql = "select * from targetmac where mac='"+mac+"'";
        String remarks = "";
        try{
            Cursor cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                remarks = cursor.getString(cursor.getColumnIndex("remarks"));
            }
        } catch (SQLException e){}
        return remarks;
    }

    // mac获取company
    public String getCompany(String mac) {
        String sql = "select * from targetmac where mac='"+mac+"'";
        String company = "";
        try{
            Cursor cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                company = cursor.getString(cursor.getColumnIndex("company"));
            }
        } catch (SQLException e){}
        return company;
    }

    /*
     * 判断mac是否已存在数据库中
     * */
    public boolean isMac(String mac){
        String sql = "select * from targetmac where mac='"+mac+"'";
        boolean flag = false;
        /*Cursor cursor = mDb.rawQuery(sql,null);
        if (cursor.moveToNext()){
            flag = true;
            Log.v("flag","true");
        }
        return flag;*/
        Cursor cursor = null;
        try{
            cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()){
                if (cursor.getCount() > 0){
                    flag = true;
                }
            }
        } catch (Exception e){
            Log.e(TAG, "isComputer: error" );
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    // remarks更新
    public void updataRemarks(String mac, String remarks){
        String sql = "update "+TABLE_MACSSID_TABLE+" set remarks='"+remarks+"' where mac='"+mac+"';";
        try{
            mDb.execSQL(sql);
        } catch (SQLException e){
        }
    }

    // 根据Mac删除数据
    public void delData(String mac){
        String sql = "delete from targetmac where mac='"+mac+"'";
        mDb.execSQL(sql);
    }

}
