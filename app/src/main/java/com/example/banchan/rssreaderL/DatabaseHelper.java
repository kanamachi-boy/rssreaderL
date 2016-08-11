package com.example.banchan.rssreaderL;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//SQLite処理Helper
public class DatabaseHelper extends SQLiteOpenHelper{

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_FILE_NAME = "NewsReaderLITE_0_4.db";
        private Context mContext;
        private SQLiteDatabase mDb;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
                mContext = context;
                mDb = this.getWritableDatabase();
            }     // コンストラクタ

        public void onCreate(SQLiteDatabase db) {
                //
                //  getWritableDatabase();の度にチェックされ
                //  DBが無い時（作成された時）だけ実行される。
                //  DB自体を作成するメソッドは無い！無ければ自動的に作成されるが
                //  それは最初のテーブルをcreteした時。
                //
            try {
                db.execSQL(
                        "CREATE TABLE uri_data ("
                                + "_id integer primary key autoincrement,"
                                + "uri text not null unique, "  //  uri
                                + "last_date text, "            //  最新の発行時刻  longを文字で保存
                                + "name text not null, "               //  名前
                                + "visible  integer not null"
                                + ")"
                );
            }catch (Exception e){
                Log.d("■", "" + e.getMessage());
            }
        }    // DB生成

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int initialSetting (TypedArray uriSet){

        int cnt=0;
        try {
            //  一旦全て削除
            mDb.execSQL("delete from uri_data");

            for (int i = 0; i < uriSet.length(); i++) {

                String[] aaa = uriSet.getString(i).split(";",0);

                long mFlg = insert(aaa[0], aaa[1]);
                if( mFlg > 0){
                    //  成功するとrowid 失敗は　-1
                    cnt ++;
                }
                else{
                    Log.d("■", "" + mFlg);
                }
            }
        }catch (Exception e){
            Log.d("■", "" + e.getMessage());
            return -1;
        }
        //  受け取った配列要素と処理数が合致しないと -1を返す
        if(uriSet.length() == cnt){
            Constants.setPrefrenceBoolean(mContext, Constants.DB_INITIALIZED, true);

            return cnt;
        } else {
            //  リソース数　　成功数
            return - ( uriSet.length() * 1000 + cnt ) ;
        }
    }

    public long insert(String uri, String name){
        /////   新規登録

        if(uri.length() <= 0 || name.length() <= 0){
            return -1000;
        }

        try {

            //  日付を作成
            Date date = new Date();
                    //  最後のニュース（仮）発行日を10日前にしておく
            String strDate = String.format("%d", date.getTime() - 10 * 24 * 60 * 60 * 1000);

            //  DBへInsert
            ContentValues cv = new ContentValues();
            cv.put("uri", uri);   //
            cv.put("last_date", strDate);   //
            cv.put("name", name); //
            cv.put("visible", 1);   //  初期値は1=表示

            long mFlg = mDb.insert("uri_data", null, cv);
            return mFlg;
        }
        catch(SQLiteConstraintException e){
            return -1;
        }
    }

    public int updateVisible (int aID, int visible){
        /////   uri
        try{
            ContentValues cv = new ContentValues();
            visible = visible == 1 ? 1 : 0; //  1=表示以外は 0=非表示
            cv.put("visible", visible);
            return mDb.update("uri_data", cv, "_id=" + aID, null);
        }
        catch(Exception e){
            return -1;
        }
    }

    public ArrayList<String[]> getUriData (int visible){
        /////   表示対象のuriデータを取得

        visible = visible == 1 ? 1 : 0; //  1=表示以外は 0=非表示

        try {
            String sql = "select uri, name from uri_data where visible = " + visible + " order by _id";
            Cursor csr = mDb.rawQuery(sql, null);

            if(csr.getCount() != 0) {

                ArrayList<String[]> rtnUri = new ArrayList<String[]>();
                csr.moveToFirst();
                do {
                    String[] aaa = new String[]{csr.getString(0), csr.getString(1)};
                    rtnUri.add(aaa);

                } while (csr.moveToNext());
                csr.close();
                return rtnUri;
            }
            else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getUriData " + e.getMessage() );
            return null;
        }
    }

    public ArrayList<String[]> getUriDataAll (){
        /////   uriデータを全部取得

        try {
            String sql = "select uri, name, visible, _id from uri_data order by _id";
            Cursor csr = mDb.rawQuery(sql, null);
            if(csr.getCount() != 0) {

                ArrayList<String[]> rtnUri = new ArrayList<String[]>();
                csr.moveToFirst();
                do {
                    String[] aaa = new String[]{
                            csr.getString(0),
                            csr.getString(1),
                            csr.getString(2),
                            csr.getString(3)
                    };
                    rtnUri.add(aaa);

                } while (csr.moveToNext());
                csr.close();
                return rtnUri;
            }
            else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getUriDataAll " + e.getMessage() );
            return null;
        }
    }

    public int delete (Integer id){
        /////   削除

        int result = mDb.delete("uri_data", "_id =" + id, null);
        return result;
    }

}