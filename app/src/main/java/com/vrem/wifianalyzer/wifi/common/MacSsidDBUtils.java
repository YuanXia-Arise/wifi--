package com.vrem.wifianalyzer.wifi.common;

import android.bluetooth.BluetoothClass;
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
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MacSsidDBUtils {
    private static final String TAG = MacSsidDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 1;
    // DB名
    public static final String DATABASE_NAME = "MacSsid.Db";

    public static final String TABLE_MACSSID_TABLE ="macssid";
    public static final String KEY_ROWID = "_id"; // integer 自增长，主key
    public static final String DEVID = "devid"; // dev唯一识别号
    public static final String MAC = "mac"; //热点MAC
    public static final String SSID = "ssid"; //热点SSID
    public static final String LAST_TIME = "last_time"; // last_time
    public static final String COUNT = "count";
    public static final String TIME = "time";

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public MacSsidDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder createMacSsidTable = new StringBuilder();
            createMacSsidTable.append("create table ").append(TABLE_MACSSID_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(DEVID).append(" text, ")
                    .append(MAC).append(" text, ")
                    .append(SSID).append(" text, ")
                    .append(LAST_TIME).append(" text, ")
                    .append(TIME).append(" text, ")
                    .append(COUNT).append(" text, ")
                    .append("unique (").append(DEVID).append(",").append(MAC).append(")")
                    .append(");");
            db.execSQL(createMacSsidTable.toString());
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

    public MacSsidDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    /*public void insertOrUpdate(String devID, String mac, String ssid) {
        StringBuilder sql1 = new StringBuilder();
        sql1.append("insert or ignore into ").append(TABLE_MACSSID_TABLE).append(" (")
                .append(DEVID).append(", ")
                .append(MAC).append(", ")
                .append(SSID).append(" ")
                .append(") values ('")
                .append(devID).append("', '")
                .append(mac).append("', '")
                .append(ssid).append("')");
        mDb.execSQL(sql1.toString());
        //Log.w("SQL", sql1.toString());

        String sql2 = "update macssid set ssid='" + ssid + "' where devid='" + devID + "' and mac='" + mac + "'";
        mDb.execSQL(sql2);
        //Log.w("SQL", sql2.toString());
    }*/

    public void insertOrUpdate(String devID, String mac, String ssid, String last_time, String count, String time){
        String sql = "insert or ignore into "+TABLE_MACSSID_TABLE+" ("+DEVID+","+MAC+","+SSID+","+LAST_TIME+","+COUNT+","+TIME+") values ('"+devID+"','"+mac+"','"+ssid+"','"+last_time+"','"+count+"','"+time+"')";
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

    public String getSSID(String devID, String mac) {
        String sql = "select ssid from macssid where devid='" + devID + "' and mac='" + mac + "'";

        String ssid = "";
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            ssid = cursor.getString(0);
            break;
        }
        return ssid;
    }

    // last_time 获取
    public String getLastTime(String mac) {
        String sql = "select * from macssid where mac='"+mac+"'";
        //String sql = "select last_time from macssid where ssid='" + ssid + "'";

        String last_time = "";
        try{
            Cursor cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                last_time = cursor.getString(cursor.getColumnIndex("last_time"));
            }
        } catch (SQLException e){}
        return last_time;
    }

    // last_time 更新
    public void updataLast_time(String mac, String last_time){
        String sql = "update "+TABLE_MACSSID_TABLE+" set last_time='"+last_time+"' where mac='"+mac+"';";
        try{
            mDb.execSQL(sql);
        } catch (SQLException e){
        }
    }

    // count获取
    public String getCount(String mac) {
        /*String query = "SELECT  * FROM " + TABLE_MACSSID_TABLE + " WHERE ssid = ? ;";
        Cursor cursor = mDb.rawQuery(query, new String[] {ssid});*/
        String sql = "select * from macssid where mac='"+mac+"'";

        String count = "";
        try{
            Cursor cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                count = cursor.getString(cursor.getColumnIndex("count"));
            }
        } catch (SQLException e){}
        return count;
    }

    // count更新
    public void updataCount(String mac, String count){
        String sql = "update "+TABLE_MACSSID_TABLE+" set count='"+count+"' where mac='"+mac+"';";
        //mDb.execSQL(sql);
        try{
            mDb.execSQL(sql);
        } catch (SQLException e){
        }
    }

    // time获取
    public String getTime(String mac) {
        /*String query = "SELECT  * FROM " + TABLE_MACSSID_TABLE + " WHERE ssid = ? ;";
        Cursor cursor = mDb.rawQuery(query, new String[] {ssid});*/
        String sql = "select * from macssid where mac='"+mac+"'";

        String time = "";
        try{
            Cursor cursor = mDb.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                time = cursor.getString(cursor.getColumnIndex("time"));
            }
        } catch (SQLException e){}
        return time;
    }

    // time更新
    public void updataTime(String mac, String time){
        String sql = "update "+TABLE_MACSSID_TABLE+" set time='"+time+"' where mac='"+mac+"';";
        //mDb.execSQL(sql);
        try{
            mDb.execSQL(sql);
        } catch (SQLException e){
        }
    }

    /**
     * 2019.09.05 hc
     * 清除数据库数据
     **/
    public void delete_sql(Context context) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from " + TABLE_MACSSID_TABLE);
        db.close();
    }


    // 导出数据库至CSV文件
    public void ExportToCSV(String fileName) {
        String sql = "select * from macssid";
        Cursor c = null;
        int rowCount = 0;
        int colCount = 0;
        FileWriter fw;
        BufferedWriter bfw;
        SimpleDateFormat timesdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String FileTime = timesdf.format(new Date()).toString();
        String Timename = FileTime.replace("-", "");
        File saveDir = new File("/storage/emulated/0/Download/" + Timename);
        if (!saveDir.exists()){
            saveDir.mkdir();
        }
        File saveFile = new File(saveDir, fileName);
        try {
            c = mDb.rawQuery(sql,null);
            rowCount = c.getCount();
            colCount = c.getColumnCount();
            fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(fw);
            while (c.moveToNext()) {
                if (rowCount > 0) {
                    c.moveToFirst();
                    for (int i = 0; i < colCount; i++) { // 写入表头
                        if (i != colCount - 1) {
                            bfw.write(c.getColumnName(i) + ',');
                        } else {
                            bfw.write(c.getColumnName(i));
                        }
                    }
                    bfw.newLine(); // 写好表头后换行
                    for (int i = 0; i < rowCount; i++) { // 写入数据
                        c.moveToPosition(i);
                        Log.v("导出数据", "正在导出第" + (i + 1) + "条");
                        for (int j = 0; j < colCount; j++) {
                            if (j != colCount - 1) {
                                bfw.write(c.getString(j) + ',');
                            } else {
                                bfw.write(c.getString(j));
                            }
                        }
                        bfw.newLine(); // 写好每条记录后换行
                    }
                }
            }
            // 将缓存数据写入文件
            bfw.flush();
            // 释放缓存
            bfw.close();
            Log.v("导出数据", "导出完毕！");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                mDb.close();
            }
        }
    }

}
