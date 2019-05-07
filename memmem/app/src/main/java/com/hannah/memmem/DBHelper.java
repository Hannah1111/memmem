package com.hannah.memmem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import static com.hannah.memmem.Utils.decodeStr;
import static com.hannah.memmem.Utils.encodeStr;


/**
 * Created by enuri_lab_036 on 2017. 1. 31..
 */

public class DBHelper extends SQLiteOpenHelper {

    public static DBHelper instance;
    static final String DBNAME = "memo.db";
    final String TABLE_NAME = "TBL_MEMO";
    final String COLUMN_IDX = "IDX";
    final String COLUMN_TITLE = "TITLE";
    final String COLUMN_MESSAGE = "MESSAGE";
    final String COLUMN_INSERT_DATE = "INSERT_DATE";
    final String COLUMN_UPDATE_DATE = "UPDATE_DATE";

    final String TABLE_WIDGET = "TBL_WIDGET";
    final String COLUMN_WIDGETID = "WIDGETID";
    final String COLUMN_KEY = "KEY";

    static int dbVersion = 1;

    private Context context;
    static SQLiteDatabase db;

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase sqLiteDatabase) {
        db = sqLiteDatabase;
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_IDX + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT, "+ COLUMN_MESSAGE +" TEXT, "+ COLUMN_INSERT_DATE +" LONG, "+ COLUMN_UPDATE_DATE +" LONG )");

        db.execSQL("CREATE TABLE " + TABLE_WIDGET + " (" + COLUMN_WIDGETID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_KEY + " INTEGER )");
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public synchronized static DBHelper getInstance(Context ctx){
        if(instance == null) {
            instance = new DBHelper(ctx,DBNAME,null,dbVersion); //DBver = 1
        }
        return instance;
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        this.context = context;
        db = getWritableDatabase();
    }


    /**
     * 현재 database의 버전을 가져오는 함수
     * @return 현재 DB버전
     */
    public int getDBversion(){
        return dbVersion;
    }



    public boolean insertData(String title, String msg){
        try{

            long now = System.currentTimeMillis();
            String sql = "INSERT INTO "+TABLE_NAME+"("+COLUMN_TITLE+","+COLUMN_MESSAGE+","+COLUMN_INSERT_DATE+","+COLUMN_UPDATE_DATE+") " +
                         "VALUES('"+title+"','"+encodeStr(msg)+"','"+now+"', '"+now+"' );";
            excuteSQL(sql);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public MemoVo getData(int idx){
        Cursor cursor = null;
        try{
            String sql = "";
            sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_IDX + "='" + idx + "' ";


            cursor = db.rawQuery(sql, null);
            MemoVo memo = null ;
            if (cursor.moveToFirst()) {
                do {

                    memo = new MemoVo(cursor.getInt(0), cursor.getString(1),decodeStr(cursor.getString(2)),cursor.getLong(3),cursor.getLong(4));
                    aLog.d("####### memo : "+memo.toString());
                } while (cursor.moveToNext());
            } else {
                if(cursor != null) cursor.close();
                return null;
            }
            if(cursor != null) cursor.close();
            return memo;
        }catch(Exception e){
            e.printStackTrace();
            if(cursor != null) cursor.close();
            return null;
        }
    }

    public int getData(String title){
        Cursor cursor = null;
        int index = -1;
        try{
            String sql = "";
            sql = "SELECT IDX FROM " + TABLE_NAME + " WHERE " + COLUMN_TITLE + "='" + title + "' ;";


            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(0);
                    aLog.d("####### index : "+index);
                } while (cursor.moveToNext());
            } else {
                if(cursor != null) cursor.close();
                return index;
            }
            if(cursor != null) cursor.close();
            return index;
        }catch(Exception e){
            e.printStackTrace();
            if(cursor != null) cursor.close();
            return index;
        }
    }
    public ArrayList<MemoVo> getData(){
        Cursor cursor = null;
        ArrayList<MemoVo> arrMemo = new ArrayList<MemoVo>();
        try{
            String sql = "";
            sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY IDX ASC;";
            aLog.d("####### sql : "+sql);

            cursor = db.rawQuery(sql, null);
            aLog.d("####### cursor : "+cursor);
            if(cursor != null) {
                while (cursor.moveToNext()) {
                    MemoVo memo = new MemoVo(cursor.getInt(0), cursor.getString(1),decodeStr(cursor.getString(2)),cursor.getLong(3),cursor.getLong(4));
                    aLog.e("####### memo : "+memo.toString());
                    arrMemo.add(memo);
                }
            } else {
                if(cursor != null) cursor.close();
                return null;
            }
            if(cursor != null) cursor.close();
            return arrMemo;
        }catch(Exception e){
            e.printStackTrace();
            if(cursor != null) cursor.close();
            return null;
        }
    }

    public boolean updateData(int key, String title, String msg){
        try{
            String sql = "";
            long now = System.currentTimeMillis();
            sql ="UPDATE " + TABLE_NAME + " SET " + COLUMN_TITLE + " ='"+title+"', "+ COLUMN_MESSAGE + " ='"+encodeStr(msg)+"' , "+COLUMN_UPDATE_DATE+"='"+now+"' WHERE "+COLUMN_IDX+"='"+key+"';";
            excuteSQL(sql);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            dbClose();
            return false;
        }
    }

    public boolean deleteData(int key){
        try{
            String sql = "";
            sql ="DELETE FROM " + TABLE_NAME + " WHERE "+COLUMN_IDX+"='"+key+"';";
            excuteSQL(sql);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            dbClose();
            return false;
        }
    }



    public boolean insertWidget(int widgetId, int key){
        try{

            String sql = "INSERT INTO "+TABLE_WIDGET+"("+COLUMN_WIDGETID+","+COLUMN_KEY+" ) " +
                    "VALUES('"+widgetId+"','"+key+"' );";
            excuteSQL(sql);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Integer> getWidgetId(int key){
        Cursor cursor = null;
        ArrayList<Integer> iList = new ArrayList<>();
        int index = -1;
        try{
            String sql = "";
            sql = "SELECT "+COLUMN_WIDGETID+" FROM " + TABLE_WIDGET + " WHERE " + COLUMN_KEY + "='" + key + "' ;";

            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    iList.add(cursor.getInt(0));
                    aLog.d("####### index : "+cursor.getInt(0));
                } while (cursor.moveToNext());
            } else {
                if(cursor != null) cursor.close();
                return iList;
            }
            if(cursor != null) cursor.close();
            return iList;
        }catch(Exception e){
            e.printStackTrace();
            if(cursor != null) cursor.close();
            return iList;
        }
    }


    public int getWidgetKey(int widgetId){
        Cursor cursor = null;
        int index = -1;
        try{
            String sql = "";
            sql = "SELECT "+COLUMN_KEY+" FROM " + TABLE_WIDGET + " WHERE " + COLUMN_WIDGETID + "='" + widgetId + "' ;";

            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    index = cursor.getInt(0);
                    aLog.d("####### index : "+cursor.getInt(0));
                } while (cursor.moveToNext());
            } else {
                if(cursor != null) cursor.close();
                return index;
            }
            if(cursor != null) cursor.close();
            return index;
        }catch(Exception e){
            e.printStackTrace();
            if(cursor != null) cursor.close();
            return index;
        }
    }

    public boolean updateWidget(int widgetId, int key){
        try{
            String sql = "";
            long now = System.currentTimeMillis();
            sql ="UPDATE " + TABLE_WIDGET + " SET " + COLUMN_KEY + " ='"+key+"' WHERE "+COLUMN_WIDGETID+"='"+widgetId+"';";
            excuteSQL(sql);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            dbClose();
            return false;
        }
    }

    public boolean deleteWidget(int widgetId){
        try{
            String sql = "";
            sql ="DELETE FROM " + TABLE_WIDGET + " WHERE "+COLUMN_WIDGETID+"='"+widgetId+"';";
            excuteSQL(sql);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            dbClose();
            return false;
        }
    }


    public void dbClose(){ //Exception 발생할 때 디비를 닫자
        if(db != null){
            if(db.isOpen()){
                if(db.inTransaction()){
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                db.close();
            }
        }
    }

    private void excuteSQL(String sql){
        try {
            aLog.d("#### "+sql+" ####");
            db.execSQL(sql);
        } catch (SQLiteDatabaseLockedException le) {
            le.printStackTrace();
            aLog.e("EXCUTE : " + le.getMessage());
        } catch (Exception ee){
            ee.printStackTrace();
            aLog.e("EXCUTE : " + ee.getMessage());
        }
    }


}
